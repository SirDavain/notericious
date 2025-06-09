package com.example.todolistcomposed.ui.mainscreen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.runtime.getValue
import com.example.todolistcomposed.MainScreenViewModel
import android.util.Log
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
                title = { Text("Main Screen") },
                navigationIcon = {
                    IconButton(onClick = {
                        Log.d("MainScreen", "Back arrow clicked!")
                        navController.navigateUp()
                    }) { // For back navigation
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
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
                Log.d("MainScreen", "Navigating to ToDoListScreen from MainScreen button")
                navController.navigate(NavRoutes.TODO_LIST_SCREEN)
            }) {
                Text("Go to ToDolist")
            }
            // Add other UI elements for your new screen
        }
    }
}