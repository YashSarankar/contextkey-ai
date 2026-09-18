package com.contextkey.ai.keyboard.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.contextkey.ai.keyboard.R
import com.contextkey.ai.keyboard.model.KeyAction
import com.contextkey.ai.keyboard.model.KeyItem
import com.contextkey.ai.keyboard.model.ShiftState

/**
 * Visual key widget rendering text label or icon with touch states and haptics.
 */
class KeyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val textView: TextView = TextView(context)
    private val imageView: ImageView = ImageView(context)

    var keyItem: KeyItem? = null
        private set

    private var onKeyClickListener: ((KeyItem) -> Unit)? = null

    init {
        isClickable = true
        isFocusable = false

        // Configure text view
        textView.gravity = Gravity.CENTER
        textView.setTextColor(ContextCompat.getColor(context, R.color.kb_text_primary))
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19f)
        val textParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        addView(textView, textParams)

        // Configure image view
        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        imageView.setColorFilter(ContextCompat.getColor(context, R.color.kb_icon_tint))
        val imageParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        addView(imageView, imageParams)
    }

    fun bind(
        item: KeyItem,
        shiftState: ShiftState,
        listener: (KeyItem) -> Unit
    ) {
        this.keyItem = item
        this.onKeyClickListener = listener

        // Set background styling based on key type
        val bgRes = when {
            item.isAction -> R.drawable.bg_key_action
            item.isSpecial -> R.drawable.bg_key_special
            else -> R.drawable.bg_key_normal
        }
        background = ContextCompat.getDrawable(context, bgRes)

        // Render Icon or Text
        if (item.action is KeyAction.Shift) {
            textView.visibility = GONE
            imageView.visibility = VISIBLE
            val shiftIcon = when (shiftState) {
                ShiftState.OFF -> R.drawable.ic_shift
                ShiftState.SHIFTED -> R.drawable.ic_shift_active
                ShiftState.CAPS_LOCKED -> R.drawable.ic_shift_caps
            }
            imageView.setImageResource(shiftIcon)
            if (shiftState != ShiftState.OFF) {
                imageView.setColorFilter(ContextCompat.getColor(context, R.color.kb_icon_active_tint))
            } else {
                imageView.setColorFilter(ContextCompat.getColor(context, R.color.kb_icon_tint))
            }
        } else if (item.iconResId != null) {
            textView.visibility = GONE
            imageView.visibility = VISIBLE
            imageView.setImageResource(item.iconResId)
            imageView.setColorFilter(ContextCompat.getColor(context, R.color.kb_icon_tint))
        } else {
            imageView.visibility = GONE
            textView.visibility = VISIBLE

            val label = when (val action = item.action) {
                is KeyAction.Character -> {
                    if (shiftState.isShiftedOrCaps()) action.shifted else action.normal
                }
                else -> item.primaryLabel ?: ""
            }

            textView.text = label
            if (item.action is KeyAction.Space) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                textView.setTextColor(ContextCompat.getColor(context, R.color.kb_text_secondary))
            } else if (item.isSpecial) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                textView.setTextColor(ContextCompat.getColor(context, R.color.kb_text_primary))
            } else {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19f)
                textView.setTextColor(ContextCompat.getColor(context, R.color.kb_text_primary))
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isPressed = true
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (isPressed) {
                    isPressed = false
                    keyItem?.let { onKeyClickListener?.invoke(it) }
                }
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                isPressed = false
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
