package com.contextkey.ai.keyboard.ui

import android.content.Context
import android.content.res.Configuration
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.contextkey.ai.keyboard.R
import com.contextkey.ai.keyboard.model.KeyboardLayoutProvider
import com.contextkey.ai.keyboard.model.KeyboardMode

/**
 * Root input view for the ContextKey keyboard.
 * Features ultra-low latency rendering, Material You key elevation,
 * Gboard-style key press popups, spacebar cursor glide, and responsive navigation bar insets.
 */
class KeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val rootLayout: LinearLayout = LinearLayout(context)
    private val headerView: KeyboardHeaderView = KeyboardHeaderView(context)
    private val rowsContainer: LinearLayout = LinearLayout(context)
    private val keyPreviewPopup: TextView = TextView(context)

    private val activeKeyViews = mutableListOf<KeyView>()

    private var controller: KeyboardController? = null
    private var currentEditorInfo: EditorInfo? = null
    var onCloseKeyboardRequested: (() -> Unit)? = null

    init {
        setBackgroundColor(ContextCompat.getColor(context, R.color.kb_background))

        rootLayout.orientation = LinearLayout.VERTICAL
        addView(rootLayout, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))

        // Connect Header Close Click
        headerView.onCloseClickListener = {
            onCloseKeyboardRequested?.invoke()
        }

        // Add Header
        val headerParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        rootLayout.addView(headerView, headerParams)

        // Rows container
        rowsContainer.orientation = LinearLayout.VERTICAL
        rowsContainer.setPadding(dpToPx(4), dpToPx(3), dpToPx(4), dpToPx(4))
        val containerParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        rootLayout.addView(rowsContainer, containerParams)

        // Key Press Preview Popup
        keyPreviewPopup.apply {
            visibility = GONE
            gravity = Gravity.CENTER
            setBackgroundResource(R.drawable.bg_key_preview)
            setTextColor(ContextCompat.getColor(context, R.color.kb_text_primary))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            elevation = dpToPx(8).toFloat()
        }
        addView(keyPreviewPopup, LayoutParams(dpToPx(52), dpToPx(56)))

        // Handle navigation bar insets
        ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
            val navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val bottomPadding = if (navInsets.bottom > 0) navInsets.bottom else dpToPx(6)
            view.setPadding(navInsets.left, 0, navInsets.right, bottomPadding)
            insets
        }
    }

    fun attachController(keyboardController: KeyboardController) {
        this.controller = keyboardController
        render()
    }

    fun setEditorInfo(editorInfo: EditorInfo?) {
        this.currentEditorInfo = editorInfo
        render()
    }

    fun updateShiftStateOnly() {
        val ctrl = controller ?: return
        for (keyView in activeKeyViews) {
            keyView.updateDisplay(ctrl.shiftState)
        }
    }

    fun render() {
        val ctrl = controller ?: return
        rowsContainer.removeAllViews()
        activeKeyViews.clear()

        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val rowHeight = if (isLandscape) dpToPx(38) else dpToPx(50)
        val keyMarginHorizontal = dpToPx(3)
        val keyMarginVertical = dpToPx(3)

        val rows = when (ctrl.keyboardMode) {
            KeyboardMode.QWERTY -> KeyboardLayoutProvider.getQwertyRows(currentEditorInfo)
            KeyboardMode.NUMERIC_SYMBOLS -> KeyboardLayoutProvider.getNumericSymbolsRows(currentEditorInfo)
            KeyboardMode.MORE_SYMBOLS -> KeyboardLayoutProvider.getMoreSymbolsRows(currentEditorInfo)
        }

        for (rowItems in rows) {
            val rowLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, rowHeight).apply {
                    setMargins(0, keyMarginVertical, 0, keyMarginVertical)
                }
            }

            for (keyItem in rowItems) {
                val keyView = KeyView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, keyItem.widthWeight).apply {
                        setMargins(keyMarginHorizontal, 0, keyMarginHorizontal, 0)
                    }
                }
                keyView.bind(
                    item = keyItem,
                    shiftState = ctrl.shiftState,
                    clickListener = { item -> ctrl.handleKeyClick(item) },
                    longClickListener = { item -> ctrl.handleKeyLongClick(item) },
                    swipeListener = { offset -> ctrl.handleSpaceGlide(offset) },
                    touchStateListener = { view, isPressed, label ->
                        showKeyPreview(view, isPressed, label)
                    }
                )
                activeKeyViews.add(keyView)
                rowLayout.addView(keyView)
            }

            rowsContainer.addView(rowLayout)
        }
    }

    private fun showKeyPreview(keyView: KeyView, isPressed: Boolean, label: String?) {
        if (!isPressed || label.isNullOrEmpty() || label.length > 2) {
            keyPreviewPopup.visibility = GONE
            return
        }

        val location = IntArray(2)
        keyView.getLocationInWindow(location)

        val rootLocation = IntArray(2)
        this.getLocationInWindow(rootLocation)

        val relativeX = location[0] - rootLocation[0]
        val relativeY = location[1] - rootLocation[1]

        val popupWidth = dpToPx(52)
        val popupHeight = dpToPx(58)

        val popupX = relativeX + (keyView.width - popupWidth) / 2
        val popupY = relativeY - popupHeight + dpToPx(4)

        keyPreviewPopup.text = label
        keyPreviewPopup.layoutParams = (keyPreviewPopup.layoutParams as LayoutParams).apply {
            width = popupWidth
            height = popupHeight
            leftMargin = popupX
            topMargin = popupY
        }
        keyPreviewPopup.visibility = VISIBLE
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}
