package com.contextkey.ai.keyboard.input

import android.text.InputType
import android.view.inputmethod.EditorInfo
import com.contextkey.ai.keyboard.security.DefaultInputSecurityPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ContextCollectorTest {

    private lateinit var collector: SafeContextCollector

    @Before
    fun setUp() {
        val securityPolicy = DefaultInputSecurityPolicy()
        collector = SafeContextCollector(securityPolicy)
    }

    @Test
    fun `isContextCollectionAllowed returns false in Phase 1`() {
        val editorInfo = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_TEXT
        }
        assertFalse(collector.isContextCollectionAllowed(editorInfo))
    }

    @Test
    fun `isContextCollectionAllowed returns false for password field`() {
        val editorInfo = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        assertFalse(collector.isContextCollectionAllowed(editorInfo))
    }

    @Test
    fun `getSurroundingContext returns null in Phase 1`() {
        val editorInfo = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_TEXT
        }
        assertNull(collector.getSurroundingContext(null, editorInfo))
    }
}
