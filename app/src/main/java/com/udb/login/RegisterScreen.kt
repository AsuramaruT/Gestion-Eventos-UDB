package com.udb.login

import android.app.Activity
import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun RegisterScreen(
    navController: NavController,
    activity: Activity,
    auth: FirebaseAuth
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isValidEmail by remember { mutableStateOf(false) }
    var isValidPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        if (errorMessage.isNotBlank()) {
            snackbarHostState.showSnackbar(errorMessage)
            errorMessage = ""
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF8EC5FC), Color(0xFFE0C3FC))
                    )
                )
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Card(
                    modifier = Modifier.padding(12.dp),
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(20.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        RowImage()

                        RowEmail(
                            email = email,
                            emailChange = {
                                email = it
                                isValidEmail = Patterns.EMAIL_ADDRESS.matcher(email).matches()
                            },
                            isValid = isValidEmail
                        )

                        RowPassword(
                            contrasena = password,
                            passwordChange = {
                                password = it
                                isValidPassword = password.length >= 6
                            },
                            passwordVisible = passwordVisible,
                            passwordVisibleChange = { passwordVisible = !passwordVisible },
                            isValidPassword = isValidPassword
                        )

                        RowPassword(
                            contrasena = confirmPassword,
                            passwordChange = { confirmPassword = it },
                            passwordVisible = confirmPasswordVisible,
                            passwordVisibleChange = { confirmPasswordVisible = !confirmPasswordVisible },
                            isValidPassword = confirmPassword == password
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                if (!isValidEmail || password.length < 6 || password != confirmPassword) {
                                    errorMessage = "Verifica los campos ingresados."
                                } else {
                                    auth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(activity) { task ->
                                            if (task.isSuccessful) {
                                                navController.navigate(Screens.Main.route) {
                                                    popUpTo(Screens.Login.route) { inclusive = true }
                                                }
                                            } else {
                                                errorMessage = "Error al registrar: ${task.exception?.message}"
                                            }
                                        }
                                }
                            }
                        ) {
                            Text("Registrarse")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Texto para volver al login
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "¿Ya tienes cuenta? Inicia sesión",
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable {
                                    navController.navigate(Screens.Login.route) {
                                        popUpTo(Screens.Register.route) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
