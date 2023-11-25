package hk.hku.cs.toiletinator1000

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.CompoundButtonCompat
import com.google.common.reflect.TypeToken
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.storage

const val REQUEST_CODE = 42

class ToiletDetailsActivity : AppCompatActivity() {

    private var isAddReviewVisible = false

    private val storage = Firebase.storage
    private val db = Firebase.firestore

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

        val addReviewButton: Button = findViewById(R.id.button_add_review)
        addReviewButton.setOnClickListener {
            toggleAddReviewFragment()
        }

        val toiletId = intent.getStringExtra("toiletId")

        val toiletDetailsLocation: TextView = findViewById(R.id.toilet_details_location)
        val toiletDetailsStars: TextView = findViewById(R.id.toilet_details_stars)
        val toiletDetailsStatus: TextView = findViewById(R.id.toilet_details_status)

        // Query the toilet with the given toiletId
        db.collection("Toilet").document(toiletId!!).get().addOnSuccessListener { document ->
            val toilet = document.toObject(Toilet::class.java)
            if (toilet != null) {
                toiletDetailsLocation.text = toilet.floor + " " + toilet.building
                toiletDetailsStars.text = "Stars: ${toilet.stars} / 5"
                toiletDetailsStatus.text = "Status: ${toilet.status}"

                // Load the first image of the toilet
                val toiletDetailsImage: ImageView = findViewById(R.id.toilet_details_image)
                if (toilet.images.size > 0) {
                    val imageRef = storage.reference.child(toilet.images[0])
                    val ONE_MEGABYTE: Long = 1024 * 1024
                    imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener { bytes ->
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        toiletDetailsImage.setImageBitmap(bitmap)
                    }.addOnFailureListener {
                        Log.e("ToiletDetailsActivity", "Failed to get image")
                    }
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to get toilet details", Toast.LENGTH_SHORT).show()
        }

        // Report button
        val reportButton: ImageButton = findViewById(R.id.button_report)
        reportButton.setOnClickListener {
            // Dialog
            AlertDialog.Builder(this)
                .setTitle("Report Toilet Status")
                .setMessage("Select a status to report")
                .setPositiveButton("Available") { _, _ ->
                    db.collection("Toilet").document(toiletId).update("status", "Available")
                        .addOnSuccessListener {
                            Toast.makeText(this, "Reported", Toast.LENGTH_SHORT).show()
                            toiletDetailsStatus.text = "Status: Available"
                        }.addOnFailureListener {
                            Toast.makeText(this, "Failed to report", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Unavailable") { _, _ ->
                    db.collection("Toilet").document(toiletId).update("status", "Unavailable")
                        .addOnSuccessListener {
                            Toast.makeText(this, "Reported", Toast.LENGTH_SHORT).show()
                            toiletDetailsStatus.text = "Status: Unavailable"
                        }.addOnFailureListener {
                            Toast.makeText(this, "Failed to report", Toast.LENGTH_SHORT).show()
                        }
                }
                .show()
        }

        //Favourite Checkbox
        val favouriteCheckbox: CheckBox = findViewById(R.id.favourite_checkBox)

        //check current user UID
        val currentUserUID = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Get the current user's document reference
        val userDocumentRef = db.collection("User").document(currentUserUID)

        //Retrieve the current user's data
        userDocumentRef.get().addOnSuccessListener{ documentSnapshot ->
            val user = documentSnapshot.toObject(User::class.java)
            if (user != null) {
                val favList = user.Fav.toMutableList() // Convert to mutable list
                val isToiletIdFav = favList.contains(toiletId)

                favouriteCheckbox.isChecked = isToiletIdFav

                favouriteCheckbox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        CompoundButtonCompat.setButtonTintList(favouriteCheckbox, ColorStateList.valueOf(
                            ContextCompat.getColor(this, R.color.red)))
                        if (!favList.contains(toiletId)) {
                            favList.add(toiletId)
                            // Update Fav list in Firestore
                            userDocumentRef.update("Fav", favList)
                                .addOnSuccessListener {
                                    Log.d("Firestore", "FavList updated successfully")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firestore", "Error updating FavList: $e")
                                }
                        }
                        //checkbox is unchecked
                    } else {
                        CompoundButtonCompat.setButtonTintList(favouriteCheckbox, ColorStateList.valueOf(
                            ContextCompat.getColor(this, R.color.grey)))
                        favList.remove(toiletId)
                        // Update Fav list in Firestore
                        userDocumentRef.update("Fav", favList)
                            .addOnSuccessListener {
                                Log.d("Firestore", "FavList updated successfully")
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore", "Error updating FavList: $e")
                            }
                    }
                }
            } else {
                // User document doesn't exist (this should be handled accordingly)
                Log.d("Firestore", "User document doesn't exist for ID: $currentUserUID")
            }
        }.addOnFailureListener { e ->
            Log.e("Firestore", "Error fetching user document: $e")
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