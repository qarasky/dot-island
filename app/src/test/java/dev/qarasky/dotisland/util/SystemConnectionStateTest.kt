package dev.qarasky.dotisland.util

import org.junit.Assert.*
import org.junit.Test

class SystemConnectionStateTest {
    @Test fun staleInstanceCannotDisconnectReplacement() {
        val connection = SystemConnectionState()
        val old = Any()
        val replacement = Any()
        assertFalse(connection.state.value)
        connection.connect(old)
        connection.connect(replacement)
        connection.disconnect(old)
        assertTrue(connection.state.value)
        connection.disconnect(replacement)
        assertFalse(connection.state.value)
    }
}
