package com.contextkey.ai.keyboard.model

import android.view.inputmethod.EditorInfo
import com.contextkey.ai.keyboard.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardLayoutTest {

    @Test
    fun `shift state isShiftedOrCaps matches shifted and caps locked states`() {
        assertFalse(ShiftState.OFF.isShiftedOrCaps())
        assertTrue(ShiftState.SHIFTED.isShiftedOrCaps())
        assertTrue(ShiftState.CAPS_LOCKED.isShiftedOrCaps())
    }

    @Test
    fun `qwerty layout provides 4 rows with all alphabet letters and number hints`() {
        val rows = KeyboardLayoutProvider.getQwertyRows()
        assertEquals(4, rows.size)

        val characters = mutableListOf<String>()
        val secondaryHints = mutableListOf<String>()
        for (row in rows) {
            for (key in row) {
                if (key.action is KeyAction.Character) {
                    characters.add(key.action.normal)
                    key.action.longPressText?.let { secondaryHints.add(it) }
                }
            }
        }

        // Must contain standard 26 English letters
        val alphabet = ('a'..'z').map { it.toString() }
        for (letter in alphabet) {
            assertTrue("Expected letter $letter in QWERTY layout", characters.contains(letter))
        }

        // Top row must have digits 1 to 0 as secondary long-press hints
        val digits = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
        for (digit in digits) {
            assertTrue("Expected secondary hint $digit in top row", secondaryHints.contains(digit))
        }
    }

    @Test
    fun `enter key icon maps dynamically to editor action`() {
        val searchEditorInfo = EditorInfo().apply { imeOptions = EditorInfo.IME_ACTION_SEARCH }
        assertEquals(R.drawable.ic_action_search, KeyboardLayoutProvider.getEnterKeyIcon(searchEditorInfo))

        val sendEditorInfo = EditorInfo().apply { imeOptions = EditorInfo.IME_ACTION_SEND }
        assertEquals(R.drawable.ic_action_send, KeyboardLayoutProvider.getEnterKeyIcon(sendEditorInfo))

        val doneEditorInfo = EditorInfo().apply { imeOptions = EditorInfo.IME_ACTION_DONE }
        assertEquals(R.drawable.ic_action_done, KeyboardLayoutProvider.getEnterKeyIcon(doneEditorInfo))
    }

    @Test
    fun `numeric symbols layout contains digits 0 to 9`() {
        val rows = KeyboardLayoutProvider.getNumericSymbolsRows()
        assertEquals(4, rows.size)

        val row1Chars = rows[0].mapNotNull {
            (it.action as? KeyAction.Character)?.normal
        }
        val digits = ('1'..'9').map { it.toString() } + listOf("0")
        assertEquals(digits, row1Chars)
    }

    @Test
    fun `more symbols layout contains extended symbols`() {
        val rows = KeyboardLayoutProvider.getMoreSymbolsRows()
        assertEquals(4, rows.size)

        val row1Chars = rows[0].mapNotNull {
            (it.action as? KeyAction.Character)?.normal
        }
        assertTrue(row1Chars.contains("~"))
        assertTrue(row1Chars.contains("`"))
    }
}
