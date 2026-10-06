/*
 * Dot Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */
package dev.qarasky.dotisland.ui

import android.os.PowerManager
import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.floor

/** One monochrome dot row. Only the source progress advances the bright portion. */
@Composable
internal fun LiveUpdateProgressDots(progress: Float?, modifier: Modifier = Modifier,
    working: Boolean = progress == null || progress < 1f) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val pulse = remember { Animatable(0f) }
    LaunchedEffect(working, owner, context) {
        if (!working) {
            pulse.snapTo(0f)
            return@LaunchedEffect
        }
        owner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            try {
                while (true) {
                    val power = context.getSystemService(PowerManager::class.java)
                    val motion = Settings.Global.getFloat(context.contentResolver,
                        Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
                    if (power?.isInteractive == true && !power.isPowerSaveMode && motion) {
                        pulse.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
                        pulse.animateTo(0f, tween(700, easing = FastOutSlowInEasing))
                        delay(350)
                    } else {
                        pulse.snapTo(0f)
                        delay(1000)
                    }
                }
            } finally {
                withContext(NonCancellable) { pulse.snapTo(0f) }
            }
        }
    }
    Canvas(modifier.semantics {
        contentDescription = "Live Update progress"
        progressBarRangeInfo = progress?.let { ProgressBarRangeInfo(it.coerceIn(0f, 1f), 0f..1f) }
            ?: ProgressBarRangeInfo.Indeterminate
    }) {
        val radius = minOf(2.dp.toPx(), size.height / 2f)
        val count = (floor((size.width - radius * 2f) / 8.dp.toPx()).toInt() + 1).coerceAtLeast(2)
        val pitch = (size.width - radius * 2f).coerceAtLeast(0f) / (count - 1)
        repeat(count) { index ->
            drawCircle(Color.White, radius,
                Offset(radius + index * pitch, size.height / 2f),
                alpha = orderProgressDotOpacity(progress, index, count, pulse.value, working))
        }
    }
}

internal fun orderProgressDotOpacity(progress: Float?, index: Int, count: Int, pulse: Float,
    working: Boolean = progress == null || progress < 1f): Float {
    if (progress == null) return 0.24f + if (working) 0.28f * pulse.coerceIn(0f, 1f) else 0f
    val normalized = progress.coerceIn(0f, 1f)
    val position = normalized * count
    val fill = (position - index).coerceIn(0f, 1f)
    val base = 0.16f + 0.66f * fill
    val isFrontier = working && index == floor(position).toInt().coerceAtMost(count - 1)
    val target = if (normalized >= 1f) 0.4f else 0.9f
    return if (isFrontier) base + (target - base) * pulse.coerceIn(0f, 1f) else base
}
