package dev.qarasky.dotisland.util

import org.junit.Assert.*
import org.junit.Test

class SystemServiceRecoveryTest {
    @Test fun neverRestartsConnectedOrUnauthorizedServiceEvenOnManualRetry() {
        assertFalse(shouldRestartAccessibility(true, true, 20000, 0, true))
        assertFalse(shouldRestartAccessibility(false, false, 20000, 0, true))
    }
    @Test fun firstAttemptAndBoundedRetryAreAllowed() {
        assertTrue(shouldRestartAccessibility(false, true, 10000, 0))
        assertFalse(shouldRestartAccessibility(false, true, 10001, 10000))
        assertTrue(shouldRestartAccessibility(false, true, 20000, 10000))
        assertTrue(shouldRestartAccessibility(false, true, 10001, 10000, true))
    }
}
