package com.udb.login

sealed class Screens(val route: String) {

    // Pantalla de Login
    object Login : Screens("login")

    // Pantalla de Registro
    object Register : Screens("register") //

    // Pantalla base con navegación inferior (navbar)
    object Main : Screens("main")

    // Pantalla principal con lista de eventos
    object Home : Screens("home")

    // Crear un nuevo evento
    object CreateEvent : Screens("create_event")

    // Historial de eventos a los que el usuario ha asistido
    object History : Screens("history")

    // Perfil del usuario
    object Profile : Screens("profile")

    // Detalles de un evento específico (requiere eventId)
    object EventDetail : Screens("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }
}
