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
package nl.openschoolcloud.calendar.presentation.screens.reflection

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.openschoolcloud.calendar.R
import nl.openschoolcloud.calendar.presentation.components.MoodSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReflectionPromptSheet(
    eventTitle: String,
    onDismiss: () -> Unit,
    onSave: (mood: Int, whatWentWell: String?, whatToDoBetter: String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedMood by remember { mutableIntStateOf(0) }
    var whatWentWell by remember { mutableStateOf("") }
    var whatToDoBetter by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = stringResource(R.string.reflection_title, eventTitle),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.reflection_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            MoodSelector(
                selectedMood = if (selectedMood > 0) selectedMood else null,
                onMoodSelected = { selectedMood = it }
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = whatWentWell,
                onValueChange = { whatWentWell = it },
                label = { Text(stringResource(R.string.reflection_what_went_well)) },
                placeholder = { Text(stringResource(R.string.reflection_what_went_well_hint)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = whatToDoBetter,
                onValueChange = { whatToDoBetter = it },
                label = { Text(stringResource(R.string.reflection_what_to_do_better)) },
                placeholder = { Text(stringResource(R.string.reflection_what_to_do_better_hint)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { onSave(selectedMood, whatWentWell, whatToDoBetter) },
                enabled = selectedMood > 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.reflection_save))
            }
        }
    }
}
