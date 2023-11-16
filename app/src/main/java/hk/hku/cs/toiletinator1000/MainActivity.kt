package hk.hku.cs.toiletinator1000

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setSupportActionBar(findViewById(R.id.toolbar))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflator: MenuInflater = menuInflater
        inflator.inflate(R.menu.filter, menu)
        return true
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true

        // Add markers and move the camera
        val coordinates: List<LatLng> = listOf(
            LatLng(22.28319531565826, 114.13741225026928),
            LatLng(22.2832596416007, 114.13805203112183),
            LatLng(22.283202080716922, 114.13785903798951),
        )
        var count = 0
        coordinates.forEach(fun (marker){
            mMap.addMarker(MarkerOptions().position(marker).title("Toilet $count").snippet("Very nice toilet"))
            count++
        })

        mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(22.28319531565826, 114.13741225026928)))
        mMap.setMinZoomPreference(16f)
    }
}