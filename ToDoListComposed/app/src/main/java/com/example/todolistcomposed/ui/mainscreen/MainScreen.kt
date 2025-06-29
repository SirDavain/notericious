package com.example.todolistcomposed.ui.mainscreen

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.todolistcomposed.MainScreenViewModel
import com.example.todolistcomposed.NavRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainScreenViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle() // Collect state

    // ... use uiState in your UI ...
    // Button(onClick = { viewModel.performSomeAction() }) { Text("Perform Action") }
    // if (uiState.isLoading) { CircularProgressIndicator() }
    // Text(uiState.data ?: "No data")
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Main Screen") }
            )
        },
        /*bottomBar = {
            InputRow(
                newTaskText = taskViewModel.newTaskText,
                onNewTaskTextChange = { taskViewModel.onNewTaskTextChange(it) },
                onAddTask = {
                    // If a task is being edited, save it before adding a new one
                    if (taskViewModel.currentlyEditingTaskId != null) {
                        taskViewModel.saveOrDeleteCurrentEditedTask()
                    }
                    taskViewModel.insertNewTask()
                    focusManager.clearFocus() // Clear focus from any item being edited
                }
            )
        },*/
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("This is the Main Screen!")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                Log.d("Navigation", "Navigating to ToDoListScreen from MainScreen button")
                navController.navigate(NavRoutes.TODO_LIST_SCREEN)
            }) {
                Text("Go to ToDolist")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                val titleToSend: String? = null
                Log.d("Navigation", "Navigating to NotesWritingScreen with title $titleToSend")
                navController.navigate(NavRoutes.notesWritingScreenWithOptionalTitle(titleToSend))
            }) {
                Text("Go to note-taking screen")
            }
        }
    }
}