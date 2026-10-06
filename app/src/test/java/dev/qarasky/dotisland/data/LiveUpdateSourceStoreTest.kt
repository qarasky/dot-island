package dev.qarasky.dotisland.data

import dev.qarasky.dotisland.util.PersonalActivityPolicy
import org.junit.Assert.*
import org.junit.Test

class LiveUpdateSourceStoreTest {
    @Test fun onlyUnconfiguredStoreDefaultsToDodo() {
        assertEquals(setOf(PersonalActivityPolicy.DODO), liveUpdateSourceDefaults(null))
        assertTrue(liveUpdateSourceDefaults(emptySet()).isEmpty())
        assertEquals(setOf("dev.example.delivery"), liveUpdateSourceDefaults(setOf("dev.example.delivery")))
    }
    @Test fun sourcePackageNamesAreValidated() {
        assertTrue(isLiveUpdatePackageName("ru.dodopizza.app"))
        assertTrue(isLiveUpdatePackageName("dev.example.live_updates"))
        assertFalse(isLiveUpdatePackageName(""))
        assertFalse(isLiveUpdatePackageName("dev..example"))
        assertFalse(isLiveUpdatePackageName("dev.example delivery"))
        assertFalse(isLiveUpdatePackageName("dev.example\nother"))
    }
}
