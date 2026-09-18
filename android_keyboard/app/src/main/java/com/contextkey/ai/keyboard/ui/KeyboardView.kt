package com.contextkey.ai.keyboard.ui

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.contextkey.ai.keyboard.R
import com.contextkey.ai.keyboard.model.KeyboardLayoutProvider
import com.contextkey.ai.keyboard.model.KeyboardMode

/**
 * Root input view for the ContextKey keyboard.
 * Renders the header and dynamic key rows according to the active mode, shift state,
 * and current EditorInfo.
 */
class KeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val headerView: KeyboardHeaderView = KeyboardHeaderView(context)
    private val rowsContainer: LinearLayout = LinearLayout(context)

    private var controller: KeyboardController? = null
    private var currentEditorInfo: EditorInfo? = null

    init {
        orientation = VERTICAL
        setBackgroundColor(ContextCompat.getColor(context, R.color.kb_background))

        // Add Header
        val headerParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        addView(headerView, headerParams)

        // Rows container
        rowsContainer.orientation = VERTICAL
        rowsContainer.setPadding(dpToPx(4), dpToPx(3), dpToPx(4), dpToPx(6))
        val containerParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        addView(rowsContainer, containerParams)
    }

    fun attachController(keyboardController: KeyboardController) {
        this.controller = keyboardController
        render()
    }

    fun setEditorInfo(editorInfo: EditorInfo?) {
        this.currentEditorInfo = editorInfo
        render()
    }

    fun render() {
        val ctrl = controller ?: return
        rowsContainer.removeAllViews()

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
                orientation = HORIZONTAL
                gravity = Gravity.CENTER
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, rowHeight).apply {
                    setMargins(0, keyMarginVertical, 0, keyMarginVertical)
                }
            }

            for (keyItem in rowItems) {
                val keyView = KeyView(context).apply {
                    layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, keyItem.widthWeight).apply {
                        setMargins(keyMarginHorizontal, 0, keyMarginHorizontal, 0)
                    }
                }
                keyView.bind(
                    item = keyItem,
                    shiftState = ctrl.shiftState,
                    clickListener = { item -> ctrl.handleKeyClick(item) },
                    longClickListener = { item -> ctrl.handleKeyLongClick(item) }
                )
                rowLayout.addView(keyView)
            }

            rowsContainer.addView(rowLayout)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}
