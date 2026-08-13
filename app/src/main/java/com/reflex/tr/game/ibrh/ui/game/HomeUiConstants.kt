package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.ui.unit.dp

internal const val PLAYER_NAME_MAX_LENGTH = 12
internal const val LEVEL_UP_POPUP_DURATION_MS = 1_800L
internal const val LEVEL_UP_BONUS_COINS = 50

/**
 * Corner radius scale. The codebase previously used thirteen different literals — including
 * near-identical 13/15/17dp one-offs — so every surface is pinned to one of these steps instead.
 */
internal val PremiumCompactRadius = 14.dp
internal val PremiumChipRadius = 16.dp
internal val PremiumCardRadius = 18.dp
internal val PremiumSurfaceRadius = 20.dp
internal val PremiumOverlayRadius = 22.dp
internal val PremiumPanelRadius = 26.dp
internal val PremiumPillRadius = 999.dp
internal val PremiumCardPadding = 12.dp
internal val PremiumSectionSpacing = 10.dp
