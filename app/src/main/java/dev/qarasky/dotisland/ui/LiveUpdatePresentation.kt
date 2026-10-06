/*
 * Dot Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */
package dev.qarasky.dotisland.ui

import dev.qarasky.dotisland.model.IslandNotification
import dev.qarasky.dotisland.util.LiveActivityParser
import dev.qarasky.dotisland.util.PersonalActivityPolicy
import kotlin.math.roundToInt

internal fun liveUpdateCompactStatus(notification: IslandNotification?): String {
    if (notification == null) return "LIVE"
    LiveActivityParser.eta(notification.title, notification.text)?.let { return it }
    if (!notification.progressIndeterminate) {
        LiveActivityParser.progress(notification.progress, notification.progressMax)?.let {
            return "${(it * 100f).roundToInt()}%"
        }
    }
    return if (notification.packageName == PersonalActivityPolicy.DODO) "ORDER" else "LIVE"
}

internal fun liveUpdateIsWorking(notification: IslandNotification): Boolean =
    if (notification.packageName == PersonalActivityPolicy.DODO) {
        !LiveActivityParser.isComplete(notification.title)
    } else notification.liveUpdateOngoing || notification.progressIndeterminate ||
        (LiveActivityParser.progress(notification.progress, notification.progressMax)?.let { it < 1f } == true)
