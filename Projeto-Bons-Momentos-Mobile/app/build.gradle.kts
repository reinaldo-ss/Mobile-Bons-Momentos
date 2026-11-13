plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.bonsmomentos.entregador"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.bonsmomentos.entregador"
        minSdk = 26          // Android 8.0 (API 26)
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Supabase (URL do projeto e chave PÚBLICA "publishable" — pode ficar no app).
        // NUNCA coloque aqui a chave "service_role" / "secret".
        buildConfigField("String", "BASE_URL", "\"aqui-vai-sua-string/\"")
        buildConfigField("String", "SUPABASE_KEY", "\"aqui-vai-sua-string/\"")

        // MODO DEMONSTRAÇÃO: true = app funciona SEM internet/servidor (login aceita
        // qualquer email/senha e mostra pedidos de exemplo). false = usa o Supabase.
        buildConfigField("boolean", "MODO_DEMO", "false")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Persistência local
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Sincronização em segundo plano
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // API REST
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Armazenamento seguro do token
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
}
