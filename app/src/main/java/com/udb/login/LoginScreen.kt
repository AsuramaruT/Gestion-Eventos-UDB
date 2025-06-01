package com.udb.login

import android.app.Activity
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider

@Composable
fun LoginScreen(
    navController: NavController,
    activity: Activity,
    auth: FirebaseAuth,
    googleSignInClient: GoogleSignInClient
) {
    var email by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var isValidEmail by remember { mutableStateOf(false) }
    var isValidPassword by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        if (errorMessage.isNotBlank()) {
            snackbarHostState.showSnackbar(errorMessage)
            errorMessage = ""
        }
    }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener(activity) {
                    if (it.isSuccessful) {
                        navController.navigate(Screens.Main.route) {
                            popUpTo(Screens.Login.route) { inclusive = true }
                        }
                    } else {
                        errorMessage = "Error al iniciar sesión con Google"
                    }
                }
        } catch (e: Exception) {
            errorMessage = "Falló Google Sign-In: ${e.message}"
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
                            contrasena = contrasena,
                            passwordChange = {
                                contrasena = it
                                isValidPassword = contrasena.length >= 6
                            },
                            passwordVisible = passwordVisible,
                            passwordVisibleChange = { passwordVisible = !passwordVisible },
                            isValidPassword = isValidPassword
                        )

                        RowButtonLogin(
                            showError = showError,
                            onClickLogin = {
                                if (email.isEmpty() || contrasena.isEmpty() || !isValidEmail || !isValidPassword) {
                                    showError = true
                                    errorMessage = "Correo o contraseña inválidos"
                                } else {
                                    showError = false
                                    auth.signInWithEmailAndPassword(email, contrasena)
                                        .addOnCompleteListener(activity) { task ->
                                            if (task.isSuccessful) {
                                                navController.navigate(Screens.Main.route) {
                                                    popUpTo(Screens.Login.route) { inclusive = true }
                                                }
                                            } else {
                                                errorMessage = "Usuario no registrado o contraseña incorrecta"
                                            }
                                        }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        CustomGoogleSignInButton {
                            val signInIntent = googleSignInClient.signInIntent
                            googleLauncher.launch(signInIntent)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                val provider = OAuthProvider.newBuilder("github.com")
                                val pending = auth.pendingAuthResult
                                if (pending != null) {
                                    pending
                                        .addOnSuccessListener {
                                            navController.navigate(Screens.Main.route) {
                                                popUpTo(Screens.Login.route) { inclusive = true }
                                            }
                                        }
                                        .addOnFailureListener {
                                            errorMessage = "Error con GitHub: ${it.message}"
                                        }
                                } else {
                                    auth.startActivityForSignInWithProvider(activity, provider.build())
                                        .addOnSuccessListener {
                                            navController.navigate(Screens.Main.route) {
                                                popUpTo(Screens.Login.route) { inclusive = true }
                                            }
                                        }
                                        .addOnFailureListener {
                                            errorMessage = "Fallo autenticación con GitHub: ${it.message}"
                                        }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                        ) {
                            Text("Iniciar sesión con GitHub", color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 👉 Enlace a la pantalla de registro
                        TextButton(
                            onClick = { navController.navigate(Screens.Register.route) },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("¿No tienes cuenta? Regístrate aquí")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomGoogleSignInButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        shape = RoundedCornerShape(6.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Text("Iniciar sesión con Google")
    }
}

@Composable
fun RowEmail(email: String, emailChange: (String) -> Unit, isValid: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = emailChange,
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            maxLines = 1,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isValid) Color.Green else Color.Red,
                focusedLabelColor = if (isValid) Color.Green else Color.Red
            )
        )
    }
}

@Composable
fun RowPassword(
    contrasena: String,
    passwordChange: (String) -> Unit,
    passwordVisible: Boolean,
    passwordVisibleChange: () -> Unit,
    isValidPassword: Boolean
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = contrasena,
            onValueChange = passwordChange,
            maxLines = 1,
            singleLine = true,
            label = { Text("Contraseña") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                IconButton(onClick = passwordVisibleChange) {
                    Icon(imageVector = image, contentDescription = "Ver contraseña")
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isValidPassword) Color.Green else Color.Red,
                focusedLabelColor = if (isValidPassword) Color.Green else Color.Red
            )
        )
    }
}

@Composable
fun RowButtonLogin(showError: Boolean, onClickLogin: () -> Unit) {
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onClickLogin
            ) {
                Text("Iniciar Sesión")
            }
        }

        if (showError) {
            Text(
                text = "Complete los campos correctamente",
                color = Color.Red,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

@Composable
fun RowImage() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Image(
            modifier = Modifier.width(100.dp),
            painter = painterResource(id = R.drawable.profile),
            contentDescription = "Imagen login"
        )
    }
}
