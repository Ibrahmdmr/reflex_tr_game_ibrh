package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

@Composable
internal fun CoinWalletCard(
    coins: Int,
    selectedTheme: PlayerTheme,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ArcadeGold.copy(alpha = 0.13f),
        shape = RoundedCornerShape(PremiumCardRadius),
        border = BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.34f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(ArcadeGold.copy(alpha = 0.24f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "◉", color = ArcadeGold, style = MaterialTheme.typography.titleMedium)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.coin_wallet_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = ArcadeGold
                )
                Text(
                    text = stringResource(R.string.coin_wallet_value, coins),
                    style = MaterialTheme.typography.titleMedium,
                    color = ReflexGamePalette.textPrimary
                )
                Text(
                    text = stringResource(R.string.selected_theme_value, stringResource(selectedTheme.titleRes)),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReflexGamePalette.textSecondary
                )
            }
        }
    }
}
