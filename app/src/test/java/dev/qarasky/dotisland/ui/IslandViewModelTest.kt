package dev.qarasky.dotisland.ui

import dev.qarasky.dotisland.data.*
import dev.qarasky.dotisland.model.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class IslandViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun teardown() { Dispatchers.resetMain() }
    private fun viewModel(repo: INotificationRepository): IslandViewModel {
        val settings = mockk<DotIslandSettingsRepository>()
        every { settings.settings } returns MutableStateFlow(DotIslandSettings(enabled = true))
        return IslandViewModel(settings, repo)
    }
    private fun music() = IslandNotification("spotify", "com.spotify.music", "Spotify", "Song", "Artist", 0L, mode = IslandMode.Music)
    private fun dodo() = IslandNotification("dodo", "ru.dodopizza.app", "Dodo Pizza", "Готовим", "Через 37 мин", 0L, mode = IslandMode.LiveActivity)
    @Test fun selectingCompanionSwitchesMainActivityWithoutExpanding() = runTest {
        val repo = DotIslandNotificationRepository()
        val vm = viewModel(repo)
        repo.postNotification(dodo())
        repo.postNotification(music())
        runCurrent()
        repeat(6) { iteration ->
            val target = if (iteration % 2 == 0) IslandMode.Music else IslandMode.LiveActivity
            val index = vm.visibleNotifications.value.indexOfFirst { it.mode == target }
            vm.setSelectedNotificationIndex(index)
            runCurrent()
            assertEquals(target, vm.mode.value)
            assertFalse(vm.expanded.value)
        }
        vm.expand()
        assertTrue(vm.expanded.value)
    }
    @Test fun idleCannotExpand() = runTest {
        val vm = viewModel(DotIslandNotificationRepository())
        runCurrent()
        vm.expand()
        assertFalse(vm.expanded.value)
    }
    @Test fun rapidReversalsAreNotDroppedByTimeGate() = runTest {
        val repo = DotIslandNotificationRepository()
        val vm = viewModel(repo)
        repo.postNotification(music())
        runCurrent()
        repeat(5) {
            vm.toggleExpanded()
            assertTrue(vm.expanded.value)
            vm.toggleExpanded()
            assertFalse(vm.expanded.value)
        }
        vm.expand()
        vm.expand()
        assertTrue(vm.expanded.value)
        vm.collapse()
        vm.collapse()
        assertFalse(vm.expanded.value)
    }
    @Test fun idleStartsSmallAndTapRevealsOnlyTemporarily() = runTest {
        val vm = viewModel(DotIslandNotificationRepository())
        runCurrent()
        assertFalse(vm.idleRevealed.value)
        vm.toggleIdleReveal()
        runCurrent()
        assertTrue(vm.idleRevealed.value)
        assertFalse(vm.expanded.value)
        advanceTimeBy(IslandViewModel.IDLE_REVEAL_DELAY_MS)
        runCurrent()
        assertFalse(vm.idleRevealed.value)
    }
    @Test fun tappingRevealedIdleTucksItAwayAndCancelsOldTimer() = runTest {
        val vm = viewModel(DotIslandNotificationRepository())
        runCurrent()
        vm.toggleIdleReveal()
        runCurrent()
        advanceTimeBy(1000)
        vm.toggleIdleReveal()
        assertFalse(vm.idleRevealed.value)
        vm.toggleIdleReveal()
        runCurrent()
        advanceTimeBy(4000)
        runCurrent()
        assertTrue(vm.idleRevealed.value)
        advanceTimeBy(1000)
        runCurrent()
        assertFalse(vm.idleRevealed.value)
    }
    @Test fun activityArrivalClearsIdleRevealAndReturningToIdleStartsSmall() = runTest {
        val repo = DotIslandNotificationRepository()
        val vm = viewModel(repo)
        runCurrent()
        vm.toggleIdleReveal()
        repo.postNotification(music())
        runCurrent()
        assertFalse(vm.idleRevealed.value)
        vm.toggleIdleReveal()
        assertFalse(vm.idleRevealed.value)
        repo.removeAllNotifications()
        runCurrent()
        assertFalse(vm.idleRevealed.value)
        assertFalse(vm.expanded.value)
    }
    @Test fun musicStaysVisibleInsideItsOwnApp() = runTest {
        val repo = DotIslandNotificationRepository()
        val vm = viewModel(repo)
        repo.postNotification(music())
        runCurrent()
        assertEquals(1, vm.visibleNotifications.value.size)
        // Inside Spotify with music playing: the pill keeps showing music,
        // never a blank pill that is neither music nor idle.
        vm.foregroundPackage.value = "com.spotify.music"
        runCurrent()
        assertEquals(1, vm.visibleNotifications.value.size)
        assertEquals(IslandMode.Music, vm.mode.value)
        assertTrue(vm.hasSourceActivity.value)
        // Leaving the app changes nothing about visibility either.
        vm.foregroundPackage.value = "com.nothing.launcher"
        runCurrent()
        assertEquals(1, vm.visibleNotifications.value.size)
    }
    @Test fun lossOfLastActivityCollapsesAndNewActivityDoesNotAutoExpand() = runTest {
        val repo = DotIslandNotificationRepository()
        val vm = viewModel(repo)
        repo.postNotification(music(), autoExpand = true)
        runCurrent()
        assertFalse(vm.expanded.value)
        vm.expand()
        assertTrue(vm.expanded.value)
        repo.removeAllNotifications()
        runCurrent()
        assertFalse(vm.expanded.value)
    }
}
