package com.contextkey.ai.keyboard.ui

import com.contextkey.ai.keyboard.input.InputController
import com.contextkey.ai.keyboard.model.KeyAction
import com.contextkey.ai.keyboard.model.KeyItem
import com.contextkey.ai.keyboard.model.KeyboardMode
import com.contextkey.ai.keyboard.model.ShiftState

/**
 * Manages keyboard modes, shift state transitions, and dispatches actions to [InputController].
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

    fun resetState() {
        keyboardMode = KeyboardMode.QWERTY
        shiftState = ShiftState.OFF
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
                shiftState = shiftState.nextOnTap()
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
}
