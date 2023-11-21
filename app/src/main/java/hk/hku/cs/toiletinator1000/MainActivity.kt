package hk.hku.cs.toiletinator1000

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObjects
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity(), OnMapReadyCallback, OnMyLocationButtonClickListener,
    OnMyLocationClickListener {

    private var permissionDenied = false

    private lateinit var mMap: GoogleMap
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    private lateinit var auth: FirebaseAuth
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setSupportActionBar(findViewById(R.id.toolbar))

        drawerLayout = findViewById(R.id.drawer_layout)
        actionBarDrawerToggle =
            ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close)
        actionBarDrawerToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /**
     * Called when the menu is created.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflator: MenuInflater = menuInflater
        inflator.inflate(R.menu.filter, menu)
        return true
    }

    /**
     * Called when an item in the menu is selected.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * This is the function that is called when the map is ready to be used.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnMyLocationButtonClickListener(this)
        mMap.setOnMyLocationClickListener(this)
        enableMyLocation()

        // Set map UI settings
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true

        // Set custom info window
        val infoWindow = CustomInfoWindow(this)
        mMap.setInfoWindowAdapter(infoWindow)
        mMap.setOnInfoWindowClickListener { marker ->
            Toast.makeText(
                this, marker.title,
                Toast.LENGTH_SHORT
            ).show()
            // Launch ToiletDetailsActivity
            val intent = Intent(this, ToiletDetailsActivity::class.java)
            intent.putExtra("toiletLocation", marker.title)
            intent.putExtra("toiletStars", marker.snippet)
            startActivity(intent)
            marker.hideInfoWindow()
        }

        // Retrive toilets from database
        db.collection("Toilet")
            .get()
            .addOnSuccessListener { documents ->
                var toilets: List<Toilet> = documents.toObjects<Toilet>();

                toilets.forEach(fun(toilet: Toilet) {

                    Log.d("AHAHAHAHAHHAHAHAHAHA", toilet.building)
                    mMap.addMarker(
                        MarkerOptions().position(LatLng(toilet.latitude, toilet.longitude))
                            .title("${toilet.floor} ${toilet.building}")
                            .snippet("Stars: ${toilet.stars}/ 5")
                    )
                })

                mMap.moveCamera(
                    CameraUpdateFactory.newLatLng(
                        LatLng(
                            22.28319531565826,
                            114.13741225026928
                        )
                    )
                )
                mMap.setMinZoomPreference(16f)
            }
            .addOnFailureListener { exception ->
                Log.d("Failure", "Error getting documents: ", exception)
            }

    }

    /**
     * Called when user clicks the My Location button. The camera animates to the user's current location.
     */
    override fun onMyLocationButtonClick(): Boolean {
        // Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT)
        //ll    .show()
        // Return false so that we don't consume the event and the default behavior still occurs
        return false
    }

    /**
     * Called when user clicks the blue dot representing their location.
     */
    override fun onMyLocationClick(location: Location) {
        Toast.makeText(this, "Current location:\n$location", Toast.LENGTH_LONG).show()
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
    override fun onResumeFragments() {
        super.onResumeFragments()
        if (permissionDenied) {
            // If permission denied, display error message
            Log.w("onResumeFragments", "Permission denied")
            AlertDialog.Builder(this)
                .setTitle("Location permission denied")
                .setMessage("You have denied location permissions, but you may still view the map. You can re-enable permissions in the app settings.")
                .setPositiveButton("OK", null)
                .show()
            permissionDenied = false
        }
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     * Code obtained from https://developers.google.com/maps/documentation/android-sdk/location
     */
    private fun enableMyLocation() {
        // 1. Check if permissions are granted, if so, enable the My Location layer
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.i("enableMyLocation", "Location permissions granted")
            mMap.isMyLocationEnabled = true
            return
        }

        // Note: Skipped permissions rationale

        // 2. Otherwise, request permission
        PermissionsUtils.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    /**
     * Called when the user clicks the "Allow" or "Deny" button in the permission request dialog.
     */
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}