/*
 * OSC Calendar - Privacy-first calendar for Dutch education
 * Copyright (C) 2025 Aldewereld Consultancy (OpenSchoolCloud)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 */
package nl.openschoolcloud.calendar.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import nl.openschoolcloud.calendar.R

@Composable
fun MoodSelector(
    selectedMood: Int?,
    onMoodSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val moods = listOf(
        1 to Pair("\uD83D\uDE2B", R.string.reflection_mood_1),
        2 to Pair("\uD83D\uDE15", R.string.reflection_mood_2),
        3 to Pair("\uD83D\uDE10", R.string.reflection_mood_3),
        4 to Pair("\uD83D\uDE42", R.string.reflection_mood_4),
        5 to Pair("\uD83E\uDD29", R.string.reflection_mood_5)
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        moods.forEach { (value, emojiAndLabel) ->
            val (emoji, labelRes) = emojiAndLabel
            val isSelected = selectedMood == value

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else Color.Transparent
                    )
                    .clickable { onMoodSelected(value) }
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = stringResource(labelRes),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
