package com.ghostyapps.heyappsmanager

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

class AppManager(private val context: Context) {

    fun getInstalledVersion(packageName: String): String? {
        return try {
            val pInfo = context.packageManager.getPackageInfo(packageName, 0)
            pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    fun determineStatus(installedVer: String?, githubVerTag: String?): AppStatus {
        if (installedVer == null) return AppStatus.NOT_INSTALLED
        if (githubVerTag == null) return AppStatus.INSTALLED

        // LOG: Ne geldiğini görelim
        Log.d("HeyDebug", "Kıyaslama Öncesi -> Cihaz: '$installedVer' vs GitHub: '$githubVerTag'")

        // KESİN TEMİZLİK: Rakam (0-9) ve Nokta (.) dışındaki her şeyi sil!
        // HeyCam_v0.2.3  -> 0.2.3
        // 0.2.3-nothing  -> 0.2.3
        val cleanGithub = githubVerTag.replace(Regex("[^0-9.]"), "")
        val cleanInstalled = installedVer.replace(Regex("[^0-9.]"), "")

        Log.d("HeyDebug", "Temizlik Sonrası -> Cihaz: '$cleanInstalled' vs GitHub: '$cleanGithub'")

        return if (isVersionGreater(cleanGithub, cleanInstalled)) {
            AppStatus.UPDATE_AVAILABLE
        } else {
            AppStatus.INSTALLED
        }
    }

    private fun isVersionGreater(newVer: String, oldVer: String): Boolean {
        // Boş string gelirse (örneğin versiyon sadece harfse) 0 kabul et
        if (newVer.isEmpty() || oldVer.isEmpty()) return false

        val newParts = newVer.split(".").map { it.toIntOrNull() ?: 0 }
        val oldParts = oldVer.split(".").map { it.toIntOrNull() ?: 0 }

        val length = maxOf(newParts.size, oldParts.size)

        for (i in 0 until length) {
            val n = if (i < newParts.size) newParts[i] else 0
            val o = if (i < oldParts.size) oldParts[i] else 0

            if (n > o) return true
            if (n < o) return false
        }

        return false
    }
}