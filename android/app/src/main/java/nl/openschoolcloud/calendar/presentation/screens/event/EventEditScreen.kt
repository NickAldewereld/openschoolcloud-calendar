package nl.openschoolcloud.calendar.presentation.screens.event

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import nl.openschoolcloud.calendar.R

/**
 * Event edit/create screen
 *
 * This is a stub implementation for Sprint 1.
 * Full implementation will be completed in Sprint 2.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditScreen(
    eventId: String?,
    initialDate: String?,
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit
) {
    val isEditing = eventId != null
    val title = if (isEditing) {
        stringResource(R.string.event_edit)
    } else {
        stringResource(R.string.event_new)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.a11y_close)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSaved) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = stringResource(R.string.event_save)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isEditing) "Edit Event: $eventId" else "Create New Event",
                    style = MaterialTheme.typography.headlineMedium
                )
                if (initialDate != null) {
                    Text(
                        text = "Date: $initialDate",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = "Coming in Sprint 2",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
