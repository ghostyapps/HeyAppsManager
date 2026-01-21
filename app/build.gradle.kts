import java.text.SimpleDateFormat
import java.util.Date
import com.android.build.gradle.internal.api.BaseVariantOutputImpl

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.ghostyapps.heyappsmanager"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.ghostyapps.heyappsmanager"
        minSdk = 34
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    applicationVariants.all {
        val variant = this
        variant.outputs
            .map { it as BaseVariantOutputImpl }
            .forEach { output ->
                // Artık import ettiğimiz için direkt sınıfları kullanıyoruz
                val date = SimpleDateFormat("yyyy-MM-dd_HH-mm").format(Date())
                val newFileName = "HeyAppsManager_v${variant.versionName}_${date}.apk"
                output.outputFileName = newFileName
            }
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    // UI Bileşenleri (Hatanın asıl çözümü bunlar)
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // İnternet ve JSON işlemleri (Mantık kısmı için gerekli olacak)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // 1. BU SATIR EN ÖNEMLİSİ (Hataların %90'ını bu çözecek)
    implementation("androidx.appcompat:appcompat:1.6.1")

    // 2. LifecycleScope hatası için bu gerekli
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // 3. Materyal tasarım bileşenleri (Genelde gereklidir)
    implementation("com.google.android.material:material:1.11.0")

    implementation("com.airbnb.android:lottie:6.1.0")
}