package dev.qarasky.dotisland.ui

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp
import dev.qarasky.dotisland.model.*
import dev.qarasky.dotisland.ui.expanded.*
import org.junit.Assert.*
import org.junit.Test

class ExpandedContentLayoutTest {
    private val environment = ExpandedLayoutEnvironment(420.dp, 2.75f, 1f,
        listOf(Rect(578f, 47f, 646f, 116f)), 12.dp)
    private val source = IslandNotification("order", "ru.dodopizza.app", "Dodo", "Cooking",
        "Arriving in ~15 min", 0, mode = IslandMode.LiveActivity, progress = 50, progressMax = 100)

    @Test fun cacheSurvivesCardSubtreeAndIgnoresTimingAndFillUpdates() {
        val measurements = ExpandedContentMeasurements()
        val key = expandedLayoutKey(source, environment)
        measurements.record(key, 124.dp)
        assertEquals(124.dp, measurements.heights[expandedLayoutKey(
            source.copy(timeMillis = 100, progress = 90, mediaPositionMs = 8000), environment)])
    }
    @Test fun textFontWidthAndCameraChangesRequireNewMeasurement() {
        val key = expandedLayoutKey(source, environment)
        assertNotEquals(key, expandedLayoutKey(source.copy(text = "More detail"), environment))
        assertNotEquals(key, expandedLayoutKey(source, environment.copy(fontScale = 1.2f)))
        assertNotEquals(key, expandedLayoutKey(source, environment.copy(width = 390.dp)))
        assertNotEquals(key, expandedLayoutKey(source, environment.copy(cameras = emptyList())))
        assertNotEquals(key, expandedLayoutKey(source.copy(progressMax = 0), environment))
    }
    @Test fun cacheIsBoundedPerSourceAndPrunesRemovedActivities() {
        val measurements = ExpandedContentMeasurements()
        measurements.record(expandedLayoutKey(source, environment), 124.dp)
        measurements.record(expandedLayoutKey(source.copy(title = "On the way"), environment), 136.dp)
        assertEquals(1, measurements.heights.size)
        measurements.retainSources(emptySet())
        assertTrue(measurements.heights.isEmpty())
    }
    @Test fun pagerHeightIsDirectInterpolationInEitherDirection() {
        assertEquals(150.dp, interpolatePageHeight(180.dp, 120.dp, 0.5f))
        assertEquals(150.dp, interpolatePageHeight(180.dp, 120.dp, -0.5f))
        assertEquals(180.dp, interpolatePageHeight(180.dp, 120.dp, 0f))
        assertEquals(120.dp, interpolatePageHeight(180.dp, 120.dp, 1f))
    }
    @Test fun playbackPositionUsesSnapshotTimeAndHonoursPauseBufferingAndBounds() {
        assertEquals(3500L, extrapolateMediaPosition(2000, 1000, 2000, true, 1.5f, 10000))
        assertEquals(2000L, extrapolateMediaPosition(2000, 1000, 2000, false, 1f, 10000))
        assertEquals(2000L, extrapolateMediaPosition(2000, 1000, 2000, true, 0f, 10000))
        assertEquals(2000L, extrapolateMediaPosition(2000, null, 2000, true, 1f, 10000))
        assertEquals(3000L, extrapolateMediaPosition(2000, 1000, 9000, true, 1f, 3000))
    }
}
