package hk.hku.cs.toiletinator1000

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.Marker
import java.util.Locale


/**
 * The home page of the app with the map.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment(), OnMapReadyCallback, OnMyLocationButtonClickListener,
    OnMyLocationClickListener {
    private var permissionDenied = false
    private val db = Firebase.firestore
    private lateinit var mapView: MapView
    private lateinit var parentActivity: MainActivity
    private lateinit var map: GoogleMap

    //store all markers on the map
    private lateinit var allMarkers: MutableList<Marker>
    private lateinit var allToilets: MutableList<Toilet>

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchAdapter: SearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
        parentActivity = activity as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize the SearchAdapter with the correct click listener
        searchAdapter = SearchAdapter { selectedToilet ->
            val selectedMarker = allMarkers.find { it.tag == selectedToilet.toiletId }
            selectedMarker?.let {
                val position = it.position
                map.moveCamera(CameraUpdateFactory.newLatLng(position))
                it.showInfoWindow()
            }
        }

        recyclerView.adapter = searchAdapter

        val searchView = view.findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query.isNullOrEmpty()) {
                    filterMarkers("")
                }else {
                    filterMarkers(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    liveSearch("")
                }else{
                    liveSearch(newText)
                }
                return true
            }
        })

        return view
    }

    private fun liveSearch(newText: String) {
        val filteredToilets = if (newText.isNotBlank()) {
            val filteredResults = allMarkers.filter { marker ->
                val title = marker.title?.toLowerCase(Locale.getDefault())
                title?.contains(newText.toLowerCase(Locale.getDefault())) ?: false
            }
            filteredResults.mapNotNull { marker ->
                val toiletId = marker.tag as? String
                allToilets.find { it.toiletId == toiletId }
            }
        } else {
            allToilets // Return all toilets if query is blank
            allMarkers.forEach { it.isVisible = true }
            recyclerView.visibility = View.GONE
            return
        }

        // Update RecyclerView and markers based on the filteredToilets
        searchAdapter.updateResults(filteredToilets)
        updateMarkersVisibility(filteredToilets)
        updateRecyclerViewVisibility(filteredToilets)
    }

    private fun updateMarkersVisibility(filteredToilets: List<Toilet>) {
        allMarkers.forEach { marker ->
            val toiletId = marker.tag as? String
            val isVisible = filteredToilets.any { it.toiletId == toiletId }
            marker.isVisible = isVisible
        }
    }

    private fun updateRecyclerViewVisibility(filteredToilets: List<Toilet>) {
        recyclerView.visibility = if (filteredToilets.isNotEmpty()) View.VISIBLE else View.GONE
    }

    //filter and display markers based on query
    private fun filterMarkers(query: String) {
        Log.d("FilterMarkers", "Query: $query")
        if (query.isNullOrBlank()) {
            allMarkers.forEach { it.isVisible = true } // Show all markers
            recyclerView.visibility = View.GONE // Hide RecyclerView when showing all markers
            return
        }

        // Filter allMarkers based on the query and update the map markers accordingly
        val filteredMarkers = allMarkers.filter {
            val title = it.title?.toLowerCase()
            Log.d("FilterMarkers", "Marker title: ${it.title}")
            title?.contains(query.toLowerCase()) ?: false
        }
        // Show filtered markers on the map
        // For example, hide all markers and then show only the filtered ones
        allMarkers.forEach { it.isVisible = false }
        filteredMarkers.forEach { it.isVisible = true }
        Log.d("FilterMarkers", "Filtered markers count: ${filteredMarkers.size}")

        // Check if there are filtered markers and show/hide RecyclerView accordingly
        if (filteredMarkers.isNotEmpty()) {
            recyclerView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.GONE
        }

    }

    /**
     * This is the function that is called when the map is ready to be used.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        Log.d("HomeFragment", "Map ready")

        map.setOnMyLocationButtonClickListener(this)
        map.setOnMyLocationClickListener(this)
        enableMyLocation()

        // Set map UI settings
        map.uiSettings.isMyLocationButtonEnabled = true
        map.uiSettings.isZoomControlsEnabled = true

        // Set custom info window
        val infoWindow = CustomInfoWindow(parentActivity)
        map.setInfoWindowAdapter(infoWindow)
        map.setOnInfoWindowClickListener { marker ->
            // Launch ToiletDetailsActivity
            val intent = Intent(parentActivity, ToiletDetailsActivity::class.java)
            intent.putExtra("toiletId", marker.tag.toString())
            startActivity(intent)
            marker.hideInfoWindow()
        }

        // Retrieve toilets from database
        db.collection("Toilet")
            .get()
            .addOnSuccessListener { documents ->
                Log.d("HomeFragment", documents.toString())
                var toilets: List<Toilet> = documents.toObjects();

                Log.d("HomeFragment", "Toilets: ${toilets.size}")

                allMarkers = mutableListOf()
                allToilets = toilets.toMutableList()

                toilets.forEach { toilet ->
                    Log.d("HomeFragment", "Toilet: ${toilet.toiletId} ${toilet.floor} ${toilet.building} ${toilet.latitude} ${toilet.longitude} ${toilet.stars} ${toilet.status}")
                    val marker = map.addMarker(
                        MarkerOptions().position(LatLng(toilet.latitude, toilet.longitude))
                            .title("${toilet.floor} ${toilet.building}")
                            .snippet("Stars: ${toilet.stars}/5")
                    )
                    marker?.tag = toilet.toiletId
                    marker?.let { allMarkers.add(it) }
                }

                map.moveCamera(
                    CameraUpdateFactory.newLatLng(
                        LatLng(
                            22.28319531565826,
                            114.13741225026928
                        )
                    )
                )
                map.setMinZoomPreference(16f)
            }
            .addOnFailureListener { exception ->
                Log.e("Failure", "Error getting documents: ", exception)
                Toast.makeText(
                    parentActivity, "Error getting toilets",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    /**
     * Checks whether the given permissions are granted.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
            return
        }

        if (PermissionsUtils.isPermissionGranted(
                permissions,
                grantResults,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) || PermissionsUtils.isPermissionGranted(
                permissions,
                grantResults,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation()
        } else {
            // Permission was denied. Display an error message
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    override fun onResume() {
        mapView.onResume()
        super.onResume()
        if (permissionDenied) {
            // If permission denied, display error message
            Log.w("onResumeFragments", "Permission denied")
            AlertDialog.Builder(parentActivity)
                .setTitle("Location permission denied")
                .setMessage("You have denied location permissions, but you may still view the map. You can re-enable permissions in the app settings.")
                .setPositiveButton("OK", null)
                .show()
            permissionDenied = false
        }
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        mapView.onLowMemory()
        super.onLowMemory()
    }

    /**
     * Called when user clicks the My Location button. The camera animates to the user's current location.
     */
    override fun onMyLocationButtonClick(): Boolean {
        // Return false so that we don't consume the event and the default behavior still occurs
        return false
    }

    /**
     * Called when user clicks the blue dot representing their location.
     */
    override fun onMyLocationClick(location: Location) {
        Toast.makeText(parentActivity, "Current location:\n$location", Toast.LENGTH_SHORT).show()
    }


    /**
     * Enables the My Location layer if the fine location permission has been granted.
     * Code obtained from https://developers.google.com/maps/documentation/android-sdk/location
     */
    private fun enableMyLocation() {
        // 1. Check if permissions are granted, if so, enable the My Location layer
        if (ContextCompat.checkSelfPermission(
                parentActivity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                parentActivity,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.i("enableMyLocation", "Location permissions granted")
            map.isMyLocationEnabled = true
            return
        }

        // Note: Skipped permissions rationale

        // 2. Otherwise, request permission
        PermissionsUtils.requestPermissions(
            parentActivity,
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
