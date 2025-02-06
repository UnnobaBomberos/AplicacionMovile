package com.example.ancapargentina.network


import com.example.ancapargentina.model.Marca
import com.example.ancapargentina.model.Modelo
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("api/marcas")
    fun obtenerTodasLasMarcas(): Call<List<Marca>>

    @GET("/api/modelos/{id}")
    fun obtenerModeloPorId(@Path("id") modeloId: Long): Call<Modelo>

    @GET("/api/modelos/por-marca/{marcaId}")
    fun obtenerModelosPorMarca(@Path("marcaId") marcaId: Long): Call<List<Modelo>>

    // Endpoint para obtener una imagen por nombre
    @GET("/api/files/files/{fileName}")
    fun obtenerImagen(@Path("fileName") fileName: String): Call<ResponseBody>  // Usamos ResponseBody para obtener el contenido del archivo

    @GET("api/modelos") // Ajusta el endpoint según corresponda
    fun obtenerTodosLosModelos(): Call<List<Modelo>>

    @GET("/api/files/download/{fileName}")
    fun descargarPdf(@Path("fileName") fileName: String): Call<ResponseBody>
}
