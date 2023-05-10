package com.example.pruebaactualizar.clases

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import androidx.core.content.FileProvider
import java.io.File

class MyReceiver(private val activity: Activity) : BroadcastReceiver() {

    private val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
    val nameAPK = "pruebaActualizar_v1_2.apk"
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            val downloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)

            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                if (columnIndex >= 0 && DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(columnIndex)) {
                    // La descarga se completó con éxito
                    val uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                    if (uriIndex >= 0) {
                        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), nameAPK)
                        Log.d("URI",file.toString())
                        // Obtener la URI del archivo descargado
                        val contentUri = FileProvider.getUriForFile(context!!, "${context.applicationContext.packageName}.provider", file)

                        // Abrir la pantalla de instalación de la nueva APK
                        val installIntent = Intent(Intent.ACTION_VIEW)
                        installIntent.setDataAndType(contentUri, "application/vnd.android.package-archive")
                        installIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                        installIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                        activity.startActivity(installIntent)

                    } else {
                        Log.e("MsjDescarga", "La columna COLUMN_LOCAL_URI no está presente en el cursor.")
                    }
                } else {
                    Log.e("MsjDescarga", "Error al descargar archivo")
                }
            }
            cursor.close()
        }
    }

    fun download(url: String) {
        val downloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(url))
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setMimeType("application/vnd.android.package-archive")
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, nameAPK)
        downloadManager.enqueue(request)
        Log.d("Descarga completa",url)
    }

    fun register(oMyReceiver: MyReceiver?) {
        activity.registerReceiver(this, intentFilter)
    }

    fun unregister(oMyReceiver: MyReceiver?) {
        activity.unregisterReceiver(this)
    }
}

