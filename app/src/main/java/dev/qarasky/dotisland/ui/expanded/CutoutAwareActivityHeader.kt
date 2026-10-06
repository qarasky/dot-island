/*
 * Dot Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */
package dev.qarasky.dotisland.ui.expanded

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.dp
import dev.qarasky.dotisland.util.calculateCutoutAvoidingTop
import kotlin.math.ceil

/** Three children: leading glyph/artwork, text, trailing status (or zero-size spacer).
 * Position side artwork normally; move only elements intersecting the camera.
 */
@Composable
internal fun CutoutAwareActivityHeader(
    modifier: Modifier,
    cameraBounds: List<Rect>,
    screenWidthPx: Int,
    screenTop: Dp,
    horizontalGap: Dp = 18.dp,
    cameraClearance: Dp = 0.dp,
    alignLeadingWithText: Boolean = false,
    alignTrailingWithText: Boolean = false,
    content: @Composable () -> Unit
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val gap = horizontalGap.roundToPx()
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val art = measurables[0].measure(loose)
        val status = measurables[2].measure(loose)
        val textWidth = (constraints.maxWidth - art.width - status.width - gap * 2).coerceAtLeast(0)
        val text = measurables[1].measure(loose.copy(minWidth = textWidth, maxWidth = textWidth))
        val naturalHeight = maxOf(art.height, text.height, status.height)
        val headerScreenLeft = (screenWidthPx - constraints.maxWidth) / 2f
        val headerScreenTop = screenTop.toPx()
        val textX = art.width + gap
        val statusX = constraints.maxWidth - status.width
        fun topFor(x: Int, width: Int, height: Int, relativeTop: Float = (naturalHeight - height) / 2f): Int {
            val top = headerScreenTop + relativeTop
            return ceil(calculateCutoutAvoidingTop(
                headerScreenLeft + x, width.toFloat(), top, height.toFloat(), cameraBounds,
                clearancePx = maxOf(1f, cameraClearance.toPx())
            ) - headerScreenTop).toInt().coerceAtLeast(0)
        }
        val textTop = topFor(textX, text.width, text.height)
        val artTop = if (alignLeadingWithText) {
            topFor(0, art.width, art.height, textTop + (text.height - art.height) / 2f)
        } else topFor(0, art.width, art.height)
        val statusTop = if (alignTrailingWithText) {
            topFor(statusX, status.width, status.height, textTop + (text.height - status.height) / 2f)
        } else topFor(statusX, status.width, status.height)
        val height = maxOf(artTop + art.height, textTop + text.height, statusTop + status.height)
        layout(constraints.maxWidth, constraints.constrainHeight(height)) {
            art.place(0, artTop)
            text.place(textX, textTop)
            status.place(statusX, statusTop)
        }
    }
}
