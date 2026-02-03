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
    /* ---------------------- Back Navigation ---------------------- */

    BackHandler {
        onBack()
    }

    /* ------------------------ Data ------------------------------- */

    val context = LocalContext.current
    val database = remember { GeoDatabase.getInstance(context) }

    val visits by database.visitDao()
        .getAllVisits()
        .collectAsState(initial = emptyList())

    /* ------------------------- UI ------------------------------- */

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Visit History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        if (visits.isEmpty()) {
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            VisitList(
                visits = visits,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}

/* ----------------------- Components ---------------------------- */

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(text = "No visits found")
    }
}

@Composable
private fun VisitList(
    visits: List<com.example.geofenceapp.data.entity.VisitEntity>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp)
    ) {
        items(visits) { visit ->
            VisitItem(visit)
        }
    }
}

@Composable
private fun VisitItem(
    visit: com.example.geofenceapp.data.entity.VisitEntity
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "Location: ${visit.geofenceName}")
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Entry: ${formatTime(visit.entryTime)}")
            Text(text = "Exit: ${formatTime(visit.exitTime)}")
            Text(text = "Duration: ${visit.durationMillis / 1000} sec")
        }
    }
}

/* ----------------------- Utilities ----------------------------- */

private fun formatTime(timeMillis: Long): String {
    val formatter = SimpleDateFormat(
        "dd MMM, HH:mm:ss",
        Locale.getDefault()
    )
    return formatter.format(Date(timeMillis))
}
