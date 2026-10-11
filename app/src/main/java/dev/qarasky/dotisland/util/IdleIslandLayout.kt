/*
 * Dot Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */
package dev.qarasky.dotisland.util

import android.content.Context
import android.hardware.display.DisplayManager
import android.view.Display
import androidx.compose.ui.geometry.Rect
import dev.qarasky.dotisland.data.DotIslandSettings

/** Empty idle surface hugs the actual camera path, not the status-bar exclusion. */
internal fun fitIdleIslandAroundCamera(
    settings: DotIslandSettings,
    screenWidthDp: Float,
    camerasDp: List<Rect>,
    clearanceDp: Float = 2f
): DotIslandSettings {
    val camera = camerasDp.filter { !it.isEmpty && it.top < 100f }
        .minByOrNull { kotlin.math.abs(it.center.x - screenWidthDp / 2f) }
    val width = camera?.let { it.width + clearanceDp * 2f } ?: 29f
    val height = camera?.let { it.height + clearanceDp * 2f } ?: 29f
    return settings.copy(
        width = width,
        height = height,
        xOffset = camera?.let { it.center.x - screenWidthDp / 2f } ?: settings.xOffset,
        yOffset = camera?.let { (it.top - clearanceDp).coerceAtLeast(0f) }
            ?: (settings.yOffset + (settings.height - height) / 2f),
        cornerRadius = minOf(width, height) / 2f
    )
}

internal fun idleIslandSettings(context: Context, settings: DotIslandSettings): DotIslandSettings {
    val metrics = context.resources.displayMetrics
    val display = context.getSystemService(DisplayManager::class.java)?.getDisplay(Display.DEFAULT_DISPLAY)
    val cameras = displayCameraBounds(display).map {
        Rect(it.left / metrics.density, it.top / metrics.density,
            it.right / metrics.density, it.bottom / metrics.density)
    }
    return fitIdleIslandAroundCamera(settings, metrics.widthPixels / metrics.density, cameras)
}
