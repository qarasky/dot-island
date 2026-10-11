package dev.qarasky.dotisland.util

import dev.qarasky.dotisland.model.IslandMode
import org.junit.Assert.*
import org.junit.Test

class NotificationWorkVersionsTest {
    @Test fun latestUpdateWinsEvenIfWorkersFinishInReverseOrder() {
        val work = NotificationWorkVersions()
        val old = work.begin("one", IslandMode.LiveActivity)
        val latest = work.begin("one", IslandMode.LiveActivity)
        assertFalse(work.isCurrent("one", IslandMode.LiveActivity, old))
        assertTrue(work.isCurrent("one", IslandMode.LiveActivity, latest))
    }
    @Test fun newerSourceOwnsSingleNativeSlotButDoesNotInvalidateCalls() {
        val work = NotificationWorkVersions()
        val old = work.begin("old-order", IslandMode.LiveActivity)
        val call = work.begin("call", IslandMode.IncomingCall)
        val latest = work.begin("new-order", IslandMode.LiveActivity)
        assertFalse(work.isCurrent("old-order", IslandMode.LiveActivity, old))
        assertTrue(work.isCurrent("new-order", IslandMode.LiveActivity, latest))
        assertTrue(work.isCurrent("call", IslandMode.IncomingCall, call))
        work.remove("new-order")
        assertFalse(work.isCurrent("new-order", IslandMode.LiveActivity, latest))
        assertFalse(work.isCurrent("old-order", IslandMode.LiveActivity, old))
    }
    @Test fun DisableRevokeOrDisconnectCannotResurrectOldWork() {
        val work = NotificationWorkVersions()
        val old = work.begin("one", IslandMode.LiveActivity)
        work.clear()
        val latest = work.begin("one", IslandMode.LiveActivity)
        assertFalse(work.isCurrent("one", IslandMode.LiveActivity, old))
        assertTrue(work.isCurrent("one", IslandMode.LiveActivity, latest))
    }
}
