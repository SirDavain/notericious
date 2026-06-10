package com.example.notericious.ui.todolist

import android.R.attr.navigationIcon
import android.app.Application
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
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
    viewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory(
            LocalContext.current.applicationContext as Application,
            useInMemoryDb = false
        )
    )
) {
    val tasksUiState by viewModel.allTasks.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val title by viewModel.listTitle.collectAsStateWithLifecycle()

    // TRIGGER NETWORK SYNC ON INITIAL LOAD HERE
    LaunchedEffect(Unit) {
        viewModel.fetchTasksFromServer()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    BasicTextField(
                        value = title,
                        onValueChange = { viewModel.updateListTitle(it) },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            if (title.isEmpty()) {
                                Text(
                                    text = "Untitled List",
                                    style = LocalTextStyle.current.copy(
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                            innerTextField()
                        }
                    )
                },
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
                newTaskText = viewModel.newTaskText,
                onNewTaskTextChange = { viewModel.onNewTaskTextChange(it) },
                onAddTask = {
                    // If a task is being edited, save it before adding a new one
                    if (viewModel.currentlyEditingTaskId != null) {
                        viewModel.saveOrDeleteCurrentEditedTask()
                    }
                    viewModel.addTaskToCurrentList()
                    focusManager.clearFocus() // Clear focus from any item being edited
                },
                isToDoItem = true
            )
        }
    ) { innerPadding ->
        NotericiousApp(
            modifier = Modifier.padding(innerPadding),
            tasksUiState = tasksUiState,
            taskViewModel = viewModel
        )
    }
}