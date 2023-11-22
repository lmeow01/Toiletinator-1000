package hk.hku.cs.toiletinator1000

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class ToiletDetailsActivity : AppCompatActivity() {

    private var isAddReviewVisible = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toilet_details)

        setSupportActionBar(findViewById(R.id.toolbar))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val toiletLocation = intent.getStringExtra("toiletLocation")
        val toiletDetailsLocation: TextView = findViewById(R.id.toilet_details_location)
        toiletDetailsLocation.text = toiletLocation

        val toiletStars = intent.getStringExtra("toiletStars")
        val toiletDetailsStars: TextView = findViewById(R.id.toilet_details_stars)
        toiletDetailsStars.text = toiletStars

        val addReviewButton: Button = findViewById(R.id.button_add_review)
        addReviewButton.setOnClickListener {
            toggleAddReviewFragment()
        }
    }

    private fun toggleAddReviewFragment() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val fragment = ReviewsFragment()

        if (isAddReviewVisible) {
            fragmentTransaction.remove(fragment)
        } else {
            fragmentTransaction.replace(R.id.reviews_container, fragment)
        }

        fragmentTransaction.commit()
        isAddReviewVisible = !isAddReviewVisible
    }
}