package com.example.ancapargentina

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ancapargentina.model.Modelo
import com.example.ancapargentina.network.RetrofitInstance
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ModeloDetailActivity : AppCompatActivity() {

    private lateinit var imageViewModelo: ImageView
    private lateinit var textViewNombreModelo: TextView
    private lateinit var textViewAnoModelo: TextView
    private lateinit var textViewTipoVehiculo: TextView
    private lateinit var textViewTipoCombustible: TextView
    private lateinit var buttonVerPdf: Button
    private lateinit var buttonDescargarPdf: Button  // Nuevo botón para la descarga
    private val REQUEST_CODE_STORAGE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modelo_detail)

        // Inicializar las vistas
        imageViewModelo = findViewById(R.id.imageViewModelo)
        textViewNombreModelo = findViewById(R.id.textViewNombreModelo)
        textViewAnoModelo = findViewById(R.id.textViewAnoModelo)
        textViewTipoVehiculo = findViewById(R.id.textViewTipoVehiculo)
        textViewTipoCombustible =findViewById(R.id.textViewTipoCombustible)

        buttonVerPdf = findViewById(R.id.buttonVerPdf)
        buttonDescargarPdf =
            findViewById(R.id.buttonDescargarPdf)  // Inicializa el botón de descarga

        val modeloId = intent.getLongExtra("modeloId", -1L)
        if (modeloId != -1L) {
            obtenerModeloPorId(modeloId)
        } else {
            Toast.makeText(this, "Modelo no válido", Toast.LENGTH_SHORT).show()
        }

        // Configurar el botón para descargar el PDF
        buttonDescargarPdf.setOnClickListener {
            val modeloId = intent.getLongExtra("modeloId", -1L)
            if (modeloId != -1L) {
                obtenerModeloPorId(modeloId)
            } else {
                Toast.makeText(this, "Modelo no válido", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun obtenerModeloPorId(modeloId: Long) {
        RetrofitInstance.apiService.obtenerModeloPorId(modeloId).enqueue(object : Callback<Modelo> {
            override fun onResponse(call: Call<Modelo>, response: Response<Modelo>) {
                if (response.isSuccessful) {
                    val modelo = response.body()
                    modelo?.let {
                        textViewNombreModelo.text = modelo.nombre
                        textViewAnoModelo.text = modelo.año.toString()
                        textViewTipoCombustible.text = modelo.tipoCombustible
                        textViewTipoVehiculo.text = modelo.tipoVehiculo

                        // Cargar la imagen utilizando Picasso
                        val baseUrl = "http://192.168.0.111:8080/api/files/files/"
                        Picasso.get()
                            .load(baseUrl + modelo.imageRes)
                            .into(imageViewModelo)

                        // Configurar el botón para abrir el PDF
                        buttonVerPdf.setOnClickListener {
                            val pdfUrl = baseUrl + modelo.pdf
                            Log.d("ModeloDetailActivity", "PDF URL: $pdfUrl")
                            val intent =
                                Intent(this@ModeloDetailActivity, AutoPdfActivity::class.java)
                            intent.putExtra("pdfUrl", pdfUrl)
                            startActivity(intent)
                        }
                        buttonDescargarPdf.setOnClickListener {
                            val pdfFileName = modelo.pdf
                            if (pdfFileName != null) {
                                descargarPdf(pdfFileName)
                            } else {
                                Toast.makeText(
                                    this@ModeloDetailActivity,
                                    "No hay archivo PDF disponible",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(
                        this@ModeloDetailActivity,
                        "Error al obtener modelo",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Modelo>, t: Throwable) {
                Toast.makeText(
                    this@ModeloDetailActivity,
                    "Error de red: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun descargarPdf(fileName: String) {
        RetrofitInstance.apiService.descargarPdf(fileName).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    // Guardar el archivo PDF en almacenamiento local
                    val inputStream = response.body()?.byteStream()
                    val file = File(getExternalFilesDir(null), "$fileName.pdf")

                    try {
                        val outputStream = FileOutputStream(file)
                        val buffer = ByteArray(8 * 1024)  // 8KB
                        var bytesRead: Int
                        while (inputStream?.read(buffer).also { bytesRead = it ?: -1 } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                        }
                        outputStream.flush()
                        outputStream.close()
                        inputStream?.close()

                        Toast.makeText(
                            this@ModeloDetailActivity,
                            "PDF descargado exitosamente",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: IOException) {
                        Toast.makeText(
                            this@ModeloDetailActivity,
                            "Error al guardar el PDF",
                            Toast.LENGTH_SHORT
                        ).show()
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(
                        this@ModeloDetailActivity,
                        "Error en la descarga del PDF",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(
                    this@ModeloDetailActivity,
                    "Error de red: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
