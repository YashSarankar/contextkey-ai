package com.contextkey.ai.keyboard.ime

import android.content.Context
import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.media.AudioManager
import android.os.Build
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import com.contextkey.ai.keyboard.input.ContextCollector
import com.contextkey.ai.keyboard.input.InputController
import com.contextkey.ai.keyboard.input.SafeContextCollector
import com.contextkey.ai.keyboard.model.ShiftState
import com.contextkey.ai.keyboard.security.DefaultInputSecurityPolicy
import com.contextkey.ai.keyboard.security.InputSecurityPolicy
import com.contextkey.ai.keyboard.ui.KeyboardController
import com.contextkey.ai.keyboard.ui.KeyboardView

/**
 * Native Android Input Method Service for ContextKey Keyboard.
 *
 * Implements the Android IME lifecycle cleanly, safely, and delivers
 * Gboard-grade responsiveness, insets, and typing features.
 */
class ContextKeyInputMethodService : InputMethodService() {

    private lateinit var securityPolicy: InputSecurityPolicy
    private lateinit var contextCollector: ContextCollector
    private lateinit var inputController: InputController
    private lateinit var keyboardController: KeyboardController

    private var keyboardView: KeyboardView? = null
    private var currentEditorInfo: EditorInfo? = null

    override fun onCreate() {
        super.onCreate()

        securityPolicy = DefaultInputSecurityPolicy()
        contextCollector = SafeContextCollector(securityPolicy)

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager

        inputController = InputController(
            inputConnectionProvider = { currentInputConnection },
            editorInfoProvider = { currentEditorInfo },
            audioManagerProvider = { audioManager }
        )

        keyboardController = KeyboardController(
            inputController = inputController,
            onStateChanged = { isFullModeChange ->
                if (isFullModeChange) {
                    keyboardView?.render()
                } else {
                    keyboardView?.updateShiftStateOnly()
                }
            },
            onSwitchImeRequested = {
                switchInputMethod()
            }
        )
    }

    override fun onConfigureWindow(win: Window, isFullscreen: Boolean, isCandidatesOnly: Boolean) {
        super.onConfigureWindow(win, isFullscreen, isCandidatesOnly)
        win.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        win.setGravity(Gravity.BOTTOM)
    }

    /**
     * Prevent full-screen extract mode in landscape or large screens,
     * allowing the host application to resize/pan and maintain text field visibility.
     */
    override fun onEvaluateFullscreenMode(): Boolean {
        return false
    }

    override fun onEvaluateInputViewShown(): Boolean {
        return true
    }

    override fun onComputeInsets(outInsets: Insets) {
        super.onComputeInsets(outInsets)
        val kbView = keyboardView
        if (kbView != null && isInputViewShown) {
            val location = IntArray(2)
            kbView.getLocationInWindow(location)
            outInsets.contentTopInsets = location[1]
            outInsets.visibleTopInsets = location[1]
            outInsets.touchableInsets = Insets.TOUCHABLE_INSETS_VISIBLE
            outInsets.touchableRegion.setEmpty()
        }
    }

    override fun onCreateInputView(): View {
        val view = KeyboardView(this)
        view.attachController(keyboardController)
        view.onCloseKeyboardRequested = {
            requestHideSelf(0)
        }
        this.keyboardView = view
        return view
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        this.currentEditorInfo = attribute

        if (!restarting) {
            val initialShift = computeAutoCapitalization(attribute)
            keyboardController.resetState()
            if (initialShift != ShiftState.OFF) {
                keyboardController.setInitialShiftState(initialShift)
            }
        }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        this.currentEditorInfo = info
        keyboardView?.setEditorInfo(info)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        keyboardView?.render()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
    }

    override fun onFinishInput() {
        super.onFinishInput()
        currentEditorInfo = null
    }

    override fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int
    ) {
        super.onUpdateSelection(
            oldSelStart,
            oldSelEnd,
            newSelStart,
            newSelEnd,
            candidatesStart,
            candidatesEnd
        )
    }

    override fun onBindInput() {
        super.onBindInput()
    }

    private fun computeAutoCapitalization(editorInfo: EditorInfo?): ShiftState {
        if (editorInfo == null) return ShiftState.OFF

        val inputType = editorInfo.inputType
        val inputClass = inputType and InputType.TYPE_MASK_CLASS

        if (inputClass != InputType.TYPE_CLASS_TEXT) {
            return ShiftState.OFF
        }

        // Do not auto-capitalize password, email, or URL fields
        val variation = inputType and InputType.TYPE_MASK_VARIATION
        when (variation) {
            InputType.TYPE_TEXT_VARIATION_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
            InputType.TYPE_TEXT_VARIATION_URI -> return ShiftState.OFF
        }

        val flags = inputType and InputType.TYPE_MASK_FLAGS
        return when {
            (flags and InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS) != 0 -> ShiftState.CAPS_LOCKED
            (flags and InputType.TYPE_TEXT_FLAG_CAP_WORDS) != 0 -> ShiftState.SHIFTED
            (flags and InputType.TYPE_TEXT_FLAG_CAP_SENTENCES) != 0 -> ShiftState.SHIFTED
            else -> ShiftState.SHIFTED
        }
    }

    /**
     * Attempts switching to the next available system input method,
     * falling back to the system IME picker dialog.
     */
    private fun switchInputMethod() {
        var switched = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (shouldOfferSwitchingToNextInputMethod()) {
                switched = switchToNextInputMethod(false)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            @Suppress("DEPRECATION")
            if (shouldOfferSwitchingToNextInputMethod()) {
                @Suppress("DEPRECATION")
                switched = switchToNextInputMethod(false)
            }
        }

        if (!switched) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showInputMethodPicker()
        }
    }
}
