package com.example.ancapargentina

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ancapargentina.model.Marca
import com.squareup.picasso.Picasso // Importa Picasso

// El adaptador ahora trabaja con objetos Marca, no Brand
class BrandAdapter(
    private var brands: List<Marca>,
    private val onClick: (Marca) -> Unit
) : RecyclerView.Adapter<BrandAdapter.BrandViewHolder>() {

    inner class BrandViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val logo: ImageView = view.findViewById(R.id.brandLogo)
        val name: TextView = view.findViewById(R.id.brandName)

        fun bind(brand: Marca) {
            // Modificar la URL para incluir la IP de tu computadora
            val baseUrl = "http://192.168.0.111:8080/api/files/files/"
            val imageUrl = baseUrl + brand.logoUrl // Concatenar la URL base con el nombre del archivo

            // Usar Picasso para cargar la imagen directamente desde la URL
            Picasso.get()
                .load(imageUrl) // Cargar directamente la imagen desde la URL
                .into(logo)

            Log.d("BrandAdapter", "URL de la imagen: $imageUrl")

            name.text = brand.nombre
            view.setOnClickListener { onClick(brand) }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.brand_item, parent, false)
        return BrandViewHolder(view)
    }

    override fun onBindViewHolder(holder: BrandViewHolder, position: Int) {
        holder.bind(brands[position])
    }

    override fun getItemCount(): Int = brands.size

    fun updateData(newBrands: List<Marca>) {
        brands = newBrands
        notifyDataSetChanged()
    }
}
