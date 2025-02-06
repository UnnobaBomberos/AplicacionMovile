package com.example.ancapargentina.model

data class Marca(
    val id: Long,       // Ajusta estos tipos si es necesario según tu backend
    val nombre: String,
    val logoUrl: String // Asumimos que el logo es una URL
)
