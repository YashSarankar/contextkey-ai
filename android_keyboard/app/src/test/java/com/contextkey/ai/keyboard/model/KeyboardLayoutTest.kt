package com.contextkey.ai.keyboard.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardLayoutTest {

    @Test
    fun `shift state transitions cycle correctly`() {
        var state = ShiftState.OFF
        assertFalse(state.isShiftedOrCaps())

        state = state.nextOnTap()
        assertEquals(ShiftState.SHIFTED, state)
        assertTrue(state.isShiftedOrCaps())

        state = state.nextOnTap()
        assertEquals(ShiftState.CAPS_LOCKED, state)
        assertTrue(state.isShiftedOrCaps())

        state = state.nextOnTap()
        assertEquals(ShiftState.OFF, state)
        assertFalse(state.isShiftedOrCaps())
    }

    @Test
    fun `qwerty layout provides 4 rows with all alphabet letters`() {
        val rows = KeyboardLayoutProvider.getQwertyRows()
        assertEquals(4, rows.size)

        val characters = mutableListOf<String>()
        for (row in rows) {
            for (key in row) {
                if (key.action is KeyAction.Character) {
                    characters.add(key.action.normal)
                }
            }
        }

        // Must contain standard 26 English letters plus basic symbols
        val alphabet = ('a'..'z').map { it.toString() }
        for (letter in alphabet) {
            assertTrue("Expected letter $letter in QWERTY layout", characters.contains(letter))
        }
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
