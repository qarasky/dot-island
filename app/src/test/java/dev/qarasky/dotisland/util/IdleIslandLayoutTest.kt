package dev.qarasky.dotisland.util

import androidx.compose.ui.geometry.Rect
import dev.qarasky.dotisland.data.DotIslandSettings
import org.junit.Assert.*
import org.junit.Test

class IdleIslandLayoutTest {
    @Test fun hugsActualA069CameraPathWithTwoDpClearance() {
        val camera = Rect(578.2f / 2.75f, 47.6f / 2.75f, 645.8f / 2.75f, 115.5f / 2.75f)
        val mini = fitIdleIslandAroundCamera(DotIslandSettings.Default, 1224f / 2.75f, listOf(camera))
        assertEquals(camera.width + 4f, mini.width, 0.001f)
        assertEquals(camera.height + 4f, mini.height, 0.001f)
        assertEquals(camera.top - 2f, mini.yOffset, 0.001f)
        assertEquals(camera.center.x, 1224f / 2.75f / 2f + mini.xOffset, 0.001f)
        assertTrue(mini.width < 30f)
        assertTrue(mini.height < 30f)
    }

    @Test fun fallbackIsSmallAndKeepsOriginalVerticalCenter() {
        val base = DotIslandSettings.Default
        val mini = fitIdleIslandAroundCamera(base, 445f, emptyList())
        assertEquals(29f, mini.width, 0f)
        assertEquals(29f, mini.height, 0f)
        assertEquals(base.yOffset + base.height / 2f, mini.yOffset + mini.height / 2f, 0f)
        assertEquals(112f, base.width, 0f)
    }

    @Test fun offCenterCameraIsNotForcedToDisplayCenter() {
        val mini = fitIdleIslandAroundCamera(DotIslandSettings.Default, 400f,
            listOf(Rect(150f, 12f, 174f, 36f)))
        assertEquals(-38f, mini.xOffset, 0f)
        assertEquals(10f, mini.yOffset, 0f)
        assertEquals(14f, mini.cornerRadius, 0f)
    }
}
