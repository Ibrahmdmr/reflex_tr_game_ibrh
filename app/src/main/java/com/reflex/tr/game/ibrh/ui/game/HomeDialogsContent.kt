package com.reflex.tr.game.ibrh.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reflex.tr.game.ibrh.R
import com.reflex.tr.game.ibrh.ui.game.components.PrimaryGameButton
import com.reflex.tr.game.ibrh.ui.game.components.SecondaryGameButton
import com.reflex.tr.game.ibrh.ui.theme.ArcadeCoral
import com.reflex.tr.game.ibrh.ui.theme.ArcadeTeal
import com.reflex.tr.game.ibrh.ui.theme.ReflexGamePalette

@Composable
internal fun LevelUpPopup(
    level: Int,
    coinBonus: Int,
    onDismiss: () -> Unit
) {
    PolishedGameDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            PrimaryGameButton(
                text = stringResource(R.string.ok),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        },
        title = stringResource(R.string.level_up_value, level)
    ) {
        Text(
            text = stringResource(R.string.level_up_bonus_message, coinBonus),
            modifier = Modifier.fillMaxWidth(),
            color = ReflexGamePalette.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
internal fun PlayerNameDialog(
    currentName: String,
    hasCurrentName: Boolean,
    onSave: (String) -> Boolean,
    onDismiss: () -> Unit
) {
    val playerNameSuggestionsArray = stringArrayResource(R.array.player_name_suggestions)
    val playerNameSuggestions = remember(playerNameSuggestionsArray.contentHashCode()) {
        playerNameSuggestionsArray.toList()
    }
    var name by remember(currentName, hasCurrentName) {
        mutableStateOf(if (hasCurrentName) currentName else playerNameSuggestions.random())
    }
    var hasError by remember { mutableStateOf(false) }
    val titleText = stringResource(R.string.player_name_dialog_title)
    val descriptionText = stringResource(R.string.player_name_dialog_description)
    val hintText = stringResource(R.string.player_name_dialog_hint)
    val saveText = stringResource(R.string.player_name_save)
    val errorText = stringResource(R.string.player_name_error)
    val randomNameText = stringResource(R.string.player_name_random)
    val suggestionsText = stringResource(R.string.player_name_suggestions)

    PolishedGameDialog(
        onDismissRequest = onDismiss,
        title = titleText,
        confirmButton = {
            PrimaryGameButton(
                text = saveText,
                onClick = {
                    val candidate = name.trim().take(PLAYER_NAME_MAX_LENGTH)
                    hasError = candidate.isBlank() || !onSave(candidate)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = descriptionText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ReflexGamePalette.textSecondary
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it.take(PLAYER_NAME_MAX_LENGTH)
                        hasError = false
                    },
                    singleLine = true,
                    isError = hasError,
                    label = {
                        Text(text = hintText)
                    },
                    supportingText = {
                        Text(
                            text = stringResource(R.string.player_name_character_count, name.length, PLAYER_NAME_MAX_LENGTH),
                            color = ReflexGamePalette.textSecondary
                        )
                    }
                )
                if (hasError) {
                    Text(
                        text = errorText,
                        style = MaterialTheme.typography.bodySmall,
                        color = ArcadeCoral
                    )
                }
                Text(
                    text = suggestionsText,
                    style = MaterialTheme.typography.labelMedium,
                    color = ReflexGamePalette.textSecondary
                )
                playerNameSuggestions.chunked(3).forEach { rowSuggestions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowSuggestions.forEach { suggestion ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        name = suggestion
                                        hasError = false
                                    },
                                color = if (name == suggestion) ArcadeTeal.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(999.dp),
                                border = BorderStroke(1.dp, ArcadeTeal.copy(alpha = if (name == suggestion) 0.52f else 0.22f))
                            ) {
                                Text(
                                    text = suggestion,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ReflexGamePalette.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                SecondaryGameButton(
                    text = randomNameText,
                    onClick = {
                        name = playerNameSuggestions.random()
                        hasError = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
    }
}
