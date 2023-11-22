package hk.hku.cs.toiletinator1000

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.firebase.Firebase
import com.google.firebase.storage.storage

const val REQUEST_CODE = 42

class ToiletDetailsActivity : AppCompatActivity() {

    private var isAddReviewVisible = false

    private val storage = Firebase.storage

    // An activity result launcher for selecting images from the gallery
    private val uploadImageFromGallery =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri != null) {
                FileUtils.uploadFile(uri, "images", "lmao").addOnSuccessListener {
                    Toast.makeText(this, "Upload successful", Toast.LENGTH_SHORT).show()
                    Log.d("ToiletDetailsActivity", "Upload successful")
                }.addOnFailureListener {
                    Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
                    Log.e("ToiletDetailsActivity", "Upload failed")
                }
            }
        }

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