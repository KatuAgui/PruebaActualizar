package com.example.pruebaactualizar

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.pruebaactualizar.clases.MyReceiver
import com.google.firebase.storage.*
import android.Manifest
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

class PantallaActualizar : AppCompatActivity() {

    private var oMyReceiver: MyReceiver? = null
    private var btnDescargar: Button? = null

    private var url: String? = null
    private var version: String? = null
    private val REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_actualizar)

        version = intent.getStringExtra("version")
        url = intent.getStringExtra("url")


        init()
        btnDescargar = findViewById(R.id.btn_Actualizar)
        btnDescargar?.setOnClickListener {
            // Verificar si ya se tienen los permisos de lectura y escritura
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.REQUEST_INSTALL_PACKAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.REQUEST_INSTALL_PACKAGES
                    ),
                    1
                )
            } else {
                // Los permisos están otorgados, puedes continuar con la instalación
                url?.let { it1 -> oMyReceiver?.download(it1) }
            }

        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            // Verificar si los permisos se otorgaron
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Los permisos se otorgaron, continuar con la instalación
                url?.let { it1 -> oMyReceiver?.download(it1) }
            } else {
                // Los permisos no se otorgaron, mostrar un mensaje o realizar alguna otra acción
                Toast.makeText(this, "Se requieren permisos de instalación para continuar.", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun init() {
        oMyReceiver = MyReceiver(this@PantallaActualizar)
        oMyReceiver?.register(oMyReceiver)
    }

    override fun onPause() {
        super.onPause()
        oMyReceiver?.unregister(oMyReceiver!!)
    }

    override fun onResume() {
        super.onResume()
        oMyReceiver?.register(oMyReceiver!!)
    }
/*
    private fun descargarEInstalarAPK(urlDescarga: String, nombreArchivo: String, contexto: Context) {
    // Crea un objeto de descarga
    val gestorDescarga = contexto.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    // Crea una solicitud de descarga con la URL de descarga
    val solicitudDescarga = DownloadManager.Request(Uri.parse(urlDescarga))

    // Define el nombre del archivo a descargar y su ubicación en el almacenamiento externo del dispositivo
    solicitudDescarga.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, nombreArchivo)

    // Inicia la descarga y obtiene el ID de descarga
    val idDescarga = gestorDescarga.enqueue(solicitudDescarga)

    // Crea un BroadcastReceiver para recibir una notificación cuando la descarga se complete
    val receptorDescarga = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Crea un URI para el archivo descargado
            val archivoAPK = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), nombreArchivo)
            val uriAPK = Uri.fromFile(archivoAPK)

            // Crea un Intent para verificar si hay algún paquete instalador disponible en el dispositivo
            val intentVerificador = Intent(Intent.ACTION_MAIN)
            intentVerificador.addCategory(Intent.CATEGORY_LAUNCHER)
            intentVerificador.setPackage("com.android.packageinstaller")

            val actividades = packageManager.queryIntentActivities(intentVerificador, 0)
            if (actividades.isNotEmpty()) {
                // Si hay paquetes instaladores disponibles, realiza la instalación del archivo APK
                val intentInstalador = Intent(Intent.ACTION_VIEW)
                intentInstalador.setDataAndType(uriAPK, "application/vnd.android.package-archive")
                intentInstalador.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                contexto.startActivity(intentInstalador)
            } else {
                // Si no hay paquetes instaladores disponibles, muestra un mensaje de error
                Toast.makeText(contexto, "No se encontró un paquete instalador en el dispositivo.", Toast.LENGTH_SHORT).show()
            }

            // Deregistra el BroadcastReceiver
            context?.unregisterReceiver(this)
        }
    }

    // Registra el BroadcastReceiver para recibir la notificación de descarga completa
    contexto.registerReceiver(receptorDescarga, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
}


    private fun instalarAplicacion(contexto: Context, archivoAPK: File) {
        // Crea un URI para el archivo APK
        val uriAPK = FileProvider.getUriForFile(contexto, "${contexto.packageName}.fileprovider", archivoAPK)

        // Crea un Intent para abrir el archivo APK y mostrar una ventana emergente para que el usuario lo instale
        val intentInstalador = Intent(Intent.ACTION_VIEW)
        intentInstalador.data = uriAPK
        intentInstalador.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        intentInstalador.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
        intentInstalador.type = "application/vnd.android.package-archive"

        // Verifica si hay algún paquete instalador disponible en el dispositivo
        val actividades = contexto.packageManager.queryIntentActivities(intentInstalador, 0)
        if (actividades.isNotEmpty()) {
            // Si hay paquetes instaladores disponibles, muestra el intent para que el usuario elija cómo instalar el archivo APK
            contexto.startActivity(intentInstalador)
        } else {
            // Si no hay paquetes instaladores disponibles, muestra un mensaje de error
            Toast.makeText(contexto, "No se encontró un paquete instalador en el dispositivo.", Toast.LENGTH_SHORT).show()
        }
    }


private fun descargarEInstalarAPK(urlDescarga: String, nombreArchivo: String, contexto: Context) {
    // Crea un objeto de descarga
    val gestorDescarga = contexto.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    // Crea una solicitud de descarga con la URL de descarga
    val solicitudDescarga = DownloadManager.Request(Uri.parse(urlDescarga))

    // Define el nombre del archivo a descargar y su ubicación en el almacenamiento externo del dispositivo
    solicitudDescarga.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, nombreArchivo)

    // Inicia la descarga y obtiene el ID de descarga
    val idDescarga = gestorDescarga.enqueue(solicitudDescarga)

    // Crea un BroadcastReceiver para recibir una notificación cuando la descarga se complete
    val receptorDescarga = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Crea un URI para el archivo descargado
            val archivoDescargado = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), nombreArchivo)
              Log.d("ArchivoDescargado",archivoDescargado.toString())
            // Llama a la función instalarAplicacion pasándole la ruta del archivo descargado
            instalarAplicacion(context, archivoDescargado.path)

            // Deregistra el BroadcastReceiver
            contexto?.unregisterReceiver(this)
        }
    }

    // Registra el BroadcastReceiver para recibir la notificación de descarga completa
    contexto.registerReceiver(receptorDescarga, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
}

    private fun instalarAplicacion(contexto: Context?, rutaArchivo: String) {
        // Crea un URI para el archivo descargado
        val archivoDescargado = File(rutaArchivo)
        val uriArchivoDescargado = archivoDescargado.let { FileProvider.getUriForFile(contexto!!, "${contexto.packageName}.fileprovider", it) }

        // Realiza un Intent para abrir el archivo descargado y mostrar una ventana emergente para que el usuario lo instale
        val intentInstalador = Intent(Intent.ACTION_VIEW)
        intentInstalador.setDataAndType(uriArchivoDescargado, "application/vnd.android.package-archive")
        intentInstalador.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)

        // Verifica si hay algún paquete instalador disponible en el dispositivo
        val intentVerificador = Intent(Intent.ACTION_VIEW)
        intentVerificador.data = Uri.parse("market://details?id=com.android.packageinstaller")
        val actividades = contexto?.packageManager?.queryIntentActivities(intentVerificador, 0)
        if (actividades != null && actividades.isNotEmpty()) {
            // Si hay paquetes instaladores disponibles, muestra el intent para que el usuario elija cómo instalar el archivo APK
            intentVerificador.setPackage("com.android.packageinstaller")
            contexto?.startActivity(intentVerificador)
        } else {
            // Si no hay paquetes instaladores disponibles, muestra el intent para que el usuario lo instale manualmente
            contexto?.startActivity(intentInstalador)
        }
    }

 */
}
