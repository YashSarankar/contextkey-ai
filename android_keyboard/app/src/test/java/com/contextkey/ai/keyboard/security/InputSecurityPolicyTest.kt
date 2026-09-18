package com.contextkey.ai.keyboard.security

import android.text.InputType
import android.view.inputmethod.EditorInfo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InputSecurityPolicyTest {

    private lateinit var policy: InputSecurityPolicy

    @Before
    fun setUp() {
        policy = DefaultInputSecurityPolicy()
    }

    @Test
    fun `null editorInfo is not sensitive and forbids AI`() {
        assertFalse(policy.isSensitiveInput(null))
        assertFalse(policy.isAiAllowed(null))
    }

    @Test
    fun `text password variation is detected as sensitive`() {
        val editorInfo = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        assertTrue(policy.isSensitiveInput(editorInfo))
        assertFalse(policy.isAiAllowed(editorInfo))
    }

    @Test
    fun `web password variation is detected as sensitive`() {
        val editorInfo = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
        }
        assertTrue(policy.isSensitiveInput(editorInfo))
        assertFalse(policy.isAiAllowed(editorInfo))
    }

    @Test
    fun `visible password variation is detected as sensitive`() {
        val editorInfo = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }
        assertTrue(policy.isSensitiveInput(editorInfo))
        assertFalse(policy.isAiAllowed(editorInfo))
    }

    @Test
    fun `number password variation is detected as sensitive`() {
        val editorInfo = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }
        assertTrue(policy.isSensitiveInput(editorInfo))
        assertFalse(policy.isAiAllowed(editorInfo))
    }

    @Test
    fun `no personalized learning flag is detected as sensitive`() {
        val editorInfo = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_TEXT
            imeOptions = EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING
        }
        assertTrue(policy.isSensitiveInput(editorInfo))
        assertFalse(policy.isAiAllowed(editorInfo))
    }

    @Test
    fun `standard text input is not sensitive but AI disabled in Phase 1`() {
        val editorInfo = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
        }
        assertFalse(policy.isSensitiveInput(editorInfo))
        assertFalse(policy.isAiAllowed(editorInfo))
    }

    @Test
    fun `email address input is not sensitive`() {
        val editorInfo = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }
        assertFalse(policy.isSensitiveInput(editorInfo))
        assertFalse(policy.isAiAllowed(editorInfo))
    }
}
