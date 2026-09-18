package com.contextkey.ai.keyboard.security

import android.text.InputType
import android.view.inputmethod.EditorInfo

/**
 * Enforces security and privacy boundaries by inspecting the target field's [EditorInfo].
 *
 * Password fields, PIN fields, and fields requesting no personalized learning are strictly
 * flagged as sensitive. When an input field is sensitive:
 * - No context is collected.
 * - No AI processing or background suggestions will be performed.
 * - No text is logged or stored.
 */
interface InputSecurityPolicy {
    fun isSensitiveInput(editorInfo: EditorInfo?): Boolean
    fun isAiAllowed(editorInfo: EditorInfo?): Boolean
}

class DefaultInputSecurityPolicy : InputSecurityPolicy {

    override fun isSensitiveInput(editorInfo: EditorInfo?): Boolean {
        if (editorInfo == null) return false

        val inputType = editorInfo.inputType
        val inputClass = inputType and InputType.TYPE_MASK_CLASS
        val variation = inputType and InputType.TYPE_MASK_VARIATION

        // Text password variations
        if (inputClass == InputType.TYPE_CLASS_TEXT) {
            when (variation) {
                InputType.TYPE_TEXT_VARIATION_PASSWORD,
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
                InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD -> return true
            }
        }

        // Number password variations (PINs, etc.)
        if (inputClass == InputType.TYPE_CLASS_NUMBER) {
            if (variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD) {
                return true
            }
        }

        // Private / Incognito / No Personalized Learning Flag
        if ((editorInfo.imeOptions and EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING) != 0) {
            return true
        }

        return false
    }

    override fun isAiAllowed(editorInfo: EditorInfo?): Boolean {
        // In Phase 1, AI is completely disabled.
        // Even in future phases, sensitive inputs unconditionally prohibit AI processing.
        if (isSensitiveInput(editorInfo)) {
            return false
        }
        // Phase 1: AI is not yet active
        return false
    }
}
