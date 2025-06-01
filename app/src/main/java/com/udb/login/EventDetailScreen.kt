@file:OptIn(ExperimentalMaterial3Api::class)
package com.udb.login

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

@Composable
fun EventDetailScreen(eventId: String, navController: NavHostController) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser

    var event by remember { mutableStateOf<Event?>(null) }
    var loading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf<String?>(null) }
    var isPastEvent by remember { mutableStateOf(false) }

    // Obtener datos del evento desde Firestore
    LaunchedEffect(eventId) {
        firestore.collection("events").document(eventId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val fetchedEvent = document.toObject<Event>()?.copy(id = document.id)
                    event = fetchedEvent

                    fetchedEvent?.let {
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        try {
                            val eventDate = LocalDate.parse(it.date, formatter)
                            isPastEvent = eventDate.isBefore(LocalDate.now())
                        } catch (_: Exception) {}
                    }
                }
                loading = false
            }
            .addOnFailureListener {
                message = "Error al cargar el evento"
                loading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Detalle del Evento") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            if (loading) {
                CircularProgressIndicator()
            } else if (event != null) {
                Text("\uD83D\uDCCC Título", style = MaterialTheme.typography.labelMedium)
                Text(event!!.title, style = MaterialTheme.typography.titleLarge)

                Text("\uD83D\uDCC5 Fecha", style = MaterialTheme.typography.labelMedium)
                Text(event!!.date, style = MaterialTheme.typography.titleMedium)

                Text("\uD83D\uDCCD Ubicación", style = MaterialTheme.typography.labelMedium)
                Text(event!!.location, style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (user != null) {
                            val attendance = hashMapOf(
                                "eventId" to eventId,
                                "userEmail" to user.email,
                                "timestamp" to System.currentTimeMillis()
                            )
                            firestore.collection("event_attendance")
                                .add(attendance)
                                .addOnSuccessListener {
                                    message = "¡Tu asistencia ha sido registrada!"
                                }
                                .addOnFailureListener {
                                    message = "Error al registrar asistencia"
                                }
                        } else {
                            message = "Debes iniciar sesión para confirmar asistencia"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("✅ Asistiré")
                }

                if (isPastEvent) {
                    Divider()

                    Text("Tu calificación", style = MaterialTheme.typography.labelLarge)
                    var rating by remember { mutableStateOf(0f) }
                    Slider(
                        value = rating,
                        onValueChange = { rating = it },
                        valueRange = 0f..5f,
                        steps = 4
                    )

                    var comment by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Comentario") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(onClick = {
                        if (user != null && rating > 0f) {
                            val review = hashMapOf(
                                "eventId" to eventId,
                                "userEmail" to user.email,
                                "rating" to rating,
                                "comment" to comment,
                                "timestamp" to System.currentTimeMillis()
                            )
                            firestore.collection("event_reviews")
                                .add(review)
                                .addOnSuccessListener {
                                    message = "¡Comentario enviado!"
                                }
                                .addOnFailureListener {
                                    message = "Error al enviar comentario"
                                }
                        } else {
                            message = "Ingresa una calificación válida"
                        }
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("Enviar comentario")
                    }
                }

                Button(
                    onClick = {
                        navController.navigate(Screens.Home.route) {
                            popUpTo(Screens.Home.route) { inclusive = false }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver más eventos")
                }

                message?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = it, color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Text("Evento no encontrado", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
