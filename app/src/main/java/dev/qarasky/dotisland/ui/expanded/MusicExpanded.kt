/*
 * Dot Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package dev.qarasky.dotisland.ui.expanded

import android.content.Context
import android.content.Intent
import android.media.MediaRouter2
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.qarasky.dotisland.data.DotIslandCommand
import dev.qarasky.dotisland.data.DotIslandSettings
import dev.qarasky.dotisland.di.DotIslandRepositories
import dev.qarasky.dotisland.model.IslandNotification
import dev.qarasky.dotisland.ui.WavyMusicSeekBar
import dev.qarasky.dotisland.ui.bounceClick
import dev.qarasky.dotisland.ui.SquircleShape
import dev.qarasky.dotisland.ui.DotMatrixActivity
import androidx.compose.ui.text.font.FontFamily
import dev.qarasky.dotisland.util.runCatchingLogged

/** Black, compact media surface; camera padding is owned by the expanded container. */
@Composable
fun MusicExpanded(
    notification: IslandNotification?,
    settings: DotIslandSettings = DotIslandSettings.Default,
    onCollapse: () -> Unit = {},
    cameraBounds: List<Rect> = emptyList(),
    cardScreenTop: Dp = 12.dp
) {
    val context = LocalContext.current
    val repository = remember(context) { DotIslandRepositories.notificationRepository(context) }
    var isPlaying by remember(notification?.key, notification?.mediaIsPlaying) {
        mutableStateOf(notification?.mediaIsPlaying == true)
    }
    // The listener owns the controller/callback and publishes one shared snapshot.
    // Opening this card must not synchronously query Binder or register a second owner.
    val artwork = notification?.largeIcon ?: notification?.icon
    val title = notification?.title?.takeIf { it.isNotBlank() } ?: "Song"
    val artist = notification?.text?.takeIf { it.isNotBlank() } ?: notification?.appName ?: "Artist"
    val durationMs = notification?.mediaDurationMs?.takeIf { it > 0 }

    Column(
        modifier = Modifier.fillMaxWidth()
            // No full-card artwork tint: the reference surface is solid black.
            .background(Color.Black)
            .padding(MUSIC_CONTENT_INSET)
    ) {
        CutoutAwareActivityHeader(
            modifier = Modifier.fillMaxWidth(),
            cameraBounds = cameraBounds,
            screenWidthPx = context.resources.displayMetrics.widthPixels,
            screenTop = cardScreenTop + MUSIC_CONTENT_INSET
        ) {
            if (artwork != null) {
                Image(
                    bitmap = artwork.asImageBitmap(), contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(64.dp).clip(SquircleShape)
                )
            } else {
                Icon(
                    Icons.Rounded.MusicNote, contentDescription = null, tint = Color.White,
                    modifier = Modifier.size(64.dp).clip(SquircleShape)
                        .background(Color(0xFF242425)).padding(16.dp)
                )
            }
            Column {
                Text(title, color = Color.White, fontSize = 17.sp, lineHeight = 22.sp,
                    fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(artist, color = MEDIA_SECONDARY, fontSize = 16.sp, lineHeight = 21.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            DotMatrixActivity(isPlaying, Modifier.size(width = 26.dp, height = 32.dp))
        }
        Spacer(Modifier.height(4.dp))
        // Polling is isolated here; playback progress does not recompose artwork/controls.
        MusicTimeline(notification, isPlaying, durationMs) { position ->
            repository.resetTimer()
            notification?.packageName?.let { repository.sendCommand(DotIslandCommand.SeekTo(it, position)) }
        }
        Box(Modifier.fillMaxWidth().height(48.dp)) {
            Row(
                modifier = Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MediaButton(Icons.Rounded.FastRewind, "Previous track", 36.dp) {
                    repository.resetTimer()
                    if (notification?.mediaToken != null)
                        repository.sendCommand(DotIslandCommand.SkipPrevious(notification.packageName))
                    else notification.sendFirstAction(context, "previous", "prev", "rewind")
                }
                MediaButton(if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    if (isPlaying) "Pause" else "Play", 40.dp) {
                    repository.resetTimer()
                    val play = !isPlaying
                    isPlaying = play
                    if (notification?.mediaToken != null)
                        repository.sendCommand(DotIslandCommand.PlayPause(notification.packageName))
                    else notification.sendFirstAction(context, "play", "pause", "resume")
                }
                MediaButton(Icons.Rounded.FastForward, "Next track", 36.dp) {
                    repository.resetTimer()
                    if (notification?.mediaToken != null)
                        repository.sendCommand(DotIslandCommand.SkipNext(notification.packageName))
                    else notification.sendFirstAction(context, "next", "skip", "forward")
                }
            }
            Box(Modifier.align(Alignment.CenterEnd)) {
                MediaButton(Icons.Rounded.Headphones, "Audio output", 24.dp, MEDIA_SECONDARY) {
                    repository.resetTimer()
                    onCollapse()
                    openAudioOutput(context)
                }
            }
        }
    }
}

@Composable
private fun MediaButton(
    icon: ImageVector,
    label: String,
    iconSize: Dp,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Box(Modifier.size(48.dp).bounceClick(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(iconSize))
    }
}

@Composable
private fun MusicTimeline(
    notification: IslandNotification?,
    isPlaying: Boolean,
    durationMs: Long?,
    onSeek: (Long) -> Unit
) {
    var positionMs by remember(notification?.key, notification?.title) {
        mutableStateOf(notification?.mediaPositionMs ?: 0L)
    }
    var lastSeekElapsed by remember(notification?.key, notification?.title) { mutableStateOf(0L) }
    LaunchedEffect(isPlaying, notification?.mediaPositionMs, notification?.mediaPositionUpdatedAtMs,
        notification?.mediaPlaybackSpeed) {
        do {
            val now = SystemClock.elapsedRealtime()
            if (now - lastSeekElapsed > SEEK_SETTLE_MS) {
                positionMs = extrapolateMediaPosition(notification?.mediaPositionMs ?: 0L,
                    notification?.mediaPositionUpdatedAtMs, now, isPlaying,
                    notification?.mediaPlaybackSpeed ?: 0f, durationMs)
            }
            if (isPlaying) kotlinx.coroutines.delay(POSITION_POLL_INTERVAL_MS)
        } while (isPlaying)
    }
    val progress = if (durationMs != null) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f)
        else if (notification != null && notification.progressMax > 0)
            (notification.progress.toFloat() / notification.progressMax).coerceIn(0f, 1f) else 0f
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(formatDuration(positionMs), color = MEDIA_SECONDARY, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        WavyMusicSeekBar(progress = progress, isPlaying = isPlaying, modifier = Modifier.weight(1f),
            progressLabel = "${notification?.title ?: "Music"} playback position",
            onSeek = { fraction ->
                if (durationMs != null) {
                    positionMs = (fraction * durationMs).toLong()
                    lastSeekElapsed = SystemClock.elapsedRealtime()
                    onSeek(positionMs)
                }
            })
        Text(formatRemainingDuration(positionMs, durationMs), color = MEDIA_SECONDARY, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
    }
}

internal fun formatRemainingDuration(positionMs: Long, durationMs: Long?): String =
    durationMs?.takeIf { it > 0 }?.let { "−${formatDuration((it - positionMs).coerceAtLeast(0))}" } ?: "--:--"

private fun openAudioOutput(context: Context) {
    val shown = Build.VERSION.SDK_INT >= 34 && runCatching {
        MediaRouter2.getInstance(context).showSystemOutputSwitcher()
    }.getOrDefault(false)
    if (!shown) {
        // Service overlays may be classified as background by the system switcher.
        runCatchingLogged("MusicExpanded", "Failed to open audio output settings") {
            context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}

private val MEDIA_SECONDARY = Color(0xFF8E8E93)
private val MUSIC_CONTENT_INSET = 20.dp
private const val POSITION_POLL_INTERVAL_MS = 100L
private const val SEEK_SETTLE_MS = 1500L

internal fun extrapolateMediaPosition(base: Long, updatedAt: Long?, now: Long,
    playing: Boolean, speed: Float, duration: Long?): Long {
    val elapsed = if (playing && updatedAt != null && updatedAt > 0 && speed.isFinite() && speed > 0f)
        ((now - updatedAt).coerceAtLeast(0) * speed).toLong() else 0L
    val position = (base.coerceAtLeast(0) + elapsed).coerceAtLeast(0)
    return duration?.takeIf { it > 0 }?.let { position.coerceAtMost(it) } ?: position
}
