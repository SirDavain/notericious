package com.example.todolistcomposed

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.todolistcomposed.ui.mainscreen.MainScreen
import com.example.todolistcomposed.ui.notes.NotesWritingScreen
import com.example.todolistcomposed.ui.theme.ToDoListComposedTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ToDoListComposedTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = NavRoutes.MAIN_SCREEN
                ) {
                    composable(NavRoutes.MAIN_SCREEN) {
                        MainScreen(navController = navController)
                    }
                    composable(NavRoutes.TODO_LIST_SCREEN) {
                        ToDoScreenWithScaffold(
                            navController = navController,
                            taskViewModel = viewModel(
                                factory = TaskViewModelFactory(
                                    LocalContext.current.applicationContext as Application,
                                    useInMemoryDb = false
                                )
                            ),
                            // Pass a lambda to trigger navigation
                            /*onNavigateToNewScreen = {
                                Log.d("NavHost", "Navigating from ToDoScreen to MainScreen triggered")
                                navController.navigate(NavRoutes.MAIN_SCREEN)
                            }*/
                        )
                    }
                    /*composable(NavRoutes.NOTES_WRITING_SCREEN) {
                        NotesWritingScreen(navController = navController)
                    }*/
                    composable(
                        route = NavRoutes.NOTES_WRITING_SCREEN,
                        arguments = listOf(
                            navArgument(NavRoutes.NOTES_TITLE_ARG) {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            }
                        )
                    ) { backStageEntry ->
                        val title = backStageEntry.arguments?.getString(NavRoutes.NOTES_TITLE_ARG)
                        NotesWritingScreen(
                            navController = navController,
                            optionalTitle = title ?: ""
                        )
                    }
                }
            }
        }
    }
}

//@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ToDoListApp(
    tasksUiState: List<TaskUiState>,
    taskViewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val currentlyEditingId = taskViewModel.currentlyEditingTaskId
    val currentEditText = taskViewModel.currentEditText

    Log.d("ToDoListAppRecomp", "Recomposing. currentlyEditingId: $currentlyEditingId, currentEditText: '$currentEditText'")
    // This makes the whole list area clickable to clear focus from an editing item
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null // No visual indication for this background click
        ) {
            // If a task is being edited, clicking outside saves and clears focus
            Log.d("ToDoListApp", "Outer Column clicked. currentlyEditingId: $currentlyEditingId") // Add this log
            if (currentlyEditingId != null) {
                Log.d("ToDoListApp", "Outer Column click - calling saveOrDeleteCurrentEditedTask")
                taskViewModel.saveOrDeleteCurrentEditedTask() // This will also clear currentlyEditingTaskId
            }
            focusManager.clearFocus() // Clear focus from any TextField
    }) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize() // Will be constrained by parent Column
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .weight(1f)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            items(
                items = tasksUiState,
                key = { task -> task.id }
            ) { task ->
                TaskItem(
                    taskState = task,
                    isBeingEdited = task.id == currentlyEditingId,
                    currentEditTextValue = if (task.id == currentlyEditingId) currentEditText else task.text,
                    onDoneChange = { newDoneState ->
                        taskViewModel.updateTaskDoneStatus(task.id, newDoneState)
                    },
                    modifier = Modifier.animateItem(),
                    onTextClick = {
                        Log.d("ToDoListApp", "onTextClick for task ID: ${task.id}. currentEditingId before: ${taskViewModel.currentlyEditingTaskId}")
                        if (taskViewModel.currentlyEditingTaskId != null && taskViewModel.currentlyEditingTaskId != task.id) {
                            Log.d("ToDoListApp", "Saving previously edited task: ${taskViewModel.currentlyEditingTaskId}")
                            taskViewModel.saveOrDeleteCurrentEditedTask()
                        }
                        taskViewModel.startEditingTask(task)
                        Log.d("ToDoListApp", "onTextClick - currentEditingId after startEditingTask: ${taskViewModel.currentlyEditingTaskId}")
                    },
                    onEditTextChange = { newText ->
                        taskViewModel.onCurrentEditTextChange(newText)
                    },
                    onEditDone = {
                        taskViewModel.saveOrDeleteCurrentEditedTask()
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)
            }
            item { Spacer(modifier = Modifier.height(80.dp)) } // For bottom input bar
        }
    }
}

@Composable
fun TaskItem(
    taskState: TaskUiState,
    isBeingEdited: Boolean,
    currentEditTextValue: String,
    onDoneChange: (Boolean) -> Unit,
    onTextClick: () -> Unit,
    onEditTextChange: (String) -> Unit,
    onEditDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var hasBeenFocused by remember { mutableStateOf(false) }

    val textAlpha by animateFloatAsState(
        targetValue = if (taskState.isDone) 0.7f else 1.0f,
        animationSpec = tween(durationMillis = 300),
        label = "TaskItemTextAlphaAnimation"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = taskState.isDone,
            onClick = {
                if (isBeingEdited)
                    onEditDone()
                onDoneChange(!taskState.isDone)
            },
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary, // Or your desired color
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        if (isBeingEdited) {
            TextField(
                value = currentEditTextValue,
                onValueChange = onEditTextChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            Log.d("TaskItemFocus", "Focus gained for ID: ${taskState.id}")
                            hasBeenFocused = true // Mark that it has gained focus
                        } else {
                            // Only call onEditDone if it had been focused before losing focus
                            if (hasBeenFocused) {
                                Log.d(
                                    "TaskItemFocus",
                                    "Focus lost for ID: ${taskState.id} AFTER being focused. Calling onEditDone."
                                )
                                onEditDone()
                            } else {
                                Log.d(
                                    "TaskItemFocus",
                                    "Focus lost for ID: ${taskState.id} WITHOUT prior focus. Ignoring."
                                )
                            }
                        }
                    }
                    .onKeyEvent { keyEvent -> // Handle Enter/Done key
                        if (keyEvent.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_ENTER ||
                            keyEvent.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_NUMPAD_ENTER
                        ) {
                            if (keyEvent.nativeKeyEvent.action == android.view.KeyEvent.ACTION_UP) { // On key up
                                onEditDone()
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                return@onKeyEvent true
                            }
                        }
                        false
                    },
                textStyle = LocalTextStyle.current.copy( // Match the Text style
                    fontSize = 16.sp,
                    color = if (taskState.isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                            else MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.Sentences
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onEditDone()
                        //keyboardController?.hide()
                        //focusManager.clearFocus()
                    }
                ),
                singleLine = false, // Allow multi-line editing
                maxLines = 5,        // Limit lines
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,    // No underline when focused
                    unfocusedIndicatorColor = Color.Transparent,  // No underline when not focused
                    disabledIndicatorColor = Color.Transparent,   // No underline when disabled
                ),
            )
            LaunchedEffect(Unit) { // Request focus when this TextField becomes visible
                Log.d("TaskItemFocus", "LaunchedEffect: Requesting focus for ID: ${taskState.id}")
                focusRequester.requestFocus()
                //keyboardController?.show()
            }
        } else {
            // Reset hasBeenFocused when not in edit mode so it's fresh for next edit
            LaunchedEffect(isBeingEdited) { // Or LaunchedEffect(Unit)
                if (!isBeingEdited) {
                    hasBeenFocused = false
                }
            }
            Text(
                text = taskState.text,
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        onClick = {
                            Log.d("TaskItem", "Text clicked for task ID: ${taskState.id}")
                            onTextClick()
                        },
                    )
                    .alpha(if (taskState.isDone) textAlpha else 1.0f),
                fontSize = 16.sp,
                color = if (taskState.isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun InputRow(
    newTaskText: String,
    onNewTaskTextChange: (String) -> Unit,
    onAddTask: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = newTaskText,
            onValueChange = onNewTaskTextChange,
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(minHeight = 48.dp),
            placeholder = { Text("Add a new task...") },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onAddTask() }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
            ),
            shape = CircleShape
        )
        Spacer(modifier = Modifier.width(30.dp))

        // I want it to control if we're creating a task or a note
        // i.e. after typing in smth into the input bar, after pressing the FAB it should give
        // you an option to create a task or a note.
        FloatingActionButton(
            onClick = { onAddTask() },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,

            shape = CircleShape,
        ) {
            Log.d("ToDoScreen", "FAB for adding a task clicked!")
            Icon(Icons.Filled.Add, contentDescription = "Add new task or go to new screen")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TodoListPreview() {
    ToDoListComposedTheme(darkTheme = true) {
        // Approach 3: Simplest for UI preview if ViewModel is complex - Pass fake data directly
        val sampleTasks = listOf(
            TaskUiState(1, "Buy groceries", false),
            TaskUiState(2, "Walk the dog", false),
            TaskUiState(3, "Read a book", true)
        )
        val dummyViewModel = remember { // Create a dummy view model that doesn't rely on real dependencies
            object { // This is not a real TaskViewModel, just something with a compatible interface for the preview
                val allTasks =
                    MutableStateFlow(sampleTasks) // Use MutableStateFlow for simplicity in dummy
                val newTaskText = ""
                val currentlyEditingTaskId: Int? = null
                val currentEditText = ""
                fun onNewTaskTextChange(s: String) {}
                fun insertNewTask() {}
                fun updateTaskDoneStatus(id: Int, b: Boolean) {}
                fun startEditingTask(t: TaskUiState) {}
                fun onCurrentEditTextChange(s: String) {}
                fun saveOrDeleteCurrentEditedTask() {}
            }
        }
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                InputRow(newTaskText = "New preview task", onNewTaskTextChange = {}, onAddTask = {})
            }
        ) { paddingValues ->
            ToDoListApp(
                tasksUiState = sampleTasks, // If allTasks in dummy VM is just this static list for preview
                // Or collectAsStateWithLifecycle from the remembered MutableStateFlow
                taskViewModel = remember { // Remember the entire anonymous ViewModel object
                    object : TaskViewModel(Application(), TaskRepository(FakeTaskDaoForPreview())) {
                        override val allTasks: MutableStateFlow<List<TaskUiState>> =
                            MutableStateFlow(sampleTasks) // This is fine if sampleTasks is stable (remembered or const)
                        // Or if you want the flow to be part of the remembered VM state:
                        // override val allTasks = MutableStateFlow(sampleTasks)

                        override var newTaskText: String by mutableStateOf("Preview new task")
                        override var currentlyEditingTaskId: Int? by mutableStateOf(null)
                        override var currentEditText: String by mutableStateOf("")

                        // Dummy implementations
                        override fun onNewTaskTextChange(newText: String) {
                            // If you want preview interaction:
                            // newTaskText = newText
                            // allTasks.value = allTasks.value // Trigger recomposition if needed
                        }
                        override fun insertNewTask() {
                            // if (newTaskText.isNotBlank()) {
                            // val newId = (allTasks.value.maxOfOrNull { it.id } ?: 0) + 1
                            // val newTask = TaskUiState(newId, newTaskText, false)
                            // allTasks.value = allTasks.value + newTask
                            // newTaskText = ""
                            // }
                        }
                        override fun updateTaskDoneStatus(taskId: Int, newDoneState: Boolean) {
                            // allTasks.value = allTasks.value.map {
                            // if (it.id == taskId) it.copy(isDone = newDoneState) else it
                            // }
                        }
                        override fun startEditingTask(task: TaskUiState) {
                            // currentlyEditingTaskId = task.id
                            // currentEditText = task.text
                        }
                        override fun onCurrentEditTextChange(newText: String) {
                            // currentEditText = newText
                        }
                        override fun saveOrDeleteCurrentEditedTask() {
                            // val id = currentlyEditingTaskId
                            // if (id != null) {
                            // if (currentEditText.isBlank()) {
                            // allTasks.value = allTasks.value.filterNot { it.id == id }
                            // } else {
                            // allTasks.value = allTasks.value.map {
                            // if (it.id == id) it.copy(text = currentEditText) else it
                            // }
                            // }
                            // }
                            // currentlyEditingTaskId = null
                            // currentEditText = ""
                        }
                    }
                },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

class FakeTaskDaoForPreview : TaskDao {
    override fun getAllTasks(): Flow<List<Task>> = MutableStateFlow(emptyList())

    override suspend fun insertTask(task: com.example.todolistcomposed.Task) {
        // No-op for preview
    }

    override suspend fun updateTask(task: com.example.todolistcomposed.Task) {
        // No-op for preview
    }

    override suspend fun updateTaskDoneStatusAndTimestamp(taskId: Int, isDone: Boolean, timestamp: Long) {
        // No-op for preview.
        // In a more interactive preview, you might modify an in-memory list here
        // if your preview ViewModel actually used this DAO to manipulate data.
        // For now, doing nothing is fine.
    }

    override suspend fun getTaskById(taskId: Int): com.example.todolistcomposed.Task? = null
    override suspend fun deleteTaskById(taskId: Int) { /* No-op */ }
}