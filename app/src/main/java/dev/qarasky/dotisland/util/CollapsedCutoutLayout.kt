/*
 * Dot Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */
package dev.qarasky.dotisland.util

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.hardware.display.DisplayManager
import android.view.Display
import androidx.compose.ui.geometry.Rect
import dev.qarasky.dotisland.data.DotIslandSettings
import dev.qarasky.dotisland.model.IslandMode
import dev.qarasky.dotisland.model.IslandNotification
import dev.qarasky.dotisland.ui.liveUpdateCompactStatus
import kotlin.math.ceil

/** Reserve the camera gap, extending only the side whose content needs room.
 * Coordinates and widths are in dp. Never persist these content-derived settings.
 */
internal fun fitCollapsedCutoutSides(
    settings: DotIslandSettings,
    screenWidthDp: Float,
    cameraBoundsDp: List<Rect>,
    leadingWidthDp: Float,
    trailingWidthDp: Float,
    clearanceDp: Float = 6f
): DotIslandSettings {
    val top = if (settings.enableNotchMode) 0f else settings.yOffset
    val cameras = cameraBoundsDp.filter { it.bottom > top && it.top < top + settings.height }
    if (cameras.isEmpty()) return settings
    val left = screenWidthDp / 2f + settings.xOffset - settings.width / 2f
    val right = left + settings.width
    val cameraLeft = cameras.minOf { it.left } - clearanceDp
    val cameraRight = cameras.maxOf { it.right } + clearanceDp
    val neededLeft = minOf(left, cameraLeft - 8f - leadingWidthDp)
    val neededRight = maxOf(right, cameraRight + trailingWidthDp + 12f)
    val growLeft = (left - neededLeft).coerceAtLeast(0f)
    val growRight = (neededRight - right).coerceAtLeast(0f)
    return settings.copy(
        width = settings.width + growLeft + growRight,
        xOffset = settings.xOffset + (growRight - growLeft) / 2f
    )
}

/** Same measured geometry for the drawn pill and its small touch window. */
internal fun collapsedCutoutSettings(
    context: Context,
    settings: DotIslandSettings,
    notification: IslandNotification?
): DotIslandSettings {
    if (notification?.mode != IslandMode.LiveActivity) return settings
    val metrics = context.resources.displayMetrics
    val display = context.getSystemService(DisplayManager::class.java)?.getDisplay(Display.DEFAULT_DISPLAY)
    val cameras = displayCameraBounds(display).map {
        Rect(it.left / metrics.density, it.top / metrics.density,
            it.right / metrics.density, it.bottom / metrics.density)
    }
    val paint = Paint().apply {
        typeface = Typeface.MONOSPACE
        textSize = 9f * metrics.scaledDensity
    }
    val labelWidth = ceil(paint.measureText(liveUpdateCompactStatus(notification))) / metrics.density
    return fitCollapsedCutoutSides(settings, metrics.widthPixels / metrics.density,
        cameras, leadingWidthDp = 22f, trailingWidthDp = labelWidth)
}
