package com.contextkey.ai.keyboard.ui

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.contextkey.ai.keyboard.R

/**
 * Top accessory bar of the keyboard containing the ContextKey AI branding
 * on the left and a dedicated Close / Dismiss Keyboard button on the right.
 */
class KeyboardHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val brandText: TextView = TextView(context)
    private val aiPillLayout: LinearLayout = LinearLayout(context)
    private val closeButton: FrameLayout = FrameLayout(context)

    var onCloseClickListener: (() -> Unit)? = null

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setBackgroundColor(ContextCompat.getColor(context, R.color.kb_header_bg))
        setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4))

        // Left Container: Brand + AI Pill
        val leftContainer = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f)
        }

        // Brand Label
        brandText.text = "ContextKey"
        brandText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        brandText.setTextColor(ContextCompat.getColor(context, R.color.kb_text_secondary))
        val brandParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            marginEnd = dpToPx(8)
        }
        leftContainer.addView(brandText, brandParams)

        // AI Placeholder Pill Container
        aiPillLayout.orientation = HORIZONTAL
        aiPillLayout.gravity = Gravity.CENTER
        aiPillLayout.background = ContextCompat.getDrawable(context, R.drawable.bg_ai_pill)
        aiPillLayout.isClickable = true
        aiPillLayout.isFocusable = false

        val sparkleIcon = ImageView(context).apply {
            setImageResource(R.drawable.ic_ai_sparkle)
            setColorFilter(ContextCompat.getColor(context, R.color.kb_ai_pill_text))
        }
        val iconSize = dpToPx(13)
        val iconParams = LayoutParams(iconSize, iconSize).apply {
            marginEnd = dpToPx(4)
        }
        aiPillLayout.addView(sparkleIcon, iconParams)

        val aiText = TextView(context).apply {
            text = context.getString(R.string.ai_placeholder_label)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            setTextColor(ContextCompat.getColor(context, R.color.kb_ai_pill_text))
        }
        aiPillLayout.addView(aiText)

        aiPillLayout.setOnClickListener {
            aiPillLayout.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            Toast.makeText(context, context.getString(R.string.ai_pill_tooltip), Toast.LENGTH_SHORT).show()
        }

        leftContainer.addView(aiPillLayout)
        addView(leftContainer)

        // Right Container: Close / Dismiss Keyboard Button
        closeButton.apply {
            val btnSize = dpToPx(32)
            layoutParams = LayoutParams(btnSize, btnSize)
            isClickable = true
            isFocusable = false
            background = ContextCompat.getDrawable(context, R.drawable.bg_key_special)

            val downIcon = ImageView(context).apply {
                setImageResource(R.drawable.ic_keyboard_down)
                setColorFilter(ContextCompat.getColor(context, R.color.kb_icon_tint))
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            }
            addView(downIcon)

            setOnClickListener {
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onCloseClickListener?.invoke()
            }
        }
        addView(closeButton)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}
