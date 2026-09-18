package com.contextkey.ai.keyboard.model

import com.contextkey.ai.keyboard.R

/**
 * Supported keyboard modes.
 */
enum class KeyboardMode {
    QWERTY,
    NUMERIC_SYMBOLS,
    MORE_SYMBOLS
}

/**
 * Shift state transitions.
 */
enum class ShiftState {
    OFF,
    SHIFTED,
    CAPS_LOCKED;

    fun isShiftedOrCaps(): Boolean = this == SHIFTED || this == CAPS_LOCKED

    fun nextOnTap(): ShiftState = when (this) {
        OFF -> SHIFTED
        SHIFTED -> CAPS_LOCKED
        CAPS_LOCKED -> OFF
    }
}

/**
 * Types of actions keys can perform.
 */
sealed class KeyAction {
    data class Character(val normal: String, val shifted: String = normal.uppercase()) : KeyAction()
    object Shift : KeyAction()
    object Backspace : KeyAction()
    object Space : KeyAction()
    object Enter : KeyAction()
    data class SwitchMode(val targetMode: KeyboardMode) : KeyAction()
    object SwitchIme : KeyAction()
    object PlaceholderAi : KeyAction()
}

/**
 * Represents a single key in the keyboard UI.
 */
data class KeyItem(
    val action: KeyAction,
    val primaryLabel: String? = null,
    val secondaryLabel: String? = null,
    val iconResId: Int? = null,
    val widthWeight: Float = 1.0f,
    val isSpecial: Boolean = false,
    val isAction: Boolean = false
) {
    companion object {
        fun charKey(char: Char, weight: Float = 1.0f): KeyItem {
            val normal = char.toString()
            return KeyItem(
                action = KeyAction.Character(normal),
                primaryLabel = normal,
                widthWeight = weight
            )
        }

        fun symbolKey(symbol: String, weight: Float = 1.0f): KeyItem {
            return KeyItem(
                action = KeyAction.Character(symbol, symbol),
                primaryLabel = symbol,
                widthWeight = weight
            )
        }
    }
}

/**
 * Provides static definitions for QWERTY and Symbol keyboard layouts.
 */
object KeyboardLayoutProvider {

    fun getQwertyRows(): List<List<KeyItem>> {
        val row1 = listOf('q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p').map { KeyItem.charKey(it) }

        val row2 = listOf('a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l').map { KeyItem.charKey(it) }

        val row3 = mutableListOf<KeyItem>().apply {
            add(
                KeyItem(
                    action = KeyAction.Shift,
                    iconResId = R.drawable.ic_shift,
                    widthWeight = 1.4f,
                    isSpecial = true
                )
            )
            addAll(listOf('z', 'x', 'c', 'v', 'b', 'n', 'm').map { KeyItem.charKey(it) })
            add(
                KeyItem(
                    action = KeyAction.Backspace,
                    iconResId = R.drawable.ic_backspace,
                    widthWeight = 1.4f,
                    isSpecial = true
                )
            )
        }

        val row4 = listOf(
            KeyItem(
                action = KeyAction.SwitchMode(KeyboardMode.NUMERIC_SYMBOLS),
                primaryLabel = "?123",
                widthWeight = 1.3f,
                isSpecial = true
            ),
            KeyItem(
                action = KeyAction.SwitchIme,
                iconResId = R.drawable.ic_globe,
                widthWeight = 1.0f,
                isSpecial = true
            ),
            KeyItem.symbolKey(",", weight = 1.0f),
            KeyItem(
                action = KeyAction.Space,
                primaryLabel = "space",
                widthWeight = 4.2f
            ),
            KeyItem.symbolKey(".", weight = 1.0f),
            KeyItem(
                action = KeyAction.Enter,
                iconResId = R.drawable.ic_enter,
                widthWeight = 1.5f,
                isSpecial = true,
                isAction = true
            )
        )

        return listOf(row1, row2, row3, row4)
    }

    fun getNumericSymbolsRows(): List<List<KeyItem>> {
        val row1 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0").map { KeyItem.symbolKey(it) }

        val row2 = listOf("@", "#", "$", "%", "&", "-", "+", "(", ")", "/").map { KeyItem.symbolKey(it) }

        val row3 = mutableListOf<KeyItem>().apply {
            add(
                KeyItem(
                    action = KeyAction.SwitchMode(KeyboardMode.MORE_SYMBOLS),
                    primaryLabel = "=\\<",
                    widthWeight = 1.4f,
                    isSpecial = true
                )
            )
            addAll(listOf("*", "\"", "\'", ":", ";", "!", "?").map { KeyItem.symbolKey(it) })
            add(
                KeyItem(
                    action = KeyAction.Backspace,
                    iconResId = R.drawable.ic_backspace,
                    widthWeight = 1.4f,
                    isSpecial = true
                )
            )
        }

        val row4 = listOf(
            KeyItem(
                action = KeyAction.SwitchMode(KeyboardMode.QWERTY),
                primaryLabel = "ABC",
                widthWeight = 1.3f,
                isSpecial = true
            ),
            KeyItem(
                action = KeyAction.SwitchIme,
                iconResId = R.drawable.ic_globe,
                widthWeight = 1.0f,
                isSpecial = true
            ),
            KeyItem.symbolKey(",", weight = 1.0f),
            KeyItem(
                action = KeyAction.Space,
                primaryLabel = "space",
                widthWeight = 4.2f
            ),
            KeyItem.symbolKey(".", weight = 1.0f),
            KeyItem(
                action = KeyAction.Enter,
                iconResId = R.drawable.ic_enter,
                widthWeight = 1.5f,
                isSpecial = true,
                isAction = true
            )
        )

        return listOf(row1, row2, row3, row4)
    }

    fun getMoreSymbolsRows(): List<List<KeyItem>> {
        val row1 = listOf("~", "`", "|", "•", "√", "π", "÷", "×", "¶", "∆").map { KeyItem.symbolKey(it) }

        val row2 = listOf("£", "€", "¥", "¢", "^", "°", "=", "{", "}", "\\").map { KeyItem.symbolKey(it) }

        val row3 = mutableListOf<KeyItem>().apply {
            add(
                KeyItem(
                    action = KeyAction.SwitchMode(KeyboardMode.NUMERIC_SYMBOLS),
                    primaryLabel = "?123",
                    widthWeight = 1.4f,
                    isSpecial = true
                )
            )
            addAll(listOf("%", "_", "<", ">", "[", "]", "§").map { KeyItem.symbolKey(it) })
            add(
                KeyItem(
                    action = KeyAction.Backspace,
                    iconResId = R.drawable.ic_backspace,
                    widthWeight = 1.4f,
                    isSpecial = true
                )
            )
        }

        val row4 = listOf(
            KeyItem(
                action = KeyAction.SwitchMode(KeyboardMode.QWERTY),
                primaryLabel = "ABC",
                widthWeight = 1.3f,
                isSpecial = true
            ),
            KeyItem(
                action = KeyAction.SwitchIme,
                iconResId = R.drawable.ic_globe,
                widthWeight = 1.0f,
                isSpecial = true
            ),
            KeyItem.symbolKey("<", weight = 1.0f),
            KeyItem(
                action = KeyAction.Space,
                primaryLabel = "space",
                widthWeight = 4.2f
            ),
            KeyItem.symbolKey(">", weight = 1.0f),
            KeyItem(
                action = KeyAction.Enter,
                iconResId = R.drawable.ic_enter,
                widthWeight = 1.5f,
                isSpecial = true,
                isAction = true
            )
        )

        return listOf(row1, row2, row3, row4)
    }
}
