package com.example.todolistcomposed

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesWritingScreen(
    navController: NavController,
    viewModel: NotesWritingScreenViewModel = viewModel(),
    optionalTitle: String?
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (!optionalTitle.isNullOrEmpty()) {
                        Text(optionalTitle)
                        Log.d("optionalTitle","Optional title is $optionalTitle")
                    }
                    else
                        Text("Title of my note")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        Log.d("NotesWritingScreen", "Back arrow clicked!")
                        navController.navigateUp()
                    }) {
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
            Text("This could be your note")
            Spacer(modifier = Modifier.height(16.dp))
            // Add other UI elements
        }
    }
}