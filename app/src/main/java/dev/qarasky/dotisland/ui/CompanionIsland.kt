/*
 * Dot Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */
package dev.qarasky.dotisland.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.*
import dev.qarasky.dotisland.data.DotIslandSettings
import dev.qarasky.dotisland.model.IslandNotification

/** Geometry reads stay in measurement/drawing; endpoint content is not duplicated. */
@Composable
internal fun CompanionIsland(notification: IslandNotification, pill: Boolean, visible: Boolean,
    expandedAlpha: () -> Float, settings: DotIslandSettings, modifier: Modifier,
    onSelect: () -> Unit, onHold: () -> Unit) {
    val width by animateDpAsState(if (pill) settings.width.dp else settings.height.dp,
        spring(dampingRatio = 1f, stiffness = 520f), label = "companionWidth")
    val radius by animateDpAsState(if (pill) settings.cornerRadius.dp else settings.height.dp / 2f,
        spring(dampingRatio = 1f, stiffness = 520f), label = "companionRadius")
    val opacity by animateFloatAsState(if (visible) 1f else 0f, tween(180), label = "companionAlpha")
    val scale by animateFloatAsState(if (visible) 1f else 0.3f,
        spring(dampingRatio = 1f, stiffness = 480f), label = "companionScale")
    val progress = {
        val difference = settings.width - settings.height
        if (difference == 0f) { if (pill) 1f else 0f }
        else ((width.value - settings.height) / difference).coerceIn(0f, 1f)
    }
    val shown by remember { derivedStateOf { opacity * (1f - expandedAlpha()) > 0.01f } }
    val circleShown by remember(settings, pill) { derivedStateOf { progress() < 0.999f } }
    val pillShown by remember(settings, pill) { derivedStateOf { progress() > 0.001f } }
    val shape = remember {
        object : Shape {
            override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline =
                RoundedCornerShape(radius).createOutline(size, layoutDirection, density)
        }
    }
    val configuration = LocalViewConfiguration.current
    val holdConfiguration = remember(configuration) {
        object : ViewConfiguration by configuration {
            override val longPressTimeoutMillis = HOLD_GESTURE_THRESHOLD_MS
        }
    }
    if (!shown) return
    CompositionLocalProvider(LocalViewConfiguration provides holdConfiguration) {
        Box(modifier.testTag("companionIsland-${notification.key}").layout { measurable, constraints ->
            val w = width.roundToPx().coerceIn(0, constraints.maxWidth)
            val h = settings.height.dp.roundToPx().coerceIn(0, constraints.maxHeight)
            val child = measurable.measure(Constraints.fixed(w, h))
            layout(w, h) { child.place(0, 0) }
        }.graphicsLayer {
            alpha = opacity * (1f - expandedAlpha())
            scaleX = scale
            scaleY = scale
            this.shape = shape
            clip = true
        }.background(Color.Black).border(ISLAND_BORDER_WIDTH, ISLAND_BORDER_COLOR, shape)
            .combinedClickable(interactionSource = remember { MutableInteractionSource() },
                indication = null, hapticFeedbackEnabled = false, onClick = onSelect, onLongClick = onHold),
            contentAlignment = Alignment.Center) {
            if (circleShown) Box(Modifier.fillMaxSize().graphicsLayer { alpha = 1f - progress() },
                contentAlignment = Alignment.Center) {
                ActivityGlyph(notification, Modifier.size(20.dp))
            }
            if (pillShown) Box(Modifier.requiredWidth(settings.width.dp).height(settings.height.dp)
                .graphicsLayer { alpha = progress() }) {
                IslandCollapsedContent(notification.mode, notification, 1f, settings)
            }
        }
    }
}
