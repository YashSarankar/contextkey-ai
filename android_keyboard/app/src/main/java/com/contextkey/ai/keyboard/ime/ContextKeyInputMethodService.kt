package com.contextkey.ai.keyboard.ime

import android.inputmethodservice.InputMethodService
import android.os.Build
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputBinding
import android.view.inputmethod.InputMethodManager
import com.contextkey.ai.keyboard.input.ContextCollector
import com.contextkey.ai.keyboard.input.InputController
import com.contextkey.ai.keyboard.input.SafeContextCollector
import com.contextkey.ai.keyboard.security.DefaultInputSecurityPolicy
import com.contextkey.ai.keyboard.security.InputSecurityPolicy
import com.contextkey.ai.keyboard.ui.KeyboardController
import com.contextkey.ai.keyboard.ui.KeyboardView

/**
 * Native Android Input Method Service for ContextKey Keyboard.
 *
 * Implements the Android IME lifecycle cleanly and safely.
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

        inputController = InputController(
            inputConnectionProvider = { currentInputConnection },
            editorInfoProvider = { currentEditorInfo }
        )

        keyboardController = KeyboardController(
            inputController = inputController,
            onStateChanged = {
                keyboardView?.render()
            },
            onSwitchImeRequested = {
                switchInputMethod()
            }
        )
    }

    override fun onCreateInputView(): View {
        val view = KeyboardView(this)
        view.attachController(keyboardController)
        this.keyboardView = view
        return view
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        this.currentEditorInfo = attribute

        // Reset mode and shift state on fresh input
        if (!restarting) {
            keyboardController.resetState()
        }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        this.currentEditorInfo = info
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
