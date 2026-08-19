package com.reflex.tr.game.ibrh.ui.game

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ads.PremiumState
import com.reflex.tr.game.ibrh.ui.game.components.SecondaryGameButton
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

/**
 * The one place that answers "what can I get out of an ad, and how much is left today?".
 *
 * Nothing here starts an ad the player did not ask for, and an offer that cannot be taken shows a
 * status line instead of a dead button — a greyed-out button reads as broken rather than as spent.
 */
@Composable
internal fun BonusesSection(
    offers: List<RewardedOfferState>,
    premiumState: PremiumState,
    onOfferClick: (RewardedOfferType) -> Unit,
    onLimitReached: (RewardedOfferType) -> Unit,
    onPremiumClick: () -> Unit,
    onOpened: (List<RewardedOfferState>) -> Unit
) {
    // One report per visit, not per ad-state change: keying this on `offers` re-fired the whole
    // set every time an ad finished loading and inflated every "viewed" count.
    val openedOffers by rememberUpdatedState(offers)
    LaunchedEffect(Unit) { onOpened(openedOffers) }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // One line rather than five "spent" cards shouting the same thing.
        if (offers.none { it.isAvailable } && offers.any { it.hasDailyLimit }) {
            Text(
                text = stringResource(R.string.bonus_limit_reached),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelMedium,
                color = ReflexGamePalette.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        offers.forEach { offer ->
            BonusOfferCard(
                offer = offer,
                onClaimClick = { onOfferClick(offer.type) },
                onLimitReached = { onLimitReached(offer.type) }
            )
        }
        PremiumComingSoonCard(
            premiumState = premiumState,
            onClick = onPremiumClick
        )
    }
}

@Composable
private fun BonusOfferCard(
    offer: RewardedOfferState,
    onClaimClick: () -> Unit,
    onLimitReached: () -> Unit
) {
    val accent = rewardedOfferAccent(offer.type.kind)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(PremiumCardRadius),
        border = BorderStroke(1.dp, accent.copy(alpha = if (offer.isAvailable) 0.40f else 0.20f))
    ) {
        Row(
            modifier = Modifier.padding(PremiumCardPadding),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = stringResource(offer.type.titleRes),
                    style = MaterialTheme.typography.titleSmall,
                    color = ReflexGamePalette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(offer.type.descriptionRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                bonusOfferStatusText(offer)?.let { status ->
                    Text(
                        text = status,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (offer.isAvailable) accent else ReflexGamePalette.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            // Only an offer that can actually pay out gets a button; the rest say why not.
            if (offer.isAvailable) {
                // Bounded width: the button fills its width, so an unconstrained one in a Row
                // collapses the text column beside it.
                SecondaryGameButton(
                    modifier = Modifier
                        .widthIn(min = 104.dp, max = 124.dp)
                        .height(46.dp),
                    text = stringResource(R.string.bonus_watch_and_claim),
                    onClick = onClaimClick
                )
            } else {
                Text(
                    text = stringResource(bonusOfferBlockedLabelRes(offer.availability)),
                    modifier = Modifier.widthIn(max = 104.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
    if (offer.availability == RewardedOfferAvailability.LimitReached) {
        // Reported on render rather than on tap: a spent offer has no button left to tap.
        LaunchedEffect(offer.type) { onLimitReached() }
    }
}

/** The line that tells the player how much of today is left, when there is a daily count at all. */
@Composable
private fun bonusOfferStatusText(offer: RewardedOfferState): String? = when {
    !offer.hasDailyLimit -> null
    offer.remaining > 0 -> stringResource(
        R.string.bonus_remaining_value,
        offer.remaining,
        offer.dailyLimit
    )
    else -> stringResource(R.string.bonus_used_value, offer.usedToday, offer.dailyLimit)
}

@StringRes
private fun bonusOfferBlockedLabelRes(availability: RewardedOfferAvailability): Int = when (availability) {
    RewardedOfferAvailability.LimitReached -> R.string.bonus_claimed_today
    RewardedOfferAvailability.AdNotReady -> R.string.bonus_ad_not_ready
    RewardedOfferAvailability.DuringGameOnly -> R.string.bonus_during_game_only
    RewardedOfferAvailability.NotApplicable -> R.string.bonus_later
    RewardedOfferAvailability.Available -> R.string.bonus_watch_and_claim
}

/**
 * Honest placeholder. There is no purchase flow behind this yet, so it says so rather than
 * offering a button that would do nothing.
 */
@Composable
private fun PremiumComingSoonCard(
    premiumState: PremiumState,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = ReflexGamePalette.cardGlassStrong,
        shape = RoundedCornerShape(PremiumCardRadius),
        border = BorderStroke(1.dp, ArcadeTeal.copy(alpha = 0.28f))
    ) {
        Row(
            modifier = Modifier.padding(PremiumCardPadding),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = stringResource(R.string.premium_no_ads_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = ReflexGamePalette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.premium_no_ads_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = stringResource(
                    if (premiumState.isNoAdsUser) {
                        R.string.premium_active
                    } else {
                        R.string.premium_coming_soon
                    }
                ),
                modifier = Modifier.widthIn(max = 104.dp),
                style = MaterialTheme.typography.labelMedium,
                color = ArcadeGold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
