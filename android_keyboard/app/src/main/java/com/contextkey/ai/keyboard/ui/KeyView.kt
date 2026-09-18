package com.contextkey.ai.keyboard.ui

import android.content.Context
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
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
 * High-performance, zero-latency key widget rendering primary labels,
 * secondary hints, and action icons. Supports instant action on touch down,
 * long-press repeating backspace, and spacebar cursor navigation.
 */
class KeyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val textView: TextView = TextView(context)
    private val secondaryTextView: TextView = TextView(context)
    private val imageView: ImageView = ImageView(context)

    var keyItem: KeyItem? = null
        private set

    private var onKeyClickListener: ((KeyItem) -> Unit)? = null
    private var onKeyLongPressListener: ((KeyItem) -> Unit)? = null
    private var onSwipeMoveListener: ((Int) -> Unit)? = null
    private var onTouchStateChangedListener: ((KeyView, Boolean, String?) -> Unit)? = null

    private val repeatHandler = Handler(Looper.getMainLooper())
    private var isRepeating = false
    private var longPressTriggered = false
    private var downX = 0f
    private var lastGlideX = 0f

    private val repeatRunnable = object : Runnable {
        override fun run() {
            if (isPressed) {
                isRepeating = true
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                keyItem?.let { onKeyClickListener?.invoke(it) }
                repeatHandler.postDelayed(this, REPEAT_INTERVAL_MS)
            }
        }
    }

    private val longPressRunnable = Runnable {
        if (isPressed && !isRepeating) {
            val item = keyItem ?: return@Runnable
            val charAction = item.action as? KeyAction.Character
            if (charAction?.longPressText != null) {
                longPressTriggered = true
                performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                onKeyLongPressListener?.invoke(item)
                onTouchStateChangedListener?.invoke(this@KeyView, false, null)
            }
        }
    }

    init {
        isClickable = true
        isFocusable = false

        // Primary text
        textView.gravity = Gravity.CENTER
        textView.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        textView.setTextColor(ContextCompat.getColor(context, R.color.kb_text_primary))
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        addView(textView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        // Secondary hint
        secondaryTextView.gravity = Gravity.TOP or Gravity.END
        secondaryTextView.setTextColor(ContextCompat.getColor(context, R.color.kb_text_secondary))
        secondaryTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9f)
        val secParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.TOP or Gravity.END
            setMargins(0, dpToPx(3), dpToPx(5), 0)
        }
        addView(secondaryTextView, secParams)

        // Icon
        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        imageView.setColorFilter(ContextCompat.getColor(context, R.color.kb_icon_tint))
        addView(imageView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    fun bind(
        item: KeyItem,
        shiftState: ShiftState,
        clickListener: (KeyItem) -> Unit,
        longClickListener: ((KeyItem) -> Unit)? = null,
        swipeListener: ((Int) -> Unit)? = null,
        touchStateListener: ((KeyView, Boolean, String?) -> Unit)? = null
    ) {
        this.keyItem = item
        this.onKeyClickListener = clickListener
        this.onKeyLongPressListener = longClickListener
        this.onSwipeMoveListener = swipeListener
        this.onTouchStateChangedListener = touchStateListener

        // Background styling
        val bgRes = when {
            item.isAction -> R.drawable.bg_key_action
            item.isSpecial -> R.drawable.bg_key_special
            else -> R.drawable.bg_key_normal
        }
        background = ContextCompat.getDrawable(context, bgRes)

        updateDisplay(shiftState)
    }

    fun updateDisplay(shiftState: ShiftState) {
        val item = keyItem ?: return

        if (item.action is KeyAction.Shift) {
            textView.visibility = GONE
            secondaryTextView.visibility = GONE
            imageView.visibility = VISIBLE

            val shiftIcon = when (shiftState) {
                ShiftState.OFF -> R.drawable.ic_shift
                ShiftState.SHIFTED -> R.drawable.ic_shift_active
                ShiftState.CAPS_LOCKED -> R.drawable.ic_shift_caps
            }
            imageView.setImageResource(shiftIcon)
            val tint = if (shiftState != ShiftState.OFF) R.color.kb_icon_active_tint else R.color.kb_icon_tint
            imageView.setColorFilter(ContextCompat.getColor(context, tint))
        } else if (item.iconResId != null) {
            textView.visibility = GONE
            secondaryTextView.visibility = GONE
            imageView.visibility = VISIBLE
            imageView.setImageResource(item.iconResId)

            val tint = if (item.isAction) R.color.kb_key_action_icon else R.color.kb_icon_tint
            imageView.setColorFilter(ContextCompat.getColor(context, tint))
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
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                textView.setTextColor(ContextCompat.getColor(context, R.color.kb_text_secondary))
            } else if (item.isSpecial) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                textView.setTextColor(ContextCompat.getColor(context, R.color.kb_text_primary))
            } else {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                textView.setTextColor(ContextCompat.getColor(context, R.color.kb_text_primary))
            }

            if (!item.secondaryLabel.isNullOrEmpty() && shiftState == ShiftState.OFF) {
                secondaryTextView.visibility = VISIBLE
                secondaryTextView.text = item.secondaryLabel
            } else {
                secondaryTextView.visibility = GONE
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val item = keyItem ?: return super.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isPressed = true
                longPressTriggered = false
                isRepeating = false
                downX = event.rawX
                lastGlideX = event.rawX
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)

                // Trigger key preview popup for letters
                val previewText = when (val act = item.action) {
                    is KeyAction.Character -> textView.text.toString()
                    else -> null
                }
                onTouchStateChangedListener?.invoke(this, true, previewText)

                // Instant input on touch down for zero-latency typing
                if (item.action !is KeyAction.Space && item.action !is KeyAction.Backspace) {
                    onKeyClickListener?.invoke(item)
                }

                if (item.action is KeyAction.Backspace) {
                    onKeyClickListener?.invoke(item)
                    repeatHandler.postDelayed(repeatRunnable, REPEAT_INITIAL_DELAY_MS)
                } else if ((item.action as? KeyAction.Character)?.longPressText != null) {
                    repeatHandler.postDelayed(longPressRunnable, LONG_PRESS_DELAY_MS)
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (item.action is KeyAction.Space) {
                    val delta = event.rawX - lastGlideX
                    val step = dpToPx(16)
                    if (kotlin.math.abs(delta) >= step) {
                        val offset = if (delta > 0) 1 else -1
                        onSwipeMoveListener?.invoke(offset)
                        lastGlideX = event.rawX
                    }
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                repeatHandler.removeCallbacks(repeatRunnable)
                repeatHandler.removeCallbacks(longPressRunnable)
                onTouchStateChangedListener?.invoke(this, false, null)

                if (isPressed) {
                    isPressed = false
                    if (item.action is KeyAction.Space && !isRepeating && !longPressTriggered) {
                        val totalSwipeDistance = kotlin.math.abs(event.rawX - downX)
                        if (totalSwipeDistance < dpToPx(16)) {
                            onKeyClickListener?.invoke(item)
                        }
                    }
                }
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                repeatHandler.removeCallbacks(repeatRunnable)
                repeatHandler.removeCallbacks(longPressRunnable)
                onTouchStateChangedListener?.invoke(this, false, null)
                isPressed = false
                isRepeating = false
                longPressTriggered = false
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    companion object {
        private const val REPEAT_INITIAL_DELAY_MS = 360L
        private const val REPEAT_INTERVAL_MS = 45L
        private const val LONG_PRESS_DELAY_MS = 380L
    }
}
