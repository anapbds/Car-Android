package com.example.myapitest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapitest.R
import com.example.myapitest.model.Item
import com.example.myapitest.ui.loadUrl
import com.squareup.picasso.Picasso

class ItemAdapter(
    private val items: List<Item>,
    private val onItemClick: (Item) -> Unit,
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {
    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView = view.findViewById<ImageView>(R.id.image)
        val modelTextView = view.findViewById<TextView>(R.id.model)
        val yearTextView = view.findViewById<TextView>(R.id.year)
        val licenseTextView = view.findViewById<TextView>(R.id.license)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_car_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemAdapter.ItemViewHolder, position: Int) {
        val item = items[position]
        holder.modelTextView.text = item.name ?: "Sem nome"
        holder.yearTextView.text = item.year ?: "N/A"
        holder.licenseTextView.text = item.licence ?: "N/A"
        holder.imageView.loadUrl(item.imageUrl)
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = items.size
}
