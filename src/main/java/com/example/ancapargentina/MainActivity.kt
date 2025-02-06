package com.example.ancapargentina

import android.text.Editable

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ancapargentina.model.Marca
import com.example.ancapargentina.network.RetrofitInstance
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.Manifest
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.widget.Toolbar


class MainMenuActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var brandAdapter: BrandAdapter
    private lateinit var searchInput: EditText
    private lateinit var allBrands: List<Marca>



    private val REQUEST_CODE_STORAGE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        recyclerView = findViewById(R.id.recyclerView)
        searchInput = findViewById(R.id.searchInput)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Configura el botón dentro del Toolbar
        val buttonLocalPdfs = findViewById<Button>(R.id.btnVerPdfsLocales)
        buttonLocalPdfs.setOnClickListener {
            val intent = Intent(this, LocalPdfActivity::class.java)
            startActivity(intent)
        }

        // Solicitar permisos de almacenamiento
        checkStoragePermission()

        // Verificar conexión a Internet y obtener marcas
        if (isInternetAvailable(this)) {
            obtenerMarcas()
        } else {
            Toast.makeText(this, "No hay conexión a Internet", Toast.LENGTH_SHORT).show()
        }
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarMarcas(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 (API 30) o superior
            if (Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivityForResult(intent, REQUEST_CODE_STORAGE)
            } else {
                Log.d("Permisos", "Acceso completo al almacenamiento concedido")
            }
        } else {
            // Android 10 y anteriores
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CODE_STORAGE
                )
            } else {
                Log.d("Permisos", "Acceso completo al almacenamiento concedido")
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_STORAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
                Log.d("Permisos", "Permiso concedido después de la solicitud")
            } else {
                Toast.makeText(this, "Se necesita acceso completo al almacenamiento", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun obtenerMarcas() {
        RetrofitInstance.apiService.obtenerTodasLasMarcas().enqueue(object : Callback<List<Marca>> {
            override fun onResponse(call: Call<List<Marca>>, response: Response<List<Marca>>) {
                if (response.isSuccessful) {
                    val marcas = response.body()
                    Log.d("MainMenuActivity", "Marcas obtenidas: $marcas")
                    marcas?.let {
                        val marcasOrdenadas = it.sortedBy { marca -> marca.nombre }
                        GlobalScope.launch {
                            runOnUiThread {
                                allBrands = it.sortedBy { marca -> marca.nombre }
                                brandAdapter = BrandAdapter(marcasOrdenadas) { brand ->
                                    val intent = Intent(this@MainMenuActivity, ModelListActivity::class.java)
                                    intent.putExtra("marcaId", brand.id)
                                    intent.putExtra("marca_name", brand.nombre)
                                    intent.putExtra("marca_logo", brand.logoUrl)
                                    startActivity(intent)
                                }
                                recyclerView.adapter = brandAdapter
                            }
                        }
                    }
                } else {
                    Toast.makeText(this@MainMenuActivity, "Error al obtener marcas", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Marca>>, t: Throwable) {
                Log.e("MainMenuActivity", "Error de red: ${t.message}")
                Toast.makeText(this@MainMenuActivity, "Error de red: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    private fun filtrarMarcas(query: String) {
        val filteredBrands = allBrands.filter {
            it.nombre.contains(query, ignoreCase = true)
        }
        brandAdapter.updateData(filteredBrands)
    }

}