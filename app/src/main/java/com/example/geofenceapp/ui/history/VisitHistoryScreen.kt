package com.example.geofenceapp.ui.history

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.geofenceapp.data.GeoDatabase
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitHistoryScreen(
    onBack: () -> Unit
) {
    // ✅ Handle physical back button
    BackHandler {
        onBack()
    }

    val context = LocalContext.current
    val db = remember { GeoDatabase.getInstance(context) }
    val visits by db.visitDao().getAllVisits()
        .collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Visit History") },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        if (visits.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No visits found")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                items(visits) { visit ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Location: ${visit.geofenceName}")
                            Spacer(Modifier.height(4.dp))
                            Text("Entry: ${formatTime(visit.entryTime)}")
                            Text("Exit: ${formatTime(visit.exitTime)}")
                            Text("Duration: ${visit.durationMillis / 1000} sec")
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(time: Long): String {
    val sdf = SimpleDateFormat("dd MMM, HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(time))
}
