package com.udb.login

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation(
    navController: NavHostController,
    activity: Activity,
    auth: FirebaseAuth,
    googleSignInClient: GoogleSignInClient,
    currentUserEmail: String?
) {
    NavHost(
        navController = navController,
        startDestination = Screens.Login.route
    ) {
        // Pantalla de inicio de sesión
        composable(Screens.Login.route) {
            LoginScreen(
                navController = navController,
                activity = activity,
                auth = auth,
                googleSignInClient = googleSignInClient
            )
        }

        // Pantalla de registro de usuario
        composable(Screens.Register.route) {
            RegisterScreen(
                navController = navController,
                activity = activity,
                auth = auth
            )
        }

        // Pantalla principal con navegación inferior
        composable(Screens.Main.route) {
            MainScaffold(navController = navController)
        }
    }
}
