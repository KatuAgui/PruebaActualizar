package com.example.pruebaactualizar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.*
import com.google.firebase.storage.*
import android.Manifest
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {
    private var versionActual = "1.1.0"
    private var versionFirebase: String? = null
    private var urlFirebase: String? = null

    private lateinit var txtVersion: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        txtVersion = findViewById(R.id.txt_version)
        txtVersion.text = "Versión $versionActual"

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }

        obtenerFirebase()
        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            Toast.makeText(this, "Hola, Soy la nueva Version", Toast.LENGTH_SHORT).show()
        }
    }

    private fun obtenerFirebase() {
        val database = FirebaseDatabase.getInstance()
        val referenciaVersion: DatabaseReference = database.getReference("version")
        val referenciaUrl: DatabaseReference = database.getReference("url")
        val storage = FirebaseStorage.getInstance()
        /*val referenciaArchivo = storage.reference.child("pruebaActualizar_v1_2.apk")

        referenciaArchivo.downloadUrl.addOnSuccessListener { uri ->
            val nuevaUrl = uri.toString()
            referenciaUrl.setValue(nuevaUrl)
                .addOnSuccessListener {
                    Log.d("URL_ACTUALIZADA", nuevaUrl)
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this@MainActivity, "Error al actualizar la URL: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            val nuevaVersion = "1.2.0"
            referenciaVersion.setValue(nuevaVersion)
                .addOnSuccessListener {
                    Log.d("VERSION_ACTUALIZADA", nuevaVersion)
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this@MainActivity, "Error al actualizar la versión: ${error.message}", Toast.LENGTH_SHORT).show()
                }
        }*/



        referenciaUrl.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                urlFirebase = dataSnapshot.getValue(String::class.java)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@MainActivity, "URL ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })

        referenciaVersion.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                versionFirebase = dataSnapshot.getValue(String::class.java)
               // versionFirebase?.let { Log.d("VERSIONFIREBASE", it) }
                //Log.d("VERSIONACTUAL",versionActual)
               // urlFirebase?.let { Log.d("URL", it) }

                if (versionFirebase!!.trim { it <= ' ' } == versionActual.trim { it <= ' ' }) {
                    Toast.makeText(this@MainActivity, "No es necesario actualizar", Toast.LENGTH_SHORT).show()
                } else {
                    val pantallaActualizar = Intent(applicationContext, PantallaActualizar::class.java)
                    pantallaActualizar.putExtra("version", versionFirebase)
                    pantallaActualizar.putExtra("url", urlFirebase)
                    finish()
                    startActivity(pantallaActualizar)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@MainActivity, "Versión ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}








