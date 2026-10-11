/*
 * Dot Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */
package dev.qarasky.dotisland.ui.expanded

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Dp
import dev.qarasky.dotisland.model.IslandNotification
import dev.qarasky.dotisland.model.IslandMode

internal data class ExpandedLayoutEnvironment(val width: Dp, val density: Float,
    val fontScale: Float, val cameras: List<Rect>, val screenTop: Dp)

/** Position/playing/progress updates do not invalidate unchanged text geometry. */
internal data class ExpandedLayoutKey(val source: String, val mode: IslandMode,
    val appName: String, val title: String, val text: String, val hasProgress: Boolean,
    val ringing: Boolean, val environment: ExpandedLayoutEnvironment)

internal fun expandedLayoutKey(item: IslandNotification, environment: ExpandedLayoutEnvironment) =
    ExpandedLayoutKey(item.key, item.mode, item.appName, item.title, item.text,
        item.progressIndeterminate || item.progressMax > 0, item.isCallRinging, environment)

/** Owned by the overlay, not the disposable expanded subtree. At most one entry per source. */
internal class ExpandedContentMeasurements {
    val heights = mutableStateMapOf<ExpandedLayoutKey, Dp>()
    fun record(key: ExpandedLayoutKey, height: Dp) {
        heights.keys.filter { it.source == key.source && it != key }.forEach(heights::remove)
        if (heights[key] != height) heights[key] = height
    }
    fun retainSources(sources: Set<String>) {
        heights.keys.filter { it.source !in sources }.forEach(heights::remove)
    }
}

internal fun interpolatePageHeight(current: Dp, next: Dp, fraction: Float): Dp =
    current + (next - current) * kotlin.math.abs(fraction).coerceIn(0f, 1f)
