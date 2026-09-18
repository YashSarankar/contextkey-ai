package com.contextkey.ai.keyboard.input

import android.media.AudioManager
import android.text.InputType
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection

/**
 * Coordinates all text mutations performed via [InputConnection],
 * cursor navigation, and sound feedback.
 */
class InputController(
    private val inputConnectionProvider: () -> InputConnection?,
    private val editorInfoProvider: () -> EditorInfo?,
    private val audioManagerProvider: (() -> AudioManager?)? = null
) {

    /**
     * Inserts the given text at the current cursor position.
     */
    fun commitText(text: String) {
        val ic = inputConnectionProvider() ?: return
        ic.commitText(text, 1)
        playSound(AudioManager.FX_KEYPRESS_STANDARD)
    }

    /**
     * Deletes the character immediately preceding the cursor or the active selection.
     */
    fun deleteBackward() {
        val ic = inputConnectionProvider() ?: return
        val selectedText = ic.getSelectedText(0)
        if (!selectedText.isNullOrEmpty()) {
            ic.commitText("", 1)
        } else {
            val success = ic.deleteSurroundingText(1, 0)
            if (!success) {
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL))
            }
        }
        playSound(AudioManager.FX_KEYPRESS_DELETE)
    }

    /**
     * Inserts a single whitespace character.
     */
    fun insertSpace() {
        commitText(" ")
        playSound(AudioManager.FX_KEYPRESS_SPACEBAR)
    }

    /**
     * Replaces previous space with period and space for double-space shortcut.
     */
    fun replaceWithPeriodSpace() {
        val ic = inputConnectionProvider() ?: return
        val textBefore = ic.getTextBeforeCursor(2, 0)
        if (textBefore != null && textBefore.endsWith(" ")) {
            ic.deleteSurroundingText(1, 0)
            ic.commitText(". ", 1)
            playSound(AudioManager.FX_KEYPRESS_STANDARD)
        } else {
            insertSpace()
        }
    }

    /**
     * Moves cursor left or right (used for spacebar glide gestures).
     */
    fun moveCursor(offset: Int) {
        val ic = inputConnectionProvider() ?: return
        val keycode = if (offset < 0) KeyEvent.KEYCODE_DPAD_LEFT else KeyEvent.KEYCODE_DPAD_RIGHT
        val count = kotlin.math.abs(offset)
        for (i in 0 until count) {
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keycode))
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keycode))
        }
    }

    /**
     * Handles the Enter / Action key based on the target [EditorInfo].
     */
    fun handleEnter() {
        val ic = inputConnectionProvider() ?: return
        val editorInfo = editorInfoProvider()

        playSound(AudioManager.FX_KEYPRESS_RETURN)

        if (editorInfo == null) {
            ic.commitText("\n", 1)
            return
        }

        val imeOptions = editorInfo.imeOptions
        val actionId = imeOptions and EditorInfo.IME_MASK_ACTION
        val noEnterAction = (imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0
        val isMultiLine = (editorInfo.inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0

        if (!noEnterAction && actionId != EditorInfo.IME_ACTION_NONE && actionId != EditorInfo.IME_ACTION_UNSPECIFIED) {
            ic.performEditorAction(actionId)
        } else if (isMultiLine) {
            ic.commitText("\n", 1)
        } else if (actionId != EditorInfo.IME_ACTION_NONE && actionId != EditorInfo.IME_ACTION_UNSPECIFIED) {
            ic.performEditorAction(actionId)
        } else {
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        }
    }

    private fun playSound(effectType: Int) {
        try {
            audioManagerProvider?.invoke()?.playSoundEffect(effectType)
        } catch (_: Exception) {
            // Ignore audio exceptions safely
        }
    }
}
