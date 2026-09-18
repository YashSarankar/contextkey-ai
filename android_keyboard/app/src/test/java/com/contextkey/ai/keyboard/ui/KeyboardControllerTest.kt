package com.contextkey.ai.keyboard.ui

import android.view.inputmethod.EditorInfo
import com.contextkey.ai.keyboard.input.InputController
import com.contextkey.ai.keyboard.model.KeyAction
import com.contextkey.ai.keyboard.model.KeyItem
import com.contextkey.ai.keyboard.model.ShiftState
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Proxy
import android.view.inputmethod.InputConnection

class KeyboardControllerTest {

    private val committedTexts = mutableListOf<String>()
    private var stateChangeCount = 0
    private var imeSwitchCount = 0

    private lateinit var controller: KeyboardController

    @Before
    fun setUp() {
        committedTexts.clear()
        stateChangeCount = 0
        imeSwitchCount = 0

        val fakeIc = Proxy.newProxyInstance(
            InputConnection::class.java.classLoader,
            arrayOf(InputConnection::class.java)
        ) { _, method, args ->
            if (method.name == "commitText") {
                committedTexts.add(args[0] as String)
                true
            } else null
        } as InputConnection

        val inputController = InputController(
            inputConnectionProvider = { fakeIc },
            editorInfoProvider = { EditorInfo() }
        )

        controller = KeyboardController(
            inputController = inputController,
            onStateChanged = { stateChangeCount++ },
            onSwitchImeRequested = { imeSwitchCount++ }
        )
    }

    @Test
    fun `single tap shift enters SHIFTED and typing character resets to OFF`() {
        assertEquals(ShiftState.OFF, controller.shiftState)

        controller.handleKeyClick(KeyItem(action = KeyAction.Shift))
        assertEquals(ShiftState.SHIFTED, controller.shiftState)

        controller.handleKeyClick(KeyItem.charKey('a'))
        assertEquals(listOf("A"), committedTexts)
        assertEquals(ShiftState.OFF, controller.shiftState)
    }

    @Test
    fun `caps lock state persists after typing characters`() {
        controller.setInitialShiftState(ShiftState.CAPS_LOCKED)

        controller.handleKeyClick(KeyItem.charKey('b'))
        controller.handleKeyClick(KeyItem.charKey('c'))

        assertEquals(listOf("B", "C"), committedTexts)
        assertEquals(ShiftState.CAPS_LOCKED, controller.shiftState)
    }

    @Test
    fun `long click on key commits secondary character`() {
        val keyWithHint = KeyItem.charKey('q', secondaryHint = "1")
        controller.handleKeyLongClick(keyWithHint)

        assertEquals(listOf("1"), committedTexts)
    }
}
