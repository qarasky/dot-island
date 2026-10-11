/*
 * Dot Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */
package dev.qarasky.dotisland.ui.expanded

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.qarasky.dotisland.data.DotIslandSettings
import dev.qarasky.dotisland.model.IslandMode
import dev.qarasky.dotisland.model.IslandNotification
import dev.qarasky.dotisland.ui.presentationFor
import kotlinx.coroutines.flow.drop

@Composable
internal fun IslandExpandedContent(notifications: List<IslandNotification>, selectedIndex: Int,
    onPageSelected: (Int) -> Unit, onOpenNotification: (IslandNotification) -> Unit,
    onCollapse: () -> Unit, statusBarHeight: Dp,
    settings: DotIslandSettings, cameraBounds: List<Rect>, cardScreenTop: Dp,
    pager: PagerState, measurements: ExpandedContentMeasurements,
    environment: ExpandedLayoutEnvironment, bodyHeight: () -> Dp,
    animateSelection: Boolean,
    modifier: Modifier = Modifier, onReplyStateChanged: (Boolean) -> Unit = {}) {
    if (notifications.isEmpty()) return
    val density = LocalDensity.current
    LaunchedEffect(selectedIndex, animateSelection) {
        if (selectedIndex in notifications.indices && !pager.isScrollInProgress && pager.currentPage != selectedIndex) {
            if (animateSelection) pager.animateScrollToPage(selectedIndex)
            else pager.scrollToPage(selectedIndex)
        }
    }
    val currentOnPageSelected by rememberUpdatedState(onPageSelected)
    val currentNotifications by rememberUpdatedState(notifications)
    LaunchedEffect(pager) {
        snapshotFlow { pager.settledPage }.drop(1).collect { page ->
            if (page in currentNotifications.indices) currentOnPageSelected(page)
        }
    }
    HorizontalPager(pager, modifier.fillMaxWidth().layout { measurable, constraints ->
        val h = bodyHeight().roundToPx().coerceIn(constraints.minHeight, constraints.maxHeight)
        val child = measurable.measure(constraints.copy(minHeight = h, maxHeight = h))
        layout(child.width, h) { child.place(0, 0) }
    }, key = { notifications[it].key }) { page ->
        notifications.getOrNull(page)?.let { item ->
            Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .clickable(remember { MutableInteractionSource() }, indication = null) { onOpenNotification(item) }) {
                val layoutKey = expandedLayoutKey(item, environment)
                key(layoutKey) {
                Box(Modifier.fillMaxWidth().wrapContentHeight().onSizeChanged {
                    val measured = with(density) { it.height.toDp() }
                    measurements.record(layoutKey, presentationFor(item.mode).clampHeight(measured))
                }) {
                    when (item.mode) {
                        IslandMode.Music -> MusicExpanded(item, settings, onCollapse, cameraBounds, cardScreenTop)
                        IslandMode.IncomingCall -> IncomingCallExpanded(item, 20.dp, onCollapse, settings)
                        IslandMode.LiveActivity -> LiveUpdateExpanded(item, cameraBounds, cardScreenTop)
                        IslandMode.Published -> PublishedExpanded(item, 20.dp)
                        IslandMode.Empty -> Unit
                    }
                }
                }
            }
        }
    }
}
