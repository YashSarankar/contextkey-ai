package com.contextkey.ai.keyboard.input

import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import com.contextkey.ai.keyboard.security.InputSecurityPolicy

/**
 * Interface defining the boundary for future context extraction.
 *
 * In Phase 1:
 * - Returns null/empty for all context queries.
 * - Enforces zero raw text logging.
 * - Strictly respects [InputSecurityPolicy].
 * - Performs no network or background analysis.
 */
interface ContextCollector {
    fun getSurroundingContext(inputConnection: InputConnection?, editorInfo: EditorInfo?): String?
    fun isContextCollectionAllowed(editorInfo: EditorInfo?): Boolean
}

class SafeContextCollector(
    private val securityPolicy: InputSecurityPolicy
) : ContextCollector {

    override fun getSurroundingContext(inputConnection: InputConnection?, editorInfo: EditorInfo?): String? {
        if (!isContextCollectionAllowed(editorInfo)) {
            return null
        }
        // In Phase 1, context collection is deliberately disabled and returns null.
        return null
    }

    override fun isContextCollectionAllowed(editorInfo: EditorInfo?): Boolean {
        if (securityPolicy.isSensitiveInput(editorInfo)) {
            return false
        }
        // Phase 1 rule: Never collect context
        return false
    }
}
