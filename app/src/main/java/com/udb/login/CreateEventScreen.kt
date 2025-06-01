@file:OptIn(ExperimentalMaterial3Api::class)

package com.udb.login

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun CreateEventScreen(navController: NavHostController) {
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val calendar = Calendar.getInstance()
    // Configurar fecha mínima: día siguiente
    calendar.add(Calendar.DAY_OF_MONTH, 1)
    val minYear = calendar.get(Calendar.YEAR)
    val minMonth = calendar.get(Calendar.MONTH)
    val minDay = calendar.get(Calendar.DAY_OF_MONTH)

    // DatePickerDialog con fecha mínima
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
            date = "$selectedYear-${(selectedMonth + 1).toString().padStart(2, '0')}-${selectedDay.toString().padStart(2, '0')}"
        },
        minYear,
        minMonth,
        minDay
    ).apply {
        // Establecer la fecha mínima permitida
        datePicker.minDate = calendar.timeInMillis
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Crear Evento") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título del evento") },
                modifier = Modifier.fillMaxWidth()
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePickerDialog.show() }
            ) {
                OutlinedTextField(
                    value = date,
                    onValueChange = {},
                    label = { Text("Fecha") },
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Ubicación") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (title.isNotBlank() && date.isNotBlank() && location.isNotBlank()) {
                        val event = hashMapOf(
                            "title" to title,
                            "date" to date,
                            "location" to location
                        )
                        db.collection("events").add(event).addOnSuccessListener {
                            message = "Evento creado correctamente"
                            // Limpiar campos después de crear el evento
                            title = ""
                            date = ""
                            location = ""
                            // Volver atrás después de un breve delay
                            scope.launch {
                                delay(1500)
                                navController.popBackStack()
                            }
                        }.addOnFailureListener {
                            message = "Error al guardar el evento"
                        }
                    } else {
                        message = "Completa todos los campos"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar evento")
            }

            message?.let {
                Text(text = it, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
