plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val stableKeystorePath = System.getenv("SUPERIA_KEYSTORE_FILE")
val stableStorePassword = System.getenv("SUPERIA_KEYSTORE_PASSWORD")
val stableKeyAlias = System.getenv("SUPERIA_KEY_ALIAS")
val stableKeyPassword = System.getenv("SUPERIA_KEY_PASSWORD")
val hasStableSigning = listOf(
    stableKeystorePath,
    stableStorePassword,
    stableKeyAlias,
    stableKeyPassword
).all { !it.isNullOrBlank() }

val ciVersionCode = maxOf(1, System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 1)

android {
    namespace = "it.superiaprivata.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "it.superiaprivata.app"
        minSdk = 26
        targetSdk = 35
        versionCode = ciVersionCode
        versionName = "0.1.$ciVersionCode"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    if (hasStableSigning) {
        signingConfigs {
            create("stable") {
                storeFile = file(stableKeystorePath!!)
                storePassword = stableStorePassword
                keyAlias = stableKeyAlias
                keyPassword = stableKeyPassword
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            if (hasStableSigning) {
                signingConfig = signingConfigs.getByName("stable")
            }
        }
    }

    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.02.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.google.ai.edge.litertlm:litertlm-android:0.15.0")
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")
    implementation("com.github.adaptech-cz.Tesseract4Android:tesseract4android:4.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
}
