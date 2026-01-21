package com.ghostyapps.heyappsmanager

import retrofit2.http.GET
import retrofit2.http.Path

interface GitHubService {
    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): ReleaseResponse
}

// JSON'dan gelen veriyi karşılayan basit model
data class ReleaseResponse(
    val tag_name: String, // Versiyon (örn: v1.0.2)
    val assets: List<Asset>
)

data class Asset(
    val browser_download_url: String // APK indirme linki
)