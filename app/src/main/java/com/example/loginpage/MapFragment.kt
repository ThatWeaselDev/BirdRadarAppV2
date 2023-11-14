import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.loginpage.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

data class Observation(
    val lat: Double,
    val lng: Double,
    val comName: String,
    var birdName: String = ""
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

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var btnRecenter: ImageButton
    private lateinit var btnLayer: ImageButton
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var isSatelliteView = false
    private var selectedMarker: Marker? = null

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.ebird.org/v2/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val eBirdService = retrofit.create(EBirdService::class.java)

    interface DirectionsService {
        @GET("json")
        fun getDirections(
            @Query("origin") origin: String,
            @Query("destination") destination: String,
            @Query("key") apiKey: String
        ): Call<DirectionsResponse>
    }

    data class DirectionsResponse(
        val routes: List<Route>
    )

    data class Route(
        val legs: List<Leg>
    )

    data class Leg(
        val steps: List<Step>
    )

    data class Step(
        val start_location: Location,
        val end_location: Location
    )

    data class Location(
        val lat: Double,
        val lng: Double
    )

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        // Initialize UI components
        btnRecenter = view.findViewById(R.id.btnRecenter)
        btnLayer = view.findViewById(R.id.btnLayer)

        // Set up the map
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Set up click listeners
        btnRecenter.setOnClickListener { recenterMap() }
        btnLayer.setOnClickListener { showLayerOptions() }

        return view
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Check for location permissions
        if (isLocationPermissionGranted()) {
            // Enable user's location
            googleMap.isMyLocationEnabled = true

            // Initialize FusedLocationProviderClient
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

            // Fetch and display birding hotspots
            fetchAndDisplayHotspots()

            // Set up the marker click listener
            googleMap.setOnMarkerClickListener { marker ->
                // Handle marker click
                selectedMarker = marker
                // Center the map on the selected marker
                centerMapOnMarker(selectedMarker)
                // Show bird information
                selectedMarker?.let {
                    displayBirdInfo(it.title, it.position)
                }
                true
            }
        } else {
            // Request location permission
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        googleMap.setOnMapLongClickListener { latLng ->
            // Handle map long click
            selectedMarker = null
            // Clear previous routes, if any
            googleMap.clear()
            // Add a marker at the long click location
            val newMarker = googleMap.addMarker(MarkerOptions().position(latLng))
            selectedMarker = newMarker
        }
    }

    private fun displayBirdInfo(birdName: String?, coordinates: LatLng) {
        val infoString = "Bird Name: ${birdName.orEmpty()}\nCoordinates: $coordinates"
        // Example: Display in a Toast
        Toast.makeText(requireContext(), infoString, Toast.LENGTH_LONG).show()
    }

    private fun isLocationPermissionGranted(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    @SuppressLint("MissingPermission")
    private fun fetchAndDisplayHotspots() {
        // Fetch user's last known location
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            // Check if the location is not null
            if (location != null) {
                // Specify the radius around the user's location (in kilometers)
                val radius = 50

                // Call eBird API to get hotspots within the specified radius
                val call = eBirdService.getRecentObservations(
                    location.latitude,
                    location.longitude,
                    radius,
                    "pupv1pi6f4dh"
                )

                call.enqueue(object : Callback<List<Observation>> {
                    override fun onResponse(
                        call: Call<List<Observation>>,
                        response: Response<List<Observation>>
                    ) {
                        if (response.isSuccessful) {
                            val observations = response.body()
                            observations?.let {
                                for (observation in it) {
                                    // Populate the birdName property based on your API response structure
                                    observation.birdName = observation.comName
                                    val hotspotLocation = LatLng(observation.lat, observation.lng)
                                    val marker =
                                        MarkerOptions().position(hotspotLocation).title(observation.birdName)
                                    googleMap.addMarker(marker)
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<List<Observation>>, t: Throwable) {
                        t.printStackTrace()
                    }
                })

                // Move the camera to the user's location
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(location.latitude, location.longitude),
                        15f
                    )
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun drawRouteToSelectedMarker() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                selectedMarker?.let { marker ->
                    // Get the LatLng of the selected marker
                    val destination = marker.position
                    // Get the route between the current location and the destination
                    getDirections(location.latitude, location.longitude, destination.latitude, destination.longitude)
                }
            }
        }
    }

    private fun getDirections(
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double
    ) {
        val apiKey = "YOUR_GOOGLE_MAPS_API_KEY"
        val retrofitDirections = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/directions/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val directionsService = retrofitDirections.create(DirectionsService::class.java)

        val call = directionsService.getDirections(
            "$originLat,$originLng",
            "$destLat,$destLng",
            apiKey
        )

        call.enqueue(object : Callback<DirectionsResponse> {
            override fun onResponse(
                call: Call<DirectionsResponse>,
                response: Response<DirectionsResponse>
            ) {
                if (response.isSuccessful) {
                    val directionsResponse = response.body()
                    directionsResponse?.let {
                        if (it.routes.isNotEmpty() && it.routes[0].legs.isNotEmpty()) {
                            val steps = it.routes[0].legs[0].steps
                            val polylineOptions = PolylineOptions()
                            for (step in steps) {
                                polylineOptions.add(LatLng(step.start_location.lat, step.start_location.lng))
                                polylineOptions.add(LatLng(step.end_location.lat, step.end_location.lng))
                            }
                            drawPolyline(polylineOptions)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    private fun drawPolyline(polylineOptions: PolylineOptions) {
        // Clear previous polylines
        googleMap.clear()
        // Draw the new polyline
        googleMap.addPolyline(polylineOptions.color(Color.BLACK))
    }

    private fun recenterMap() {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    // Check if the location is not null
                    if (location != null) {
                        // Move camera to user's current location
                        val userLocation = LatLng(location.latitude, location.longitude)
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                        // Also, trigger the route drawing when recentering the map
                        drawRouteToSelectedMarker()
                    }
                }
        } catch (securityException: SecurityException) {
            securityException.printStackTrace()
        }
    }

    private fun showLayerOptions() {
        // Toggle between satellite and normal map types
        if (isSatelliteView) {
            googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        } else {
            googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        }

        // Update the flag
        isSatelliteView = !isSatelliteView
    }

    private fun centerMapOnMarker(marker: Marker?) {
        marker?.let {
            val markerPosition = it.position
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(markerPosition))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, initialize the map
                    onMapReady(googleMap)
                } else {
                    // Permission denied, handle accordingly
                    // You may show a dialog to the user explaining why you need the permission and then request it again
                }
            }
        }
    }
}
