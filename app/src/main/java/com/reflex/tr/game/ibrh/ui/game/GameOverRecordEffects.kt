package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.theme.ArcadeBlue
import com.reflex.tr.game.ibrh.ui.theme.ArcadeCoral
import com.reflex.tr.game.ibrh.ui.theme.ArcadeGold
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

@Composable
internal fun NewRecordBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(PremiumSurfaceRadius),
        color = ArcadeGold.copy(alpha = 0.18f),
        border = androidx.compose.foundation.BorderStroke(1.dp, ArcadeGold.copy(alpha = 0.54f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            ArcadeGold.copy(alpha = 0.18f),
                            ArcadeCoral.copy(alpha = 0.12f),
                            ArcadeBlue.copy(alpha = 0.12f)
                        )
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            NewRecordConfetti()
            Text(
                text = stringResource(R.string.new_record),
                style = MaterialTheme.typography.titleMedium,
                color = ReflexGamePalette.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
internal fun NewRecordConfetti() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val colors = listOf(ArcadeGold, ArcadeBlue, ArcadeCoral, Color.White)
        repeat(14) { index ->
            val x = size.width * (((index * 23) % 100) / 100f)
            val y = size.height * (((index * 41) % 100) / 100f)
            drawCircle(
                color = colors[index % colors.size].copy(alpha = 0.52f),
                radius = 2.2f + (index % 3),
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
        }
    }
}
