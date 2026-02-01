package com.ghostyapps.heyappsmanager

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import android.util.Log

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: AppsAdapter
    private lateinit var appManager: AppManager
    private val myApps = mutableListOf<AppModel>()
    private var downloadIdMap = mutableMapOf<Long, AppModel>()

    private var pendingInstallId: Long? = null

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val service = retrofit.create(GitHubService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        appManager = AppManager(this)

        val listenToBroadcast = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Context.RECEIVER_EXPORTED
        } else {
            0
        }
        registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), listenToBroadcast)

        // SADECE İKİ UYGULAMA
        myApps.apply {
            add(AppModel("HeyCam", "Camera App", "com.ghostyapps.heycam", "ghostyApps", "HeyCam"))
            add(AppModel("HeyNotes", "Simple Notes", "com.ghostyapps.heynotes", "ghostyApps", "HeyNotes"))
            add(AppModel("HeyBattery", "Detailed battery statistics", "com.ghostyapps.heybattery", "ghostyApps", "HeyBattery"))
            add(AppModel("HeyWidgets", "Custom Widgets I made for myself", "com.ghostyapps.heywidgets", "ghostyApps", "HeyWidgets"))
            add(AppModel("HeyPlayer", "Simple Music Player", "com.ghostyapps.heyplayer", "ghostyApps", "HeyPlayer"))
            add(AppModel("HeyApps", "This App Manager", "com.ghostyapps.heyappsmanager", "ghostyApps", "HeyAppsManager"))
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewApps)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = AppsAdapter(myApps) { app ->
            handleButtonClick(app)
        }
        recyclerView.adapter = adapter

        fetchGitHubUpdates()
    }

    override fun onResume() {
        super.onResume()

        if (pendingInstallId != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && packageManager.canRequestPackageInstalls()) {
                installApk(pendingInstallId!!)
                pendingInstallId = null
            }
        }
        refreshLocalStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onDownloadComplete)
    }

    private fun refreshLocalStatus() {
        myApps.forEach { app ->
            val installedVer = appManager.getInstalledVersion(app.packageName)

            // 1. Önce AppManager ile gerçek durumu hesapla
            // (installedVer null ise zaten NOT_INSTALLED döner)
            val calculatedStatus = appManager.determineStatus(installedVer, app.latestVersion)

            // LOG: Durumu görelim
            Log.d("HeyDebug", "APP: ${app.name} -> Hesaplanan: $calculatedStatus, Mevcut Durum: ${app.status}")

            // 2. KRİTİK MÜDAHALE:
            // Eğer hesaplanan durum INSTALLED ise (yani yükleme bitmiş, versiyonlar eşleşmişse),
            // uygulama şu an "DOWNLOADING" modunda olsa bile zorla "INSTALLED" yap.
            // Böylece Lottie durur, buton "Open" olur.
            if (calculatedStatus == AppStatus.INSTALLED) {
                app.status = AppStatus.INSTALLED
                Log.d("HeyDebug", ">> Yükleme tamamlandı! Lottie durduruluyor.")
            }
            // 3. Eğer yüklü değilse veya update varsa...
            else {
                // ...ve şu an indirme işlemi devam ediyorsa DOKUNMA.
                if (app.status == AppStatus.DOWNLOADING) {
                    Log.d("HeyDebug", ">> İndirme devam ediyor, karışılmadı.")
                    return@forEach
                }
                // İndirme yoksa hesaplanan durumu (Update veya Not Installed) uygula
                app.status = calculatedStatus
            }
        }
        adapter.notifyDataSetChanged()
    }
    private fun fetchGitHubUpdates() {
        lifecycleScope.launch {
            myApps.forEach { app ->
                try {
                    val installedVer = appManager.getInstalledVersion(app.packageName)

                    // Log: İstek atılıyor
                    Log.d("HeyDebug", "${app.name} için GitHub isteği gönderiliyor...")

                    val release = service.getLatestRelease(app.repoOwner, app.repoName)

                    // Log: Cevap geldi
                    Log.d("HeyDebug", "CEVAP GELDİ: ${app.name} -> GitHub Tag: ${release.tag_name}")

                    app.latestVersion = release.tag_name
                    val apkAsset = release.assets.find { it.browser_download_url.endsWith(".apk") }
                    app.downloadUrl = apkAsset?.browser_download_url

                    app.status = appManager.determineStatus(installedVer, release.tag_name)

                } catch (e: Exception) {
                    Log.e("HeyDebug", "HATA: ${app.name} verisi çekilemedi. Sebebi: ${e.message}")

                    val installedVer = appManager.getInstalledVersion(app.packageName)
                    app.status = if (installedVer != null) AppStatus.INSTALLED else AppStatus.NOT_INSTALLED
                }
                adapter.notifyDataSetChanged()
            }
        }
    }
    private fun handleButtonClick(app: AppModel) {
        if (app.status == AppStatus.NOT_INSTALLED || app.status == AppStatus.UPDATE_AVAILABLE) {
            if (app.downloadUrl != null) {
                app.status = AppStatus.DOWNLOADING
                adapter.notifyDataSetChanged()
                downloadApk(app)
            } else {
                Toast.makeText(this, "Download link not found", Toast.LENGTH_SHORT).show()
            }
        } else if (app.status == AppStatus.INSTALLED) {
            val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
            if (launchIntent != null) startActivity(launchIntent)
        }
    }

    private fun downloadApk(app: AppModel) {
        try {
            val fileName = "${app.name}.apk"
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
            if (file.exists()) file.delete()

            val request = DownloadManager.Request(Uri.parse(app.downloadUrl))
                .setTitle("Downloading ${app.name}")
                .setDescription("Please wait...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

            val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = manager.enqueue(request)

            downloadIdMap[downloadId] = app

        } catch (e: Exception) {
            app.status = AppStatus.NOT_INSTALLED
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private val onDownloadComplete = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            // Map'ten ilgili uygulamayı buluyoruz
            val app = downloadIdMap[id]

            if (app != null) {
                // KRİTİK HAMLE:
                // İndirme bittiği için artık "Downloading" modunda kalmasına gerek yok.
                // Durumu "LOADING" (Checking...) yapıyoruz.
                // Bu sayede onResume çalıştığında "Ha bu indirme modunda değilmiş, durumunu güncelleyebilirim"
                // diyebilecek ve Cancel dersen eski haline döndürecek.

                app.status = AppStatus.LOADING
                adapter.notifyDataSetChanged() // Listeyi uyar

                // Kurulumu başlat
                installApk(id)
            }
        }
    }
    private fun installApk(downloadId: Long) {
        try {
            val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = manager.getUriForDownloadedFile(downloadId)

            if (uri != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (!packageManager.canRequestPackageInstalls()) {
                        pendingInstallId = downloadId
                        Toast.makeText(this, "Please allow permission to install updates", Toast.LENGTH_LONG).show()
                        startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                            data = Uri.parse("package:$packageName")
                        })
                        return
                    }
                }

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Install Failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}