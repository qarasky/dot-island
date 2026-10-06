package dev.qarasky.dotisland.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderProgressDotsTest {
    @Test fun halfProgressIsHalfBrightHalfDim() {
        repeat(20) { index ->
            assertEquals(if (index < 10) 0.82f else 0.16f,
                orderProgressDotOpacity(0.5f, index, 20, 0f), 0.001f)
        }
    }

    @Test fun onlyFrontierPulsesWithoutAdvancingCompletedDots() {
        repeat(20) { index ->
            val resting = orderProgressDotOpacity(0.5f, index, 20, 0f)
            val pulsing = orderProgressDotOpacity(0.5f, index, 20, 1f)
            if (index == 10) assertTrue(pulsing > resting)
            else assertEquals(resting, pulsing, 0f)
        }
    }

    @Test fun partialDotUsesOpacityRatherThanRoundingSourceProgress() {
        assertEquals(0.49f, orderProgressDotOpacity(0.525f, 10, 20, 0f), 0.001f)
    }

    @Test fun completeProgressIsBrightAndStopsPulsing() {
        repeat(20) { index ->
            assertEquals(0.82f, orderProgressDotOpacity(1f, index, 20, 1f), 0.001f)
        }
    }

    @Test fun sourceProgressAtOneHundredCanStillPulseWhileDelivering() {
        assertEquals(0.4f, orderProgressDotOpacity(1f, 19, 20, 1f, working = true), 0.001f)
        assertEquals(0.82f, orderProgressDotOpacity(1f, 18, 20, 1f, working = true), 0.001f)
        assertEquals(0.82f, orderProgressDotOpacity(1f, 19, 20, 1f, working = false), 0.001f)
    }

    @Test fun zeroProgressStartsDimAndOnlyFirstDotPulses() {
        assertEquals(0.16f, orderProgressDotOpacity(0f, 0, 20, 0f), 0.001f)
        assertEquals(0.9f, orderProgressDotOpacity(0f, 0, 20, 1f), 0.001f)
        assertEquals(0.16f, orderProgressDotOpacity(0f, 1, 20, 1f), 0.001f)
    }
}
