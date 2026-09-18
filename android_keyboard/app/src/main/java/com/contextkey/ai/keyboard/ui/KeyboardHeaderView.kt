package com.contextkey.ai.keyboard.ui

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.contextkey.ai.keyboard.R

/**
 * Top accessory bar of the keyboard containing the ContextKey brand indicator
 * and a non-functional placeholder [ AI ] pill button for Phase 1.
 */
class KeyboardHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val brandText: TextView = TextView(context)
    private val aiPillLayout: LinearLayout = LinearLayout(context)

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setBackgroundColor(ContextCompat.getColor(context, R.color.kb_header_bg))
        setPadding(dpToPx(12), dpToPx(6), dpToPx(12), dpToPx(6))

        // Brand Label
        brandText.text = "ContextKey"
        brandText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        brandText.setTextColor(ContextCompat.getColor(context, R.color.kb_text_secondary))
        val brandParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f)
        addView(brandText, brandParams)

        // AI Placeholder Pill Container
        aiPillLayout.orientation = HORIZONTAL
        aiPillLayout.gravity = Gravity.CENTER
        aiPillLayout.background = ContextCompat.getDrawable(context, R.drawable.bg_ai_pill)
        aiPillLayout.isClickable = true
        aiPillLayout.isFocusable = false

        val sparkleIcon = ImageView(context)
        sparkleIcon.setImageResource(R.drawable.ic_ai_sparkle)
        sparkleIcon.setColorFilter(ContextCompat.getColor(context, R.color.kb_ai_pill_text))
        val iconSize = dpToPx(14)
        val iconParams = LayoutParams(iconSize, iconSize).apply {
            marginEnd = dpToPx(4)
        }
        aiPillLayout.addView(sparkleIcon, iconParams)

        val aiText = TextView(context)
        aiText.text = context.getString(R.string.ai_placeholder_label)
        aiText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        aiText.setTextColor(ContextCompat.getColor(context, R.color.kb_ai_pill_text))
        aiPillLayout.addView(aiText)

        // Tap listener for placeholder pill: informative feedback only, no network/processing
        aiPillLayout.setOnClickListener {
            aiPillLayout.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            Toast.makeText(context, context.getString(R.string.ai_pill_tooltip), Toast.LENGTH_SHORT).show()
        }

        addView(aiPillLayout)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}
