package dev.qarasky.dotisland.ui

import dev.qarasky.dotisland.model.IslandMode
import dev.qarasky.dotisland.model.IslandNotification
import dev.qarasky.dotisland.util.PersonalActivityPolicy
import org.junit.Assert.*
import org.junit.Test

class LiveUpdatePresentationTest {
    private fun update() = IslandNotification(packageName = "dev.example.delivery",
        appName = "Delivery", title = "On the way", text = "", timeMillis = 0,
        mode = IslandMode.LiveActivity, liveUpdateOngoing = true)

    @Test fun genericCompactLabelsUseOnlySourceValues() {
        assertEquals("LIVE", liveUpdateCompactStatus(update()))
        assertEquals("40%", liveUpdateCompactStatus(update().copy(progress = 40, progressMax = 100)))
        assertEquals("~15 мин", liveUpdateCompactStatus(update().copy(text = "Будем через ~15 мин")))
        assertEquals("LIVE", liveUpdateCompactStatus(update().copy(
            progress = 40, progressMax = 100, progressIndeterminate = true)))
        assertEquals("ORDER", liveUpdateCompactStatus(update().copy(packageName = PersonalActivityPolicy.DODO)))
    }
    @Test fun ongoingFullProgressDoesNotMeanActivityIsOver() {
        assertTrue(liveUpdateIsWorking(update().copy(progress = 100, progressMax = 100)))
        assertFalse(liveUpdateIsWorking(update().copy(progress = 100, progressMax = 100, liveUpdateOngoing = false)))
    }
    @Test fun dodoKeepsExplicitCompletionFallback() {
        assertTrue(liveUpdateIsWorking(update().copy(packageName = PersonalActivityPolicy.DODO,
            title = "Везем заказ", progress = 100, progressMax = 100)))
        assertFalse(liveUpdateIsWorking(update().copy(packageName = PersonalActivityPolicy.DODO,
            title = "Заказ доставлен", progress = 100, progressMax = 100)))
    }
    @Test fun indeterminateDotsPulseWithoutInventingCompletedPortion() {
        repeat(20) { index ->
            assertEquals(0.24f, orderProgressDotOpacity(null, index, 20, 0f), 0.001f)
            assertEquals(0.52f, orderProgressDotOpacity(null, index, 20, 1f), 0.001f)
        }
    }
}
