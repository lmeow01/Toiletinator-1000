package hk.hku.cs.toiletinator1000

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.Gallery
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.widget.CompoundButtonCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

const val REQUEST_CODE = 42

class ToiletDetailsActivity : AppCompatActivity() {

    private var isAddReviewVisible = false
    private val storage = Firebase.storage
    private val db = Firebase.firestore
    private lateinit var popupView: View
    private lateinit var popupWindow: PopupWindow

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

        val toiletId = intent.getStringExtra("toiletId")

        val toiletDetailsLocation: TextView = findViewById(R.id.toilet_details_location)
        val toiletDetailsStars: TextView = findViewById(R.id.toilet_details_stars)
        val toiletDetailsStatus: TextView = findViewById(R.id.toilet_details_status)

        // Image gallery setup
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        popupView = inflater.inflate(R.layout.gallery_popup, null)
        popupWindow = PopupWindow(
            popupView,
            Toolbar.LayoutParams.MATCH_PARENT,
            Toolbar.LayoutParams.WRAP_CONTENT,
            true,
        )
        val toiletDetailsImage: ImageView = findViewById(R.id.toilet_details_image)
        toiletDetailsImage.setOnClickListener {
            popupWindow.showAsDropDown(it)
        }

        if (toiletId == null) {
            Toast.makeText(this, "Failed to get toilet", Toast.LENGTH_SHORT).show()
            return
        }

        // Query the toilet with the given toiletId
        GlobalScope.launch(Dispatchers.IO) {
            val toilet = db.collection("Toilet").document(toiletId!!).get().await()
                .toObject(Toilet::class.java)
            if (toilet == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ToiletDetailsActivity,
                        "Failed to get toilet",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@launch
            }

            val images = toilet.images.filter { it != "" }
            val imageRefs = images.map { storage.reference.child(it) }
            val ONE_MEGABYTE: Long = 1024 * 1024
            val bitmaps =
                imageRefs.map { async { it.getBytes(ONE_MEGABYTE).await() } }?.map { it.await() }
                    ?.map { BitmapFactory.decodeByteArray(it, 0, it.size) }

            withContext(Dispatchers.Main) {
                toiletDetailsLocation.text = toilet.floor + " " + toilet.building
                toiletDetailsStars.text = "Stars: ${toilet.stars} / 5"
                toiletDetailsStatus.text = "Status: ${toilet.status}"

                val gallery: Gallery = popupView.findViewById(R.id.gallery)
                gallery.adapter = GalleryAdapter(this@ToiletDetailsActivity, bitmaps!!)
                if (bitmaps.isEmpty()) {
                    // Show "No images" text
                    val noImagesText: TextView = popupView.findViewById(R.id.no_images_text)
                    noImagesText.visibility = View.VISIBLE
                } else {
                    // Set toilet details image to the first image
                    toiletDetailsImage.setImageBitmap(bitmaps[0])
                }
            }
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
        userDocumentRef.get().addOnSuccessListener { documentSnapshot ->
            val user = documentSnapshot.toObject(User::class.java)
            if (user != null) {
                val favList = user.Fav.toMutableList() // Convert to mutable list
                val isToiletIdFav = favList.contains(toiletId)

                if (isToiletIdFav) {
                    favouriteCheckbox.isChecked = isToiletIdFav
                    CompoundButtonCompat.setButtonTintList(
                        favouriteCheckbox, ColorStateList.valueOf(
                            ContextCompat.getColor(this, R.color.red)
                        )
                    )
                }

                favouriteCheckbox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        CompoundButtonCompat.setButtonTintList(
                            favouriteCheckbox, ColorStateList.valueOf(
                                ContextCompat.getColor(this, R.color.red)
                            )
                        )
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
                        CompoundButtonCompat.setButtonTintList(
                            favouriteCheckbox, ColorStateList.valueOf(
                                ContextCompat.getColor(this, R.color.grey)
                            )
                        )
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

        db.collection("Review")
            .whereEqualTo("toiletId", toiletId)
            .get()
            .addOnSuccessListener { documents ->
                val fragmentTransaction = supportFragmentManager.beginTransaction()
                val fragment = ReviewsFragment()

                val mBundle = Bundle()
                mBundle.putString("toiletId", intent.getStringExtra("toiletId").toString())
                fragment.arguments = mBundle
                fragmentTransaction.replace(R.id.reviews_container, fragment)

                fragmentTransaction.commit()
            }
            .addOnFailureListener { exception ->
                Log.d("ReviewsFragment", exception.toString())
            }
        // Review Fragment
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val fragment = ReviewsFragment()

        val mBundle = Bundle()
        Log.d("toiletid", toiletId.toString())
        mBundle.putString("toiletId", toiletId.toString())
        fragment.arguments = mBundle
        fragmentTransaction.replace(R.id.reviews_container, fragment)
        fragmentTransaction.commit()
    }


//    private fun toggleAddReviewFragment() {
//        val fragmentTransaction = supportFragmentManager.beginTransaction()
//        val fragment = ReviewsFragment()
//
//        val mBundle = Bundle()
//        Log.d("toiletid", intent.getStringExtra("toiletId").toString())
//        mBundle.putString("toiletId", intent.getStringExtra("toiletId").toString())
//        fragment.arguments = mBundle
//
//        if (isAddReviewVisible) {
//            fragmentTransaction.remove(fragment)
//        } else {
//            fragmentTransaction.replace(R.id.reviews_container, fragment)
//        }
//
//        fragmentTransaction.commit()
//        isAddReviewVisible = !isAddReviewVisible
//    }

}

class GalleryAdapter(private val context: Context, private val images: List<Bitmap>) :
    BaseAdapter() {

    override fun getCount(): Int {
        return images.size
    }

    override fun getItem(position: Int): Any {
        return images[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, view: View?, parent: android.view.ViewGroup?): View {
        val imageView = ImageView(context)
        imageView.setImageBitmap(images[position])
        imageView.layoutParams = Gallery.LayoutParams(300, 300)
        imageView.scaleType = ImageView.ScaleType.FIT_XY

        return imageView
    }
}

private fun Bundle.putSerializable(s: String, reviews: List<Review>) {

}
