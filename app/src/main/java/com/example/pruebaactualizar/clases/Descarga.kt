package com.example.pruebaactualizar.clases

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment

class Descarga(urlDescarga: String, nombreArchivo: String, contexto: Context)  {

    fun descargarEInstalarAPK(urlDescarga: String, nombreArchivo: String, contexto: Context) {
        // Crea un objeto de descarga
        val gestorDescarga = contexto.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        // Crea una solicitud de descarga con la URL de descarga
        val solicitudDescarga = DownloadManager.Request(Uri.parse(urlDescarga))

        // Define el nombre del archivo a descargar y su ubicación en el almacenamiento externo del dispositivo
        solicitudDescarga.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, nombreArchivo)

        // Inicia la descarga y obtiene el ID de descarga
        val idDescarga = gestorDescarga.enqueue(solicitudDescarga)

        // Crea un intent para abrir el archivo una vez que se complete la descarga
        val intentInstalador = Intent(Intent.ACTION_VIEW)
        intentInstalador.setDataAndType(Uri.fromFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).listFiles().firstOrNull { it.name == nombreArchivo }), "application/vnd.android.package-archive")
        intentInstalador.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION

        // Registra un BroadcastReceiver para saber cuándo se complete la descarga
        val receptorDescarga = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // Verifica si la descarga se completó y si lo hizo, abre el instalador
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == intent?.action) {
                    val idDescargaCompletada = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (idDescarga == idDescargaCompletada) {
                        contexto.startActivity(intentInstalador)
                    }
                }
            }
        }
        contexto.registerReceiver(receptorDescarga, android.content.IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }


}
