package com.contextkey.ai.keyboard.ui

import com.contextkey.ai.keyboard.input.InputController
import com.contextkey.ai.keyboard.model.KeyAction
import com.contextkey.ai.keyboard.model.KeyItem
import com.contextkey.ai.keyboard.model.KeyboardMode
import com.contextkey.ai.keyboard.model.ShiftState

/**
 * Manages keyboard modes, double-tap shift transitions (Caps Lock),
 * and dispatches actions to [InputController].
 */
class KeyboardController(
    private val inputController: InputController,
    private val onStateChanged: () -> Unit,
    private val onSwitchImeRequested: () -> Unit
) {

    var keyboardMode: KeyboardMode = KeyboardMode.QWERTY
        private set

    var shiftState: ShiftState = ShiftState.OFF
        private set

    private var lastShiftTapTime: Long = 0L

    fun resetState() {
        keyboardMode = KeyboardMode.QWERTY
        shiftState = ShiftState.OFF
        lastShiftTapTime = 0L
        onStateChanged()
    }

    fun setInitialShiftState(initialState: ShiftState) {
        this.shiftState = initialState
        onStateChanged()
    }

    fun handleKeyClick(keyItem: KeyItem) {
        when (val action = keyItem.action) {
            is KeyAction.Character -> {
                val text = if (shiftState.isShiftedOrCaps()) action.shifted else action.normal
                inputController.commitText(text)

                // If single-tap shift was active, unshift after typing one character
                if (shiftState == ShiftState.SHIFTED) {
                    shiftState = ShiftState.OFF
                    onStateChanged()
                }
            }

            is KeyAction.Shift -> {
                val currentTime = System.currentTimeMillis()
                val isDoubleTap = (currentTime - lastShiftTapTime) < DOUBLE_TAP_TIMEOUT_MS

                shiftState = when {
                    shiftState == ShiftState.CAPS_LOCKED -> ShiftState.OFF
                    isDoubleTap && shiftState == ShiftState.SHIFTED -> ShiftState.CAPS_LOCKED
                    shiftState == ShiftState.SHIFTED -> ShiftState.OFF
                    else -> ShiftState.SHIFTED
                }
                lastShiftTapTime = currentTime
                onStateChanged()
            }

            is KeyAction.Backspace -> {
                inputController.deleteBackward()
            }

            is KeyAction.Space -> {
                inputController.insertSpace()
            }

            is KeyAction.Enter -> {
                inputController.handleEnter()
            }

            is KeyAction.SwitchMode -> {
                keyboardMode = action.targetMode
                onStateChanged()
            }

            is KeyAction.SwitchIme -> {
                onSwitchImeRequested()
            }

            is KeyAction.PlaceholderAi -> {
                // Phase 1 placeholder: no action performed
            }
        }
    }

    fun handleKeyLongClick(keyItem: KeyItem) {
        val charAction = keyItem.action as? KeyAction.Character
        val longPressChar = charAction?.longPressText ?: return
        inputController.commitText(longPressChar)
    }

    companion object {
        private const val DOUBLE_TAP_TIMEOUT_MS = 400L
    }
}
