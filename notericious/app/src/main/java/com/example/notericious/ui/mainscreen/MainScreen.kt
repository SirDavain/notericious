package com.example.notericious.ui.mainscreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.notericious.InputRow
import com.example.notericious.NavRoutes
import com.example.notericious.TaskUiState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.notericious.ui.theme.NotericiousTheme

@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainScreenViewModel = hiltViewModel()
) {
    val tasks by viewModel.allTasks.collectAsState()

    MainScreenContent(
        tasks = tasks,
        newTaskText = viewModel.newTaskText,
        onNewTaskTextChange = { viewModel.onNewTaskTextChange(it) },
        onTaskClick = { task ->
            if (task.isNote) {
                navController.navigate(NavRoutes.notesWritingScreenWithOptionalTitle(task.text))
            } else {
                navController.navigate(NavRoutes.TODO_LIST_SCREEN)
            }
        },
        onNewListClick = { 
            if (viewModel.newTaskText.isNotBlank()) {
                viewModel.insertNewTask(isNote = false)
            } else {
                navController.navigate(NavRoutes.TODO_LIST_SCREEN)
            }
        },
        onNewNoteClick = { 
            if (viewModel.newTaskText.isNotBlank()) {
                viewModel.insertNewTask(isNote = true)
            } else {
                navController.navigate(NavRoutes.notesWritingScreenWithOptionalTitle(null))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    tasks: List<TaskUiState>,
    newTaskText: String,
    onNewTaskTextChange: (String) -> Unit,
    onTaskClick: (TaskUiState) -> Unit,
    onNewListClick: () -> Unit,
    onNewNoteClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("presenting: notericious") }
            )
        },
        bottomBar = {
            InputRow(
                newTaskText = newTaskText,
                onNewTaskTextChange = onNewTaskTextChange,
                onAddTask = {
                    // This is still called by keyboard 'Done' or can be used as 'Default'
                    // For now, let's make it trigger the New Note action as a default
                    onNewNoteClick()
                },
                onNewListClick = onNewListClick,
                onNewNoteClick = onNewNoteClick
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (tasks.isEmpty()) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No notes or lists yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Type a title below to get started",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    items(tasks) { task ->
                        TaskSummaryItem(
                            task = task,
                            onClick = { onTaskClick(task) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
fun TaskSummaryItem(
    task: TaskUiState,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = task.text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (task.isNote) "Note" else "To-Do List",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    NotericiousTheme {
        val sampleTasks = listOf(
            TaskUiState(1, "Buy groceries", false, isNote = false),
            TaskUiState(2, "Walk the dog", false, isNote = false),
            TaskUiState(3, "Read a book", true, isNote = true)
        )
        MainScreenContent(
            tasks = sampleTasks,
            newTaskText = "New note title",
            onNewTaskTextChange = {},
            onTaskClick = {},
            onNewListClick = {},
            onNewNoteClick = {}
        )
    }
}
