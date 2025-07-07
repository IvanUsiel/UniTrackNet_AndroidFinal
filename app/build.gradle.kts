import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.irjarqui.unitracknetv3"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.irjarqui.unitracknetv3"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if(localPropertiesFile.exists()){
            localProperties.load(localPropertiesFile.inputStream())
        }

        val mapsApiKey = localProperties.getProperty("MAPS_API_KEY")

        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
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

    buildFeatures{
        viewBinding=true

    }
}


dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //Dependencias pra el proyecto

    //Para retrofit
    implementation(libs.retrofit)

    //Gson
    implementation(libs.converter.gson)

    //Adicional para el interceptor
    implementation(libs.logging.interceptor)

    //Glide
    implementation(libs.glide)

    //Para las corrutinas con alcance lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Corrutinas Kotlin
    implementation(libs.kotlinx.coroutines.android)

    //Imágenes con bordes redondeados
    implementation(libs.roundedimageview)

    // Material Desing
    implementation(libs.material.v1110)

    //Encriptacion shared Preferences
    implementation(libs.androidx.security.crypto)

    //Biometria
    implementation (libs.androidx.biometric)

    //Google Maps (Play services de Google Maps, tanto para vistas XML como para Compose)
    implementation(libs.play.services.maps)

    //API'S opcionales para la ubicación (XML y Compose). Ej. Clase FusedLocationProviderClient
    implementation(libs.play.services.location)

    //Google Maps (Play services de Google Maps, utilidad para vista)
    implementation (libs.android.maps.utils)

    implementation (libs.androidx.fragment.ktx)

    //Poleo de alarmas
    implementation (libs.androidx.work.runtime.ktx)

    //refresh swipe
    implementation (libs.androidx.swiperefreshlayout)

    //loading
    implementation (libs.androidx.fragment.ktx.v162)

    //grafica de velocidadping
    implementation (libs.anastr.speedviewlib)

    //splashscreen
    implementation (libs.androidx.core.splashscreen)




}