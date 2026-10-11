package dev.qarasky.dotisland

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.qarasky.dotisland.data.DotIslandSettings
import dev.qarasky.dotisland.model.*
import dev.qarasky.dotisland.ui.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Functional clock tests, not a frame-time benchmark or a system-overlay test. */
@RunWith(AndroidJUnit4::class)
class IslandAnimationTest {
    @get:Rule val compose = createComposeRule()
    private val order = IslandNotification("order", "ru.dodopizza.app", "Dodo Pizza",
        "Заказ доставлен", "", 0, progress = 100, progressMax = 100, mode = IslandMode.LiveActivity)
    private val music = IslandNotification("music", "com.spotify.music", "Spotify",
        "Song", "Artist", 0, mode = IslandMode.Music)

    @Test fun measuredCardHeightIsStableAndReusedOnReopen() {
        val expanded = mutableStateOf(false)
        compose.setContent {
            DotIslandTheme {
                IslandOverlayView(DotIslandSettings.Default, expanded.value, listOf(order), 0,
                    onPageSelected = {}, onOpenNotification = {},
                    onToggleExpanded = { expanded.value = !expanded.value },
                    onDismissNotification = {}, onOpenFloatingWindow = {}, statusBarHeight = 24f,
                    onExpand = { expanded.value = true }, onCollapse = { expanded.value = false })
            }
        }
        val surface = compose.onNodeWithTag("islandSurface")
        val compactHeight = surface.fetchSemanticsNode().boundsInRoot.height
        compose.runOnIdle { expanded.value = true }
        val firstHeight = surface.fetchSemanticsNode().boundsInRoot.height
        assertTrue(firstHeight > compactHeight)
        compose.mainClock.advanceTimeBy(1000)
        assertEquals(firstHeight, surface.fetchSemanticsNode().boundsInRoot.height, 1f)
        compose.runOnIdle { expanded.value = false }
        assertEquals(compactHeight, surface.fetchSemanticsNode().boundsInRoot.height, 1f)
        compose.runOnIdle { expanded.value = true }
        assertEquals(firstHeight, surface.fetchSemanticsNode().boundsInRoot.height, 1f)
    }

    @Test fun companionTapSelectsAndHoldExpandsWhileFingerRemainsDown() {
        val selected = mutableStateOf(0)
        val expanded = mutableStateOf(false)
        compose.setContent {
            DotIslandTheme {
                IslandOverlayView(DotIslandSettings.Default, expanded.value, listOf(music, order), selected.value,
                    onPageSelected = { selected.value = it }, onOpenNotification = {},
                    onToggleExpanded = { expanded.value = !expanded.value },
                    onDismissNotification = {}, onOpenFloatingWindow = {}, statusBarHeight = 24f,
                    onExpand = { expanded.value = true }, onCollapse = { expanded.value = false })
            }
        }
        compose.onNodeWithTag("companionIsland-order").performTouchInput { click() }
        compose.runOnIdle {
            assertEquals(1, selected.value)
            assertFalse(expanded.value)
        }
        compose.onNodeWithTag("companionIsland-music").performTouchInput { down(center) }
        compose.mainClock.advanceTimeBy(350)
        compose.runOnIdle {
            assertEquals(0, selected.value)
            assertTrue(expanded.value)
        }
        // The companion fades away during expansion. Release on the persistent root.
        compose.onRoot().performTouchInput { up() }
    }
}
