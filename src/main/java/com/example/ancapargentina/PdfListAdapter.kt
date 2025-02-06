package com.example.ancapargentina
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class PdfListAdapter(private val pdfFiles: List<File>, private val onPdfClick: (File) -> Unit) :
    RecyclerView.Adapter<PdfListAdapter.PdfViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_pdf, parent, false)
        return PdfViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PdfViewHolder, position: Int) {
        val file = pdfFiles[position]
        holder.bind(file)
    }

    override fun getItemCount(): Int = pdfFiles.size

    inner class PdfViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val pdfNameTextView: TextView = itemView.findViewById(R.id.pdfName)

        fun bind(file: File) {
            pdfNameTextView.text = file.name
            itemView.setOnClickListener {
                onPdfClick(file)
            }
        }
    }
}

