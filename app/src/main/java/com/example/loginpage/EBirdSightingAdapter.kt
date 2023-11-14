package com.example.loginpage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BirdSightingAdapter(
    private val birdSightings: List<BirdSighting>,
    private val listener: OnItemClickListener? = null
) : RecyclerView.Adapter<BirdSightingAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val speciesNameTextView: TextView = itemView.findViewById(R.id.textSpeciesName)
        val locationTextView: TextView = itemView.findViewById(R.id.textLocation)
        val dateTextView: TextView = itemView.findViewById(R.id.textDate)
        val descriptionTextView: TextView = itemView.findViewById(R.id.textSpeciesDescription)

        init {
            // Set click listener for the whole card
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener?.onItemClick(position)
                }
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_ebird, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val birdSighting = birdSightings[position]
        holder.speciesNameTextView.text = birdSighting.speciesName
        holder.locationTextView.text = "${birdSighting.lat}, ${birdSighting.lng}" // Display coordinates
        holder.dateTextView.text = birdSighting.date
        holder.descriptionTextView.text = birdSighting.description
    }

    override fun getItemCount(): Int {
        return birdSightings.size
    }
}
