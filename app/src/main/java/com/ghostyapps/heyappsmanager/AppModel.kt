package com.ghostyapps.heyappsmanager

data class AppModel(
    val name: String,           // Örn: "HeyCam"
    val description: String,    // Örn: "Kamera uygulaması"
    val packageName: String,    // Örn: "com.ghostyapps.heycam"
    val repoOwner: String,      // Örn: "ghostyApps"
    val repoName: String,       // Örn: "HeyCam"
    var status: AppStatus = AppStatus.LOADING, // Durum
    var downloadUrl: String? = null,
    var latestVersion: String? = null
)

enum class AppStatus {
    LOADING, NOT_INSTALLED, UPDATE_AVAILABLE, INSTALLED, DOWNLOADING
}