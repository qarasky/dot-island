/*
 * Dot Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package dev.qarasky.dotisland

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.qarasky.dotisland.model.IslandMode
import dev.qarasky.dotisland.model.IslandNotification
import dev.qarasky.dotisland.ui.IslandCollapsedContent
import dev.qarasky.dotisland.ui.DotIslandTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IslandOverlaySmokeTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun tinyIdleRevealsOldPillOnTapWithoutExpandingAnActivity() {
        val revealed = mutableStateOf(false)
        var expansionCalls = 0
        compose.setContent {
            val base = dev.qarasky.dotisland.data.DotIslandSettings.Default
            DotIslandTheme {
                dev.qarasky.dotisland.ui.IslandOverlayView(
                    settings = if (revealed.value) base else base.copy(width = 29f, height = 29f, yOffset = 14.5f, cornerRadius = 14.5f),
                    expanded = false, notifications = emptyList(), selectedIndex = 0,
                    onPageSelected = {}, onOpenNotification = {}, onToggleExpanded = { expansionCalls++ },
                    onDismissNotification = {}, onOpenFloatingWindow = {}, statusBarHeight = 24f,
                    showIdleIndicator = revealed.value, isMinimalIdle = !revealed.value,
                    onIdleTap = { revealed.value = !revealed.value }
                )
            }
        }
        compose.onNodeWithText("IDLE").assertDoesNotExist()
        compose.onNodeWithContentDescription("Reveal idle island").performClick()
        compose.onNodeWithText("IDLE").assertExists()
        // The host test Activity is full-screen, unlike the real compact window.
        // Tap the actual pill, not the centre of that full-screen root.
        compose.onNodeWithTag("islandSurface").performTouchInput { click() }
        compose.runOnIdle { org.junit.Assert.assertFalse(revealed.value) }
        // Wait for recomposition AND the outgoing content fade, not just clock
        // time advanced before Android has committed the new layout.
        compose.waitUntil(timeoutMillis = 5000) {
            compose.onAllNodesWithText("IDLE").fetchSemanticsNodes().isEmpty()
        }
        compose.onNodeWithText("IDLE").assertDoesNotExist()
        compose.runOnIdle { org.junit.Assert.assertEquals(0, expansionCalls) }
    }

    @Test
    fun personal_screen_has_only_one_switch_and_no_old_feature_panels() {
        compose.setContent {
            val context = LocalContext.current
            DotIslandTheme {
                dev.qarasky.dotisland.ui.DotIslandHomeScreen(
                    dev.qarasky.dotisland.data.DotIslandSettingsRepository(context),
                    dev.qarasky.dotisland.data.DotIslandNotificationRepository(),
                    dev.qarasky.dotisland.data.DotIslandPublisherStore(context),
                    dev.qarasky.dotisland.data.LiveUpdateSourceStore(context)
                )
            }
        }
        compose.onAllNodes(isToggleable()).assertCountEquals(1)
        compose.onNodeWithText("Spotify").assertExists()
        compose.onNodeWithText("LIVE UPDATES").assertExists()
        compose.onNodeWithText("Calls").assertExists()
        compose.onNodeWithText("Allowed notification sources").assertExists()
        compose.onNodeWithText("Color Studio").assertDoesNotExist()
        compose.onNodeWithText("Notification History").assertDoesNotExist()
    }

    @Test
    fun collapsed_renders_idle() {
        compose.setContent {
            DotIslandTheme {
                IslandCollapsedContent(
                    mode = IslandMode.Empty,
                    notification = null,
                    collapsedAlpha = 1f,
                    settings = dev.qarasky.dotisland.data.DotIslandSettings.Default,
                    showIdleIndicator = true
                )
            }
        }
        compose.onNodeWithText("IDLE").assertExists()
    }
}
