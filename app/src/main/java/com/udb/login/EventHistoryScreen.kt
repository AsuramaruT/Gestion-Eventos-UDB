@file:OptIn(ExperimentalMaterial3Api::class)

package com.udb.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.Alignment
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

@Composable
fun EventHistoryScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val adminEmail = "admin@example.com"
    var attendedEvents by remember { mutableStateOf<List<Triple<Event, Float?, List<Map<String, Any>>>>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(adminEmail) {
        try {
            val attendanceSnapshot = db.collection("event_attendance")
                .whereEqualTo("userEmail", adminEmail)
                .get()
                .await()

            val eventIds = attendanceSnapshot.documents.mapNotNull { it.getString("eventId") }

            val today = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

            val eventsList = mutableListOf<Triple<Event, Float?, List<Map<String, Any>>>>()
            for (eventId in eventIds) {
                val eventDoc = db.collection("events").document(eventId).get().await()
                if (eventDoc.exists()) {
                    val event = eventDoc.toObject(Event::class.java)?.copy(id = eventDoc.id)
                    if (event != null) {
                        val eventDate = try {
                            LocalDate.parse(event.date, formatter)
                        } catch (e: Exception) {
                            null
                        }
                        if (eventDate != null && eventDate.isBefore(today)) {
                            val feedbacks = db.collection("event_feedback")
                                .whereEqualTo("eventId", eventId).get().await()
                            val ratings = feedbacks.documents.mapNotNull { it.getDouble("rating")?.toFloat() }
                            val average = if (ratings.isNotEmpty()) ratings.average().toFloat() else null
                            val comments = feedbacks.documents.mapNotNull { doc ->
                                val data = doc.data
                                if (data != null) mapOf(
                                    "userEmail" to (data["userEmail"] as? String ?: ""),
                                    "comment" to (data["comment"] as? String ?: ""),
                                    "rating" to (data["rating"] as? Number ?: 0).toFloat()
                                ) else null
                            }
                            eventsList.add(Triple(event, average, comments))
                        }
                    }
                }
            }

            attendedEvents = eventsList
        } catch (e: Exception) {
            errorMessage = "Error al cargar historial: ${e.message}"
        } finally {
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Historial de Eventos") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .padding(16.dp)
        ) {
            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                Text(errorMessage ?: "", color = MaterialTheme.colorScheme.error)
            } else if (attendedEvents.isEmpty()) {
                Text("No hay eventos en el historial.", style = MaterialTheme.typography.bodyLarge)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(attendedEvents) { (event, rating, comments) ->
                        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
                            Column(Modifier.padding(16.dp)) {
                                Text(event.title, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(4.dp))
                                Text("\uD83D\uDCC5 ${event.date}")
                                Text("\uD83D\uDCCD ${event.location}")

                                if (rating != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("⭐ Puntuación: ${"%.1f".format(rating)}")
                                    Row {
                                        repeat(rating.toInt()) {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107))
                                        }
                                    }
                                }

                                if (comments.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Comentarios:", style = MaterialTheme.typography.labelLarge)
                                    comments.forEach { comment ->
                                        Text("- ${comment["userEmail"]}: ${comment["comment"]}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
