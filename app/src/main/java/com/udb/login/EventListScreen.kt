@file:OptIn(ExperimentalMaterial3Api::class)

package com.udb.login

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

data class Event(
    val id: String = "",
    val title: String = "",
    val date: String = "",
    val location: String = ""
)

@Composable
fun EventListScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    var futureEvents by remember { mutableStateOf(listOf<Event>()) }
    var pastEvents by remember { mutableStateOf(listOf<Event>()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(true) {
        try {
            val result = db.collection("events").get().await()
            val allEvents = result.documents.mapNotNull { doc ->
                val data = doc.data
                if (data != null) {
                    Event(
                        id = doc.id,
                        title = data["title"] as? String ?: "",
                        date = data["date"] as? String ?: "",
                        location = data["location"] as? String ?: ""
                    )
                } else null
            }

            val today = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            futureEvents = allEvents.filter {
                try {
                    LocalDate.parse(it.date, formatter).isAfter(today)
                } catch (e: Exception) {
                    false
                }
            }
            pastEvents = allEvents.filterNot { futureEvents.contains(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text("Eventos Futuros", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 8.dp))

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(futureEvents) { event ->
                    EventCard(event, navController, context)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Eventos Pasados", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(pastEvents) { event ->
                    EventCard(event, navController, context)
                }
            }
        }
    }
}

@Composable
fun EventCard(event: Event, navController: NavController, context: android.content.Context) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(event.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text("\uD83D\uDCC5 ${event.date}")
            Text("\uD83D\uDCCD ${event.location}")
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = {
                    navController.navigate(Screens.EventDetail.createRoute(event.id))
                }) {
                    Text("Ver detalles")
                }

                IconButton(onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "¡Mira este evento! \uD83D\uDCCC ${event.title} \n\uD83D\uDCC5 ${event.date}\n\uD83D\uDCCD ${event.location}"
                        )
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Compartir evento vía"))
                }) {
                    Icon(Icons.Default.Share, contentDescription = "Compartir")
                }
            }
        }
    }
}
