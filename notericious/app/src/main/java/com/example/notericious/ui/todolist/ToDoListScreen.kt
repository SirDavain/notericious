package com.example.notericious.ui.todolist

import android.app.Application
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.notericious.InputRow
import com.example.notericious.NotericiousApp
import com.example.notericious.TaskViewModel
import com.example.notericious.TaskViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToDoListScreen(
    navController: NavController,
    taskViewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory(
            LocalContext.current.applicationContext as Application,
            useInMemoryDb = false
        )
    ),
    optionalTitle: String?
) {
    val tasksUiState by taskViewModel.allTasks.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    // TRIGGER NETWORK SYNC ON INITIAL LOAD HERE
    LaunchedEffect(Unit) {
        taskViewModel.fetchTasksFromServer()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(optionalTitle ?: "My List") },
                navigationIcon = {
                    IconButton(onClick = {
                        Log.d("ToDoScreen", "Back arrow clicked!")
                        navController.navigateUp()
                    }) { // For back navigation
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            InputRow(
                newTaskText = taskViewModel.newTaskText,
                onNewTaskTextChange = { taskViewModel.onNewTaskTextChange(it) },
                onAddTask = {
                    // If a task is being edited, save it before adding a new one
                    if (taskViewModel.currentlyEditingTaskId != null) {
                        taskViewModel.saveOrDeleteCurrentEditedTask()
                    }
                    taskViewModel.addTaskToCurrentList()
                    focusManager.clearFocus() // Clear focus from any item being edited
                },
                isToDoItem = true
            )
        }
    ) { innerPadding ->
        NotericiousApp(
            modifier = Modifier.padding(innerPadding),
            tasksUiState = tasksUiState,
            taskViewModel = taskViewModel
        )
    }
}