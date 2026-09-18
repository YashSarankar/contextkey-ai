package com.contextkey.ai.keyboard.ui

import com.contextkey.ai.keyboard.input.InputController
import com.contextkey.ai.keyboard.model.KeyAction
import com.contextkey.ai.keyboard.model.KeyItem
import com.contextkey.ai.keyboard.model.KeyboardMode
import com.contextkey.ai.keyboard.model.ShiftState

/**
 * Manages keyboard modes, double-tap shift transitions (Caps Lock),
 * double-tap space period shortcuts, cursor glides, and dispatches actions to [InputController].
 */
class KeyboardController(
    private val inputController: InputController,
    private val onStateChanged: (Boolean) -> Unit, // isFullModeChange: Boolean
    private val onSwitchImeRequested: () -> Unit
) {

    var keyboardMode: KeyboardMode = KeyboardMode.QWERTY
        private set

    var shiftState: ShiftState = ShiftState.OFF
        private set

    private var lastShiftTapTime: Long = 0L
    private var lastSpaceTapTime: Long = 0L

    fun resetState() {
        keyboardMode = KeyboardMode.QWERTY
        shiftState = ShiftState.OFF
        lastShiftTapTime = 0L
        lastSpaceTapTime = 0L
        onStateChanged(true)
    }

    fun setInitialShiftState(initialState: ShiftState) {
        this.shiftState = initialState
        onStateChanged(false)
    }

    fun handleKeyClick(keyItem: KeyItem) {
        when (val action = keyItem.action) {
            is KeyAction.Character -> {
                val text = if (shiftState.isShiftedOrCaps()) action.shifted else action.normal
                inputController.commitText(text)

                // If single-tap shift was active, unshift after typing one character
                if (shiftState == ShiftState.SHIFTED) {
                    shiftState = ShiftState.OFF
                    onStateChanged(false)
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
                onStateChanged(false)
            }

            is KeyAction.Backspace -> {
                inputController.deleteBackward()
            }

            is KeyAction.Space -> {
                val currentTime = System.currentTimeMillis()
                val isDoubleSpace = (currentTime - lastSpaceTapTime) < DOUBLE_SPACE_TIMEOUT_MS

                if (isDoubleSpace) {
                    inputController.replaceWithPeriodSpace()
                    shiftState = ShiftState.SHIFTED
                    onStateChanged(false)
                    lastSpaceTapTime = 0L
                } else {
                    inputController.insertSpace()
                    lastSpaceTapTime = currentTime
                }
            }

            is KeyAction.Enter -> {
                inputController.handleEnter()
            }

            is KeyAction.SwitchMode -> {
                keyboardMode = action.targetMode
                onStateChanged(true)
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

    fun handleSpaceGlide(offset: Int) {
        inputController.moveCursor(offset)
    }

    companion object {
        private const val DOUBLE_TAP_TIMEOUT_MS = 400L
        private const val DOUBLE_SPACE_TIMEOUT_MS = 400L
    }
}
