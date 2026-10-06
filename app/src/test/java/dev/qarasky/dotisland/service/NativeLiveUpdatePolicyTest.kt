package dev.qarasky.dotisland.service

import dev.qarasky.dotisland.model.IslandMode
import dev.qarasky.dotisland.util.PersonalActivityPolicy as Policy
import org.junit.Assert.assertEquals
import org.junit.Test

class NativeLiveUpdatePolicyTest {
    private val source = "dev.example.delivery"
    private fun classify(allowed: Boolean = true, ongoing: Boolean = true,
        style: Boolean = true, promoted: Boolean = false, media: Boolean = false,
        category: String? = null, summary: Boolean = false): IslandMode = Policy.mode(
        source, category, media, ongoing, "Delivery status", "Arriving soon", 100, summary,
        allowedLiveUpdatePackages = if (allowed) setOf(source) else emptySet(),
        progressStyle = style, promotedOngoing = promoted
    )

    @Test fun approvedOngoingProgressStyleIsAccepted() {
        assertEquals(IslandMode.LiveActivity, classify())
    }
    @Test fun approvedPromotedOngoingUpdateDoesNotNeedProgressStyle() {
        assertEquals(IslandMode.LiveActivity, classify(style = false, promoted = true))
    }
    @Test fun unapprovedNativeLiveUpdateIsIgnored() {
        assertEquals(IslandMode.Empty, classify(allowed = false))
        assertEquals(IslandMode.Empty, classify(allowed = false, promoted = true))
    }
    @Test fun progressFieldsAloneDoNotTurnOrdinaryAlertsIntoLiveUpdates() {
        assertEquals(IslandMode.Empty, classify(style = false))
        assertEquals(IslandMode.Empty, classify(ongoing = false))
        assertEquals(IslandMode.Empty, classify(ongoing = false, promoted = true))
    }
    @Test fun groupSummariesCallsAndMediaCannotEnterGenericPath() {
        assertEquals(IslandMode.Empty, classify(summary = true))
        assertEquals(IslandMode.Empty, classify(media = true))
        assertEquals(IslandMode.Empty, classify(category = "call"))
    }
    @Test fun revokingDodoAlsoDisablesCompatibilityFallback() {
        assertEquals(IslandMode.Empty, Policy.mode(Policy.DODO, null, false, true,
            "Готовим", "Будем через ~43 мин", 100, allowedLiveUpdatePackages = emptySet()))
    }
    @Test fun dodoPromotionIsRejectedEvenIfProgressStyled() {
        assertEquals(IslandMode.Empty, Policy.mode(Policy.DODO, null, false, true,
            "Ваш заказ", "Промокод на скидку", 100, progressStyle = true))
    }
}
