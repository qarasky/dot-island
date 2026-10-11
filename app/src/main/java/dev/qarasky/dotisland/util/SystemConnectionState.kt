/*
 * Dot Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */
package dev.qarasky.dotisland.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** A late disconnect from a replaced instance must not clear the new connection. */
internal class SystemConnectionState {
    private var owner: Any? = null
    private val connected = MutableStateFlow(false)
    val state = connected.asStateFlow()
    @Synchronized fun connect(instance: Any) { owner = instance; connected.value = true }
    @Synchronized fun disconnect(instance: Any) {
        if (owner === instance) { owner = null; connected.value = false }
    }
}
