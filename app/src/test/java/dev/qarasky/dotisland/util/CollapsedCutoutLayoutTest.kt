package dev.qarasky.dotisland.util

import androidx.compose.ui.geometry.Rect
import dev.qarasky.dotisland.data.DotIslandSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CollapsedCutoutLayoutTest {
    private val settings = DotIslandSettings.Default
    private val camera = Rect(210f, 17f, 235f, 42f)
    private fun left(s: DotIslandSettings) = 222.5f + s.xOffset - s.width / 2f
    private fun right(s: DotIslandSettings) = left(s) + s.width

    @Test fun longEtaExtendsOnlyRightEdgeAndPreservesCameraClearance() {
        val result = fitCollapsedCutoutSides(settings, 445f, listOf(camera), 22f, 43f)
        assertEquals(left(settings), left(result), 0.001f)
        assertEquals(296f, right(result), 0.001f)
        assertEquals(camera.right + 6f, right(result) - 12f - 43f, 0.001f)
        assertEquals(settings.height, result.height, 0f)
    }

    @Test fun shortStatusDoesNotGrowEitherSide() {
        assertEquals(settings, fitCollapsedCutoutSides(settings, 445f, listOf(camera), 22f, 19f))
    }

    @Test fun wideLeadingContentExtendsOnlyLeftEdge() {
        val result = fitCollapsedCutoutSides(settings, 445f, listOf(camera), 44f, 19f)
        assertTrue(left(result) < left(settings))
        assertEquals(right(settings), right(result), 0.001f)
        assertEquals(camera.left - 6f, left(result) + 8f + 44f, 0.001f)
    }

    @Test fun missingOrVerticallySeparateCutoutDoesNotChangePill() {
        assertEquals(settings, fitCollapsedCutoutSides(settings, 445f, emptyList(), 22f, 43f))
        assertEquals(settings, fitCollapsedCutoutSides(settings, 445f,
            listOf(Rect(210f, 80f, 235f, 105f)), 22f, 43f))
    }

    @Test fun etaShrinkReturnsToBaselineInsteadOfAccumulatingGrowth() {
        val long = fitCollapsedCutoutSides(settings, 445f, listOf(camera), 22f, 60f)
        val short = fitCollapsedCutoutSides(settings, 445f, listOf(camera), 22f, 19f)
        assertTrue(long.width > short.width)
        assertEquals(settings, short)
    }
}
