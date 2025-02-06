package com.example.ancapargentina

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.os.Build
import android.os.Environment
import android.provider.Settings

class LocalPdfActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_CODE = 100 // Código de solicitud para permisos
        private const val REQUEST_CODE_STORAGE = 101 // Código para acceso a almacenamiento completo en Android 11+
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_local_pdf)

        verificarPermisos()
    }

    private fun verificarPermisos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 y superior requiere permiso especial
            if (Environment.isExternalStorageManager()) {
                // Si ya tiene acceso, proceder con la carga de archivos
                mostrarPdfsLocales()
            } else {
                try {
                    // Solicitar acceso completo al almacenamiento
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.parse("package:$packageName")
                    startActivityForResult(intent, REQUEST_CODE_STORAGE)
                } catch (e: Exception) {
                    Toast.makeText(this, "No se puede abrir la configuración de permisos.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // En versiones anteriores, solo se necesita permiso de lectura y escritura
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Si no se tienen permisos, se solicita al usuario
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CODE
                )
            } else {
                // Si ya se tienen permisos, proceder con la carga de archivos
                mostrarPdfsLocales()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                mostrarPdfsLocales()
            } else {
                Toast.makeText(this, "Permisos denegados para acceder a los PDFs locales", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_STORAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
                mostrarPdfsLocales()
            } else {
                Toast.makeText(this, "Permiso para almacenamiento completo no concedido.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarPdfsLocales() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val pdfFiles = listarPdfsLocales()
        if (pdfFiles.isNotEmpty()) {
            recyclerView.adapter = PdfListAdapter(pdfFiles) { selectedFile ->
                abrirPdf(selectedFile)
            }
        } else {
            Toast.makeText(this, "No hay PDFs descargados", Toast.LENGTH_SHORT).show()
        }
    }

    private fun listarPdfsLocales(): List<File> {
        val dir = getExternalFilesDir(null)
        val pdfFiles = dir?.listFiles()?.filter { it.extension == "pdf" } ?: emptyList()

        pdfFiles.forEach { file ->
            Log.d("LocalPdfActivity", "Archivo encontrado: ${file.absolutePath}")
        }

        return pdfFiles
    }

    private fun abrirPdf(file: File) {
        val intent = Intent(this, AutoPdfActivity::class.java)
        intent.putExtra("localFilePath", file.absolutePath)
        startActivity(intent)
    }
}
