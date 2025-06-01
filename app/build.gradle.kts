plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.udb.login"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.udb.login"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3" // Ajusta a la versión correcta que uses
    }
}

dependencies {
    // Firebase BOM para manejar versiones consistentes
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))

    // Firebase Auth y Firestore con Kotlin Extensions
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Google Play Services Auth para inicio sesión con Google
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // Coroutines para integración con Firebase Tasks
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4")

    // Biblioteca para manejo de fechas (ThreeTenABP)
    implementation("com.jakewharton.threetenabp:threetenabp:1.4.4")

    // Jetpack Compose BOM (mantiene versiones consistentes de Compose)
    implementation(platform(libs.androidx.compose.bom))

    // Librerías core de AndroidX y Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Material3 UI y herramientas de Compose
    implementation(libs.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)

    // Navegación en Compose
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.compose.v277)

    // Íconos extendidos Material Compose (Schedule, Person, etc)
    implementation("androidx.compose.material:material-icons-extended")

    // Animaciones Jetpack Compose
    implementation(libs.androidx.animation)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Debugging tools para Compose
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
