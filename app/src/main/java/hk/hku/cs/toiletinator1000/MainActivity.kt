package hk.hku.cs.toiletinator1000

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.PopupWindow
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.slider.RangeSlider
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), HomeFragment.OnFragmentInteractionListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var auth: FirebaseAuth
    private lateinit var popupView: View
    private lateinit var popupWindow: PopupWindow

    public interface OnFilterListener {
        fun onFilter(minStars: Int, maxStars: Int, status: String, building: String)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Action bar stuff
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Navigation drawer stuff
        drawerLayout = findViewById(R.id.drawer_layout)
        actionBarDrawerToggle =
            ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close)
        actionBarDrawerToggle.syncState()

        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.logout -> {
                    auth.signOut()
                    Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            true
        }

        // Popup window stuff
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        popupView = inflater.inflate(R.layout.filter_popup, null)
        popupWindow = PopupWindow(
            popupView,
            Toolbar.LayoutParams.WRAP_CONTENT,
            Toolbar.LayoutParams.WRAP_CONTENT,
            true,
        )

        // Set the filter button to filter the toilets
        val filterButton: AppCompatButton = popupView.findViewById(R.id.filter_button)
        filterButton.setOnClickListener {
            val starsSlider: RangeSlider = popupView.findViewById(R.id.stars_slider)
            val minStars = starsSlider.values[0].toInt()
            val maxStars = starsSlider.values[1].toInt()
            val status =
                popupView.findViewById<Spinner>(R.id.status_spinner).selectedItem.toString()
            val building =
                popupView.findViewById<Spinner>(R.id.building_spinner).selectedItem.toString()

            val fragment =
                supportFragmentManager.findFragmentById(R.id.fragment_container)
//            if (fragment is HomeFragment) {
//                fragment.filterToilets(minStars, maxStars, status, building)
//            } else if (fragment is FavouritesFragment) {
//                fragment.filterToilets(minStars, maxStars, status, building)
//            }
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNavigationView.selectedItemId = R.id.home_page

        // Load the home fragment by default
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()).commit()
        }

        val homePage = findViewById<View>(R.id.home_page)
        homePage.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()).commit()
            bottomNavigationView.selectedItemId = R.id.home_page
        }

        val favPage = findViewById<View>(R.id.favourites_page)
        favPage.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, FavouritesFragment()).commit()
            bottomNavigationView.selectedItemId = R.id.favourites_page
        }
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

        // Popup window for filter
        val fragmentContainer: FrameLayout = findViewById(R.id.fragment_container)
        fragmentContainer.post {
            popupWindow.showAsDropDown(
                fragmentContainer,
                fragmentContainer.width,
                -fragmentContainer.height
            )
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * Called when the toilet data is passed from the HomeFragment.
     */
    fun onFragmentInteraction(toilets: List<Toilet>) {
        Log.d("MainActivity", "Toilet data received: $toilets")
        val statusSpinner: Spinner = popupView.findViewById(R.id.status_spinner)
        val buildingSpinner: Spinner = popupView.findViewById(R.id.building_spinner)

        // Add status and buildings to spinner values
        val statusList = mutableListOf<String>()
        val buildingList = mutableListOf<String>()
        statusList.add("All")
        buildingList.add("All")
        for (toilet in toilets) {
            if (!statusList.contains(toilet.status)) {
                statusList.add(toilet.status)
            }
            if (!buildingList.contains(toilet.building)) {
                buildingList.add(toilet.building)
            }
        }

        // Set spinner entries
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusList)
        val buildingAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, buildingList)
        statusSpinner.adapter = statusAdapter
        buildingSpinner.adapter = buildingAdapter
    }
}