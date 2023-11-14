package com.example.loginpage

import MapFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

data class BirdSighting(
    val speciesName: String,
    val lat: Double,
    val lng: Double,
    val date: String,
    val description: String
)

class CustomBirdSightingAdapter(private val birdSightings: List<BirdSighting>) :
    RecyclerView.Adapter<CustomBirdSightingAdapter.ViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val speciesNameTextView: TextView = itemView.findViewById(R.id.textSpeciesName)
        val locationTextView: TextView = itemView.findViewById(R.id.textLocation)
        val dateTextView: TextView = itemView.findViewById(R.id.textDate)
        val descriptionTextView: TextView = itemView.findViewById(R.id.textSpeciesDescription)

        init {
            itemView.setOnClickListener {
                onItemClickListener?.onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_ebird, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val birdSighting = birdSightings[position]
        holder.speciesNameTextView.text = birdSighting.speciesName
        holder.locationTextView.text = "Lat: ${birdSighting.lat}, Lng: ${birdSighting.lng}"
        holder.dateTextView.text = birdSighting.date
        holder.descriptionTextView.text = birdSighting.description
    }

    override fun getItemCount(): Int {
        return birdSightings.size
    }
}

data class Observation(
    val lat: Double,
    val lng: Double,
    val comName: String,
    val obsDt: String,
    val description: String
)

interface EBirdService {
    @GET("data/obs/geo/recent")
    fun getRecentObservations(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
        @Query("dist") distance: Int,
        @Header("X-eBirdApiToken") apiKey: String
    ): Call<List<Observation>>
}

class BirdFragment : Fragment(), CustomBirdSightingAdapter.OnItemClickListener {

    private lateinit var birdSightings: List<BirdSighting>
    private val apiKey = "pupv1pi6f4dh"

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.ebird.org/v2/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val eBirdService = retrofit.create(EBirdService::class.java)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_bird, container, false)

        val recyclerView: RecyclerView = rootView.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val call = eBirdService.getRecentObservations(
            latitude = 40.7128, // Replace with your latitude
            longitude = -74.0060, // Replace with your longitude
            distance = 10, // Replace with the desired distance
            apiKey = apiKey
        )

        call.enqueue(object : Callback<List<Observation>> {
            override fun onResponse(call: Call<List<Observation>>, response: Response<List<Observation>>) {
                if (response.isSuccessful) {
                    birdSightings = response.body()?.map {
                        BirdSighting(
                            speciesName = it.comName,
                            lat = it.lat,
                            lng = it.lng,
                            date = it.obsDt,
                            description = it.description ?: ""
                        )
                    } ?: emptyList()
                    val adapter = CustomBirdSightingAdapter(birdSightings)
                    recyclerView.adapter = adapter

                    // Set item click listener
                    adapter.setOnItemClickListener(this@BirdFragment)
                } else {
                    // Handle API response error
                }
            }

            override fun onFailure(call: Call<List<Observation>>, t: Throwable) {
                // Handle network request failure
            }
        })

        return rootView
    }

    override fun onItemClick(position: Int) {
        // Handle item click, e.g., navigate to the pin on the map
        val birdSighting = birdSightings[position]
        val lat = birdSighting.lat
        val lng = birdSighting.lng

        // Navigate to MapFragment
        val mapFragment = MapFragment()
        val bundle = Bundle()
        bundle.putDouble("lat", lat)
        bundle.putDouble("lng", lng)
        mapFragment.arguments = bundle

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, mapFragment)
            .addToBackStack(null)
            .commit()
    }
}
