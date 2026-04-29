plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.aplicacionviajes"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.aplicacionviajes"
        minSdk = 33
        targetSdk = 36
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
}

// firebase-auth-common is a defunct artifact merged into firebase-auth in modern versions.
// Exclude it globally to prevent old transitive deps from requesting a version that no longer exists.
configurations.all {
    exclude(group = "com.google.firebase", module = "firebase-auth-common")
}

dependencies {
    implementation(libs.appcompat)
    implementation("com.github.bumptech.glide:glide:4.14.2")
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.androidx.core)
    implementation(libs.espresso.core)

    // Firebase – enforcedPlatform ensures the BOM version wins over any transitive override
    implementation(enforcedPlatform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore)

    // Google Maps & Location
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    // Retrofit + Gson
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // Google Sign-In via Credential Manager
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)

    // Dependencies for unit tests (including those using Espresso in src/test)
    testImplementation(libs.junit)
    testImplementation(libs.ext.junit)
    testImplementation(libs.espresso.core)
    testImplementation(libs.espresso.intents)
    testImplementation(libs.androidx.test.core)
    testImplementation("org.robolectric:robolectric:4.12.1")

    // Dependencies for instrumented tests
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.espresso.intents)
}
