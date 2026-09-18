package com.contextkey.ai.keyboard.input

import android.text.InputType
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection

/**
 * Coordinates all text mutations performed via [InputConnection].
 */
class InputController(
    private val inputConnectionProvider: () -> InputConnection?,
    private val editorInfoProvider: () -> EditorInfo?
) {

    /**
     * Inserts the given text at the current cursor position.
     */
    fun commitText(text: String) {
        val ic = inputConnectionProvider() ?: return
        ic.commitText(text, 1)
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
            // Standard backspace deletion
            val success = ic.deleteSurroundingText(1, 0)
            if (!success) {
                // Fallback to key events for webviews / non-standard editors
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL))
            }
        }
    }

    /**
     * Inserts a single whitespace character.
     */
    fun insertSpace() {
        commitText(" ")
    }

    /**
     * Handles the Enter / Action key based on the target [EditorInfo].
     */
    fun handleEnter() {
        val ic = inputConnectionProvider() ?: return
        val editorInfo = editorInfoProvider()

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
}
