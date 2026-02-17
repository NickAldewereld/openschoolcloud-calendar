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
package nl.openschoolcloud.calendar.presentation.screens.planning

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import nl.openschoolcloud.calendar.R
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekPlanningScreen(
    onNavigateBack: () -> Unit,
    viewModel: WeekPlanningViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.planning_title))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.a11y_close)
                        )
                    }
                },
                actions = {
                    Text(
                        text = stringResource(R.string.planning_week_number, uiState.weekNumber),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Step indicator
            StepIndicator(
                currentStep = uiState.step,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            when {
                uiState.isComplete -> CompletionContent(
                    streak = uiState.planningStreak,
                    onDone = onNavigateBack
                )
                else -> when (uiState.step) {
                    PlanningStep.ADD_TASKS -> AddTasksStep(
                        tasks = uiState.tasks,
                        onAddTask = viewModel::addTask,
                        onRemoveTask = viewModel::removeTask,
                        onUpdateTitle = viewModel::updateTaskTitle,
                        onNext = viewModel::nextStep,
                        hasTasksWithText = uiState.hasTasksWithText
                    )
                    PlanningStep.ASSIGN_DAYS -> AssignDaysStep(
                        tasks = uiState.tasks,
                        weekDays = uiState.weekDays,
                        onAssignDay = viewModel::assignTaskToDay,
                        onPrevious = viewModel::previousStep,
                        onNext = viewModel::nextStep
                    )
                    PlanningStep.CONFIRM -> ConfirmStep(
                        tasks = uiState.tasks,
                        weekDays = uiState.weekDays,
                        tasksByDay = uiState.tasksByDay,
                        isSaving = uiState.isSaving,
                        onPrevious = viewModel::previousStep,
                        onConfirm = viewModel::savePlan
                    )
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(
    currentStep: PlanningStep,
    modifier: Modifier = Modifier
) {
    val steps = PlanningStep.entries
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            val isActive = step.ordinal <= currentStep.ordinal
            val label = when (step) {
                PlanningStep.ADD_TASKS -> stringResource(R.string.planning_step_tasks)
                PlanningStep.ASSIGN_DAYS -> stringResource(R.string.planning_step_days)
                PlanningStep.CONFIRM -> stringResource(R.string.planning_step_confirm)
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (step == currentStep) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (index < steps.lastIndex) {
                Text(
                    text = " — ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AddTasksStep(
    tasks: List<PlanningTask>,
    onAddTask: () -> Unit,
    onRemoveTask: (String) -> Unit,
    onUpdateTitle: (String, String) -> Unit,
    onNext: () -> Unit,
    hasTasksWithText: Boolean
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.planning_add_tasks_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.planning_add_tasks_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(tasks, key = { _, task -> task.id }) { index, task ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = task.title,
                        onValueChange = { onUpdateTitle(task.id, it) },
                        modifier = Modifier.weight(1f),
                        label = {
                            Text(stringResource(R.string.planning_task_label, index + 1))
                        },
                        singleLine = true
                    )
                    if (tasks.size > 1) {
                        IconButton(onClick = { onRemoveTask(task.id) }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.a11y_delete),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (tasks.size < 10) {
                item {
                    TextButton(onClick = onAddTask) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.planning_add_task))
                    }
                }
            }
        }

        Button(
            onClick = onNext,
            enabled = hasTasksWithText,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text(stringResource(R.string.planning_next))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AssignDaysStep(
    tasks: List<PlanningTask>,
    weekDays: List<LocalDate>,
    onAssignDay: (String, LocalDate) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.planning_assign_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.planning_assign_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tasks.filter { it.title.isNotBlank() }, key = { it.id }) { task ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            weekDays.forEach { day ->
                                val isSelected = task.assignedDay == day
                                val dayLabel = day.dayOfWeek.getDisplayName(
                                    TextStyle.SHORT, Locale.getDefault()
                                )
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onAssignDay(task.id, day) },
                                    label = { Text(dayLabel) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onPrevious,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.planning_previous))
            }
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.planning_next))
            }
        }
    }
}

@Composable
private fun ConfirmStep(
    tasks: List<PlanningTask>,
    weekDays: List<LocalDate>,
    tasksByDay: Map<LocalDate, List<PlanningTask>>,
    isSaving: Boolean,
    onPrevious: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.planning_confirm_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            weekDays.forEach { day ->
                val dayTasks = tasksByDay[day] ?: emptyList()
                val unassigned = if (day == weekDays.first()) {
                    tasks.filter { it.title.isNotBlank() && it.assignedDay == null }
                } else emptyList()
                val allTasks = dayTasks + unassigned

                if (allTasks.isNotEmpty()) {
                    item(key = "day_${day}") {
                        val dayLabel = day.dayOfWeek.getDisplayName(
                            TextStyle.FULL, Locale.getDefault()
                        )
                        Text(
                            text = "$dayLabel ${day.dayOfMonth}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(allTasks, key = { "confirm_${it.id}" }) { task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp, top = 2.dp, bottom = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onPrevious,
                enabled = !isSaving,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.planning_previous))
            }
            Button(
                onClick = onConfirm,
                enabled = !isSaving,
                modifier = Modifier.weight(1f)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.planning_confirm))
                }
            }
        }
    }
}

@Composable
private fun CompletionContent(
    streak: Int,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = true,
            enter = scaleIn() + fadeIn()
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.planning_complete_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.planning_complete_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (streak > 0) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.planning_streak, streak),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onDone) {
            Text(stringResource(R.string.planning_done))
        }
    }
}
