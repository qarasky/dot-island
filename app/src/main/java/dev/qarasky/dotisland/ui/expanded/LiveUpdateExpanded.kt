/*
 * Dot Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */
package dev.qarasky.dotisland.ui.expanded

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.qarasky.dotisland.model.IslandNotification
import dev.qarasky.dotisland.ui.ActivityGlyph
import dev.qarasky.dotisland.ui.LiveUpdateProgressDots
import dev.qarasky.dotisland.ui.liveUpdateIsWorking
import dev.qarasky.dotisland.util.LiveActivityParser

@Composable
fun LiveUpdateExpanded(notification: IslandNotification,
    cameraBounds: List<Rect> = emptyList(), cardScreenTop: Dp = 12.dp) {
    val context = LocalContext.current
    val eta = LiveActivityParser.eta(notification.title, notification.text)
    Column(Modifier.fillMaxWidth().padding(LIVE_UPDATE_CONTENT_INSET),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        CutoutAwareActivityHeader(Modifier.fillMaxWidth(), cameraBounds,
            context.resources.displayMetrics.widthPixels, cardScreenTop + LIVE_UPDATE_CONTENT_INSET,
            horizontalGap = 12.dp, cameraClearance = 8.dp,
            alignLeadingWithText = true, alignTrailingWithText = true) {
            ActivityGlyph(notification, Modifier.size(32.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(notification.appName.ifBlank { "Live Update" }, fontSize = 13.sp, color = Color(0xFF8E8E93),
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(notification.title.ifBlank { "Live Update" }, color = Color.White,
                    fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            if (eta != null) {
                Text(eta, color = Color.White, fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp, fontWeight = FontWeight.Medium)
            } else Spacer(Modifier.size(0.dp))
        }
        // Keep additional source details, but don't repeat an ETA-only sentence.
        LiveActivityParser.detailText(notification.title, notification.text)?.let { detail ->
            Text(detail, fontSize = 14.sp, lineHeight = 19.sp,
                color = Color(0xFFB8B8BD), maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        if (notification.progressIndeterminate) {
            LiveUpdateProgressDots(null, Modifier.fillMaxWidth().height(4.dp), working = liveUpdateIsWorking(notification))
        } else LiveActivityParser.progress(notification.progress, notification.progressMax)?.let { progress ->
            LiveUpdateProgressDots(progress, Modifier.fillMaxWidth().height(4.dp),
                working = liveUpdateIsWorking(notification))
        }
    }
}

private val LIVE_UPDATE_CONTENT_INSET = 20.dp
