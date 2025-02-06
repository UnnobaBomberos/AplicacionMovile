package com.example.ancapargentina

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AutoPdfActivity : AppCompatActivity() {

    private lateinit var pdfRenderer: PdfRenderer
    private lateinit var currentPage: PdfRenderer.Page
    private lateinit var parcelFileDescriptor: ParcelFileDescriptor
    private lateinit var imageView: ImageView
    private lateinit var nextPageButton: Button
    private lateinit var prevPageButton: Button
    private var pageIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auto_pdf)

        imageView = findViewById(R.id.imageView)
        nextPageButton = findViewById(R.id.nextPageButton)
        prevPageButton = findViewById(R.id.prevPageButton)

        nextPageButton.setOnClickListener { showPage(pageIndex + 1) }
        prevPageButton.setOnClickListener { showPage(pageIndex - 1) }

        val pdfUrl = intent.getStringExtra("pdfUrl")
        if (pdfUrl != null) {
            downloadPdfAndOpen(pdfUrl)
        } else {
            val localFilePath = intent.getStringExtra("localFilePath")
            if (localFilePath != null) {
                val file = File(localFilePath)
                if (file.exists()) {
                    openPdfRenderer(file)
                } else {
                    Toast.makeText(this, "El archivo PDF no existe", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "No se pudo cargar el PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun downloadPdfAndOpen(pdfUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(pdfUrl).build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@AutoPdfActivity, "Error al descargar el PDF", Toast.LENGTH_SHORT).show()
                        }
                        return@use
                    }

                    // Guardar el PDF descargado en el directorio de caché
                    val file = File(cacheDir, "temp_pdf_file.pdf")
                    FileOutputStream(file).use { output ->
                        response.body?.byteStream()?.copyTo(output)
                    }
                    withContext(Dispatchers.Main) {
                        openPdfRenderer(file)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AutoPdfActivity, "Error al descargar el PDF: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun openPdfRenderer(file: File) {
        try {
            parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(parcelFileDescriptor)
            showPage(0)
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error al abrir el PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showPage(index: Int) {
        if (index < 0 || index >= pdfRenderer.pageCount) return

        if (::currentPage.isInitialized) {
            currentPage.close()
        }

        currentPage = pdfRenderer.openPage(index)
        val bitmap = Bitmap.createBitmap(currentPage.width, currentPage.height, Bitmap.Config.ARGB_8888)
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        imageView.setImageBitmap(bitmap)

        pageIndex = index
        updateNavigationButtons()
    }

    private fun updateNavigationButtons() {
        prevPageButton.isEnabled = pageIndex > 0
        nextPageButton.isEnabled = pageIndex < pdfRenderer.pageCount - 1
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::currentPage.isInitialized) {
            currentPage.close()
        }
        pdfRenderer.close()
        parcelFileDescriptor.close()
    }
}
