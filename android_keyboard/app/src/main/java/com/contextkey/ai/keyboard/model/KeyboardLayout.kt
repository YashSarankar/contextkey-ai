package com.contextkey.ai.keyboard.model

import android.text.InputType
import android.view.inputmethod.EditorInfo
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
}

/**
 * Types of actions keys can perform.
 */
sealed class KeyAction {
    data class Character(
        val normal: String,
        val shifted: String = normal.uppercase(),
        val longPressText: String? = null
    ) : KeyAction()

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
        fun charKey(char: Char, secondaryHint: String? = null, weight: Float = 1.0f): KeyItem {
            val normal = char.toString()
            return KeyItem(
                action = KeyAction.Character(normal, normal.uppercase(), secondaryHint),
                primaryLabel = normal,
                secondaryLabel = secondaryHint,
                widthWeight = weight
            )
        }

        fun symbolKey(symbol: String, longPressHint: String? = null, weight: Float = 1.0f): KeyItem {
            return KeyItem(
                action = KeyAction.Character(symbol, symbol, longPressHint),
                primaryLabel = symbol,
                secondaryLabel = longPressHint,
                widthWeight = weight
            )
        }
    }
}

/**
 * Provides static definitions for QWERTY and Symbol keyboard layouts.
 */
object KeyboardLayoutProvider {

    fun getEnterKeyIcon(editorInfo: EditorInfo?): Int {
        if (editorInfo == null) return R.drawable.ic_enter

        val isMultiLine = (editorInfo.inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0
        if (isMultiLine) return R.drawable.ic_enter

        val action = editorInfo.imeOptions and (EditorInfo.IME_MASK_ACTION or EditorInfo.IME_FLAG_NO_ENTER_ACTION)
        val noEnter = (editorInfo.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0

        if (noEnter) return R.drawable.ic_enter

        return when (action and EditorInfo.IME_MASK_ACTION) {
            EditorInfo.IME_ACTION_SEARCH -> R.drawable.ic_action_search
            EditorInfo.IME_ACTION_SEND -> R.drawable.ic_action_send
            EditorInfo.IME_ACTION_GO -> R.drawable.ic_action_go
            EditorInfo.IME_ACTION_DONE -> R.drawable.ic_action_done
            EditorInfo.IME_ACTION_NEXT -> R.drawable.ic_action_next
            else -> R.drawable.ic_enter
        }
    }

    fun getQwertyRows(editorInfo: EditorInfo? = null): List<List<KeyItem>> {
        val topRowChars = listOf('q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p')
        val topRowDigits = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
        val row1 = topRowChars.zip(topRowDigits) { c, d -> KeyItem.charKey(c, d) }

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

        val enterIcon = getEnterKeyIcon(editorInfo)

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
            KeyItem.symbolKey(",", longPressHint = ";", weight = 1.0f),
            KeyItem(
                action = KeyAction.Space,
                primaryLabel = "space",
                widthWeight = 4.2f
            ),
            KeyItem.symbolKey(".", longPressHint = "?", weight = 1.0f),
            KeyItem(
                action = KeyAction.Enter,
                iconResId = enterIcon,
                widthWeight = 1.5f,
                isSpecial = true,
                isAction = true
            )
        )

        return listOf(row1, row2, row3, row4)
    }

    fun getNumericSymbolsRows(editorInfo: EditorInfo? = null): List<List<KeyItem>> {
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

        val enterIcon = getEnterKeyIcon(editorInfo)

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
                iconResId = enterIcon,
                widthWeight = 1.5f,
                isSpecial = true,
                isAction = true
            )
        )

        return listOf(row1, row2, row3, row4)
    }

    fun getMoreSymbolsRows(editorInfo: EditorInfo? = null): List<List<KeyItem>> {
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

        val enterIcon = getEnterKeyIcon(editorInfo)

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
                iconResId = enterIcon,
                widthWeight = 1.5f,
                isSpecial = true,
                isAction = true
            )
        )

        return listOf(row1, row2, row3, row4)
    }
}
