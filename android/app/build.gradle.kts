plugins {
    id("com.android.application")
    id("kotlin-android")
    id("dev.flutter.flutter-gradle-plugin")
}

android {
    namespace = "com.example.smart_watch_face"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.smart_watch_face"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Wear OS Push Libraries
    implementation("com.google.android.gms:play-services-wearable:19.0.0")
    implementation("androidx.wear.watchfacepush:watchfacepush:1.0.0-beta01")
    
    implementation ("com.google.android.gms:play-services-wearable:18.1.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0") 

    // implementation ("com.google.android.gms:play-services-wearable:18.1.0")
    // implementation ("androidx.wear.watchface:watchface-push:1.2.1")
    // implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
}