@file:OptIn(ExperimentalMaterial3Api::class)

package com.udb.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun UserInitialsAvatar(nameOrEmail: String, modifier: Modifier = Modifier) {
    val initial = nameOrEmail.trim().firstOrNull()?.uppercaseChar() ?: '?'
    val backgroundColor = remember(initial) {
        when (initial) {
            in 'A'..'F' -> Color(0xFFEF5350) // rojo
            in 'G'..'L' -> Color(0xFFAB47BC) // morado
            in 'M'..'R' -> Color(0xFF42A5F5) // azul
            in 'S'..'Z' -> Color(0xFF26A69A) // verde
            else -> Color.Gray
        }
    }
    Surface(
        modifier = modifier
            .size(96.dp)
            .clip(CircleShape),
        color = backgroundColor,
        tonalElevation = 8.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = initial.toString(),
                style = MaterialTheme.typography.displaySmall.copy(color = Color.White)
            )
        }
    }
}

@Composable
fun ProfileScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val email = user?.email ?: "Correo no disponible"
    var message by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Perfil del Usuario") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UserInitialsAvatar(nameOrEmail = email)

            Text("Correo electrónico", style = MaterialTheme.typography.labelLarge)
            Text(email, style = MaterialTheme.typography.titleLarge)

            Divider(modifier = Modifier.fillMaxWidth())

            Button(
                onClick = {
                    if (user != null) {
                        isLoading = true
                        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                            isLoading = false
                            message = if (task.isSuccessful) {
                                "Correo para restablecer contraseña enviado a $email"
                            } else {
                                "Error al enviar correo: ${task.exception?.message ?: "Desconocido"}"
                            }
                        }
                    } else {
                        message = "No hay usuario autenticado"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Enviando..." else "Cambiar contraseña")
            }

            message?.let {
                Text(
                    text = it,
                    color = if (it.startsWith("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    auth.signOut()
                    navController.navigate(Screens.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Cerrar sesión", color = Color.White)
            }
        }
    }
}
