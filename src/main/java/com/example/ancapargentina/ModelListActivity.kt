package com.example.ancapargentina

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ancapargentina.model.Modelo
import com.example.ancapargentina.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ModelListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var modeloAdapter: ModeloAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_model_list)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val marcaId = intent.getLongExtra("marcaId", -1L)
        if (marcaId != -1L) {
            obtenerModelosPorMarca(marcaId)
        } else {
            Toast.makeText(this, "Marca no válida", Toast.LENGTH_SHORT).show()
        }
    }

    private fun obtenerModelosPorMarca(marcaId: Long) {
        RetrofitInstance.apiService.obtenerModelosPorMarca(marcaId).enqueue(object : Callback<List<Modelo>> {
            override fun onResponse(call: Call<List<Modelo>>, response: Response<List<Modelo>>) {
                if (response.isSuccessful) {
                    val modelos = response.body()
                    modelos?.let {
                        modeloAdapter = ModeloAdapter(it) { modelo -> // Pasamos la lista de modelos
                            val intent = Intent(this@ModelListActivity, ModeloDetailActivity::class.java)
                            intent.putExtra("modeloId", modelo.id)
                            startActivity(intent)
                            Log.d("ModelListActivity", "Modelo seleccionado: ${modelo.nombre}")
                        }
                        recyclerView.adapter = modeloAdapter
                    }
                } else {
                    Toast.makeText(this@ModelListActivity, "Error al obtener modelos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Modelo>>, t: Throwable) {
                Toast.makeText(this@ModelListActivity, "Error de red: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}