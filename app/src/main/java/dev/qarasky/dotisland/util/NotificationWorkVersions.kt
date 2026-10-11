/*
 * Dot Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */
package dev.qarasky.dotisland.util

import dev.qarasky.dotisland.model.IslandMode

/** Main-thread event ownership; workers may finish in any order without reviving old slots. */
internal class NotificationWorkVersions {
    private var sequence = 0L
    private val keys = mutableMapOf<String, Long>()
    private val slots = mutableMapOf<IslandMode, Long>()
    fun begin(key: String, mode: IslandMode): Long = (++sequence).also {
        keys[key] = it
        slots[mode] = it
    }
    fun isCurrent(key: String, mode: IslandMode, version: Long): Boolean =
        keys[key] == version && slots[mode] == version
    fun remove(key: String) { keys.remove(key) }
    fun clear() { keys.clear(); slots.clear() }
}
