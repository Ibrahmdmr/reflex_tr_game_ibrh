package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.runtime.Composable

@Composable
internal fun ProfileTabContent(
    bestScore: Int,
    playerProfile: PlayerProfile,
    progressionState: ProgressionState,
    onEditNameClick: () -> Unit,
    onTitleSelect: (PlayerTitle) -> Unit,
    onTitlesOpened: () -> Unit,
    onPersonalGoalClaim: () -> Unit,
    onProfileBadgeSelect: (ProfileBadge) -> Unit,
    onQuickMenuSelected: (HomeTab) -> Unit
) {
    ProfileProgressCard(
        playerProfile = playerProfile,
        progressionState = progressionState,
        bestScore = bestScore,
        onEditNameClick = onEditNameClick
    )
    PlayerTitleCard(
        profile = playerProfile,
        progressionState = progressionState,
        onTitleSelect = onTitleSelect,
        onTitlesOpened = onTitlesOpened
    )
    PersonalGoalCard(
        state = progressionState.personalGoal,
        onClaimClick = onPersonalGoalClaim
    )
    BadgeShowcaseCard(
        progressionState = progressionState,
        onBadgeSelected = onProfileBadgeSelect
    )
    ProfileQuickMenu(onTabSelected = onQuickMenuSelected)
}
