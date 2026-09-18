package com.contextkey.ai.keyboard.input

import android.view.KeyEvent
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Proxy

class InputControllerTest {

    private val committedTextList = mutableListOf<String>()
    private val deletedSurrounding = mutableListOf<Pair<Int, Int>>()
    private val performedActions = mutableListOf<Int>()
    private val sentKeyEvents = mutableListOf<Int>()

    private var selectedTextToReturn: CharSequence? = null
    private var testEditorInfo: EditorInfo? = null

    private lateinit var fakeInputConnection: InputConnection
    private lateinit var inputController: InputController

    @Before
    fun setUp() {
        committedTextList.clear()
        deletedSurrounding.clear()
        performedActions.clear()
        sentKeyEvents.clear()
        selectedTextToReturn = null
        testEditorInfo = EditorInfo()

        fakeInputConnection = Proxy.newProxyInstance(
            InputConnection::class.java.classLoader,
            arrayOf(InputConnection::class.java)
        ) { _, method, args ->
            when (method.name) {
                "commitText" -> {
                    committedTextList.add(args[0] as String)
                    true
                }
                "deleteSurroundingText" -> {
                    deletedSurrounding.add(Pair(args[0] as Int, args[1] as Int))
                    true
                }
                "getSelectedText" -> selectedTextToReturn
                "performEditorAction" -> {
                    performedActions.add(args[0] as Int)
                    true
                }
                "sendKeyEvent" -> {
                    val event = args[0] as KeyEvent
                    sentKeyEvents.add(event.keyCode)
                    true
                }
                else -> null
            }
        } as InputConnection

        inputController = InputController(
            inputConnectionProvider = { fakeInputConnection },
            editorInfoProvider = { testEditorInfo }
        )
    }

    @Test
    fun `commitText appends string to input connection`() {
        inputController.commitText("hello")
        assertEquals(listOf("hello"), committedTextList)
    }

    @Test
    fun `insertSpace commits whitespace character`() {
        inputController.insertSpace()
        assertEquals(listOf(" "), committedTextList)
    }

    @Test
    fun `deleteBackward deletes 1 character before cursor when no selection`() {
        selectedTextToReturn = null
        inputController.deleteBackward()
        assertEquals(listOf(Pair(1, 0)), deletedSurrounding)
    }

    @Test
    fun `deleteBackward deletes selection when active selection exists`() {
        selectedTextToReturn = "selected"
        inputController.deleteBackward()
        assertEquals(listOf(""), committedTextList)
    }

    @Test
    fun `handleEnter triggers performEditorAction when action is SEARCH`() {
        testEditorInfo?.imeOptions = EditorInfo.IME_ACTION_SEARCH
        inputController.handleEnter()
        assertEquals(listOf(EditorInfo.IME_ACTION_SEARCH), performedActions)
    }

    @Test
    fun `handleEnter commits newline when multiline text flag is active`() {
        testEditorInfo?.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
        testEditorInfo?.imeOptions = EditorInfo.IME_ACTION_NONE
        inputController.handleEnter()
        assertEquals(listOf("\n"), committedTextList)
    }
}
