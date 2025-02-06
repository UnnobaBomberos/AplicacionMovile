package com.example.ancapargentina

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.example.ancapargentina.model.Modelo

class ModeloAdapter(
    private val modelos: List<Modelo>,
    private val onClick: (Modelo) -> Unit
) : RecyclerView.Adapter<ModeloAdapter.ModeloViewHolder>() {

    inner class ModeloViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val imagenUrl: ImageView = view.findViewById(R.id.modelImage)
        val nombre: TextView = view.findViewById(R.id.modelName)

        fun bind(modelo: Modelo) {
            Log.d("ModeloAdapter", "Modelo recibido: ${modelo.nombre}, Imagen URL: ${modelo.imageRes}")

            // Construir la URL completa de la imagen
            val baseUrl = "http://192.168.0.111:8080/api/files/files/" // Cambia esto a tu IP y puerto correctos
            val imageUrl = baseUrl + modelo.imageRes

            // Cargar la imagen con Picasso
            Picasso.get()
                .load(imageUrl)
                .into(imagenUrl)

            nombre.text = modelo.nombre
            view.setOnClickListener { onClick(modelo) }
            Log.d("ModeloAdapter", "URL de la imagen cargada: $imageUrl")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModeloViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.modelo_item, parent, false)
        return ModeloViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModeloViewHolder, position: Int) {
        holder.bind(modelos[position])
    }

    override fun getItemCount(): Int = modelos.size
}
