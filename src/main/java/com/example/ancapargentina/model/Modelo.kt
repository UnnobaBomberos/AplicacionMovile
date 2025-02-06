package com.example.ancapargentina.model

data class Modelo(
    val id: Long,       // Ajusta estos tipos si es necesario según tu backend
    val nombre: String,
    val año: Int,
    val imageRes: String, // Asumimos que la imagen es una URL
    val pdf: String,
    val marca: Marca,
    val tipoCombustible: String,
    val tipoVehiculo: String
)
