package hk.hku.cs.toiletinator1000

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.math.max

/**
 * A simple [Fragment] subclass.
 * Use the [ReviewsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ReviewsFragment : Fragment(), AddReviewFragment.ReviewSubmissionListener {
    private var toiletId: String? = null

    //for displaying ratings and descriptions

    private var reviews = ArrayList<Review>()
    private val db = Firebase.firestore
    private lateinit var auth: FirebaseAuth
    private lateinit var parentActivity: ToiletDetailsActivity
    private lateinit var reviewsAdapter: ReviewsAdapter
    private var currentUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            toiletId = it.getString("toiletId")
        }

        auth = FirebaseAuth.getInstance()
        parentActivity = activity as ToiletDetailsActivity
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_reviews, container, false)
        val buttonAddReview = view.findViewById<Button>(R.id.button_add_review)
        val addReviewContainer = view.findViewById<FrameLayout>(R.id.addReviewContainer)

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            currentUserId = currentUser.uid
        } else {
            Log.d("Failed", "Find User : $currentUser")
        }

        buttonAddReview.setOnClickListener {
            Log.d("Button Click", "Add Review Button Click")

            val fragmentManager = childFragmentManager

            if (addReviewContainer.childCount == 0) {
                val addReviewFragment = AddReviewFragment()
                var mBundle = Bundle()
                mBundle.putString("toiletId", arguments?.getString("toiletId").toString())
                addReviewFragment.arguments = mBundle

                addReviewFragment.setReviewSubmissionListener(this)

                fragmentManager.beginTransaction()
                    .replace(R.id.addReviewContainer, addReviewFragment)
                    .addToBackStack(null)
                    .commit()
            }

            addReviewContainer.visibility =
                if (addReviewContainer.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        Log.d("REVIEWSSSSSSS", reviews.size.toString())
        val recyclerView: RecyclerView = view.findViewById(R.id.reviews_recycler_view)
        reviewsAdapter = ReviewsAdapter(
            this,
            reviews,
            requireContext(),
            currentUserId,
            { review -> deleteReview(review) })
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        recyclerView.adapter = reviewsAdapter

        db.collection("Review")
            .whereEqualTo("toiletId", arguments?.getString("toiletId").toString())
            .get()
            .addOnSuccessListener { documents ->
                reviews.clear()
                val reviewList = documents.toObjects(Review::class.java)
                for (review in reviewList) {
                    reviews.add(review)
                    reviewsAdapter.notifyDataSetChanged()
                }
            }

        return view
    }


    override fun onSubmitReview() {
        val addReviewContainer = view?.findViewById<FrameLayout>(R.id.addReviewContainer)
        addReviewContainer?.visibility = View.GONE
    }

    private fun deleteReview(review: Review) {
        val reviewId = review.reviewId

        db.collection("Review")
            .document(reviewId!!)
            .delete()
            .addOnSuccessListener { _ ->
                // Delete successful, remove the review from the local list and update the RecyclerView
                reviews.remove(review)

                // Recalculate the average rating
                var total = 0.0
                for (review in reviews) {
                    total += review.stars!!
                }
                val avgRating = total / max(reviews.size, 1)
                val formattedStars = String.format("%.2f", avgRating)

                // Update the toilet's average rating in Firestore
                db.collection("Toilet")
                    .document(arguments?.getString("toiletId").toString())
                    .update("stars", avgRating)
                    .addOnSuccessListener {
                        // Update successful
                        Log.d("Delete Review", "Review deleted successfully")
                        val intent = Intent(this.parentActivity, ToiletDetailsActivity::class.java)
                        intent.putExtra("toiletId", toiletId)
                        startActivity(intent)
                    }
                    .addOnFailureListener { exception ->
                        // Handle Firestore update failure
                        Log.e("Delete Review", "Error updating toilet average rating: $exception")
                    }

                // Clear and re-populate the list after deletion
                reviews.clear()
                // Fetch updated data from Firestore
                fetchReviewsFromFirestore()
            }
            .addOnFailureListener { exception ->
                // Handle Firestore query failure
                Log.e("Query Review", "Error getting review for deletion: $exception")
            }
    }


    private fun fetchReviewsFromFirestore() {
        db.collection("Review")
            .whereEqualTo("toiletId", arguments?.getString("toiletId").toString())
            .get()
            .addOnSuccessListener { documents ->
                val reviewList = documents.toObjects(Review::class.java)
                reviews.addAll(reviewList)
                reviewsAdapter.notifyDataSetChanged()
            }
    }


    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() = ReviewsFragment().apply {
            arguments = Bundle().apply {}
        }
    }
}

class ReviewsAdapter(
    val fragment: Fragment,
    private val mReviews: ArrayList<Review>,
    private val context: Context,
    private val currentUserId: String,
    private val onDeleteClickListener: (Review) -> Unit
) :
    RecyclerView.Adapter<ReviewsAdapter.ViewHolder>() {


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rating: TextView
        val comment: TextView
        val deleteButton: ImageButton

        val userId: TextView
        val auth: FirebaseAuth

        init {
            rating = view.findViewById(R.id.displayRating)
            comment = view.findViewById(R.id.displayReviewDesc)
            deleteButton = view.findViewById(R.id.delete_review_button)
            // Can add review modifying section in the future
            userId = view.findViewById(R.id.displayUserId)
            auth = FirebaseAuth.getInstance()
            // Can add review modifying section in the future
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.review_list, parent, false)
        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val review = mReviews[position]
        holder.rating.text = review.stars.toString()
        holder.comment.text = review.comment.toString()
        holder.userId.text = review.userId.toString()

        if (holder.userId.text != holder.auth.uid) {
            val rootView = holder.rating.rootView;
            rootView.setBackgroundColor(Color.parseColor("#ffffff"))
        }

        val addReviewContainer = fragment.view?.findViewById<FrameLayout>(R.id.addReviewContainer)
        holder.itemView.setOnClickListener {
            if (holder.userId.text == holder.auth.uid) {
                Log.d("Button Click", "Modify Review Button Click")

                val fragmentManager = fragment.childFragmentManager

                if (addReviewContainer?.childCount == 0) {
                    val modifyReviewFragment = ModifyReviewFragment()
                    var mBundle = Bundle()
                    mBundle.putString("toiletId", review.toiletId.toString())
                    mBundle.putString("reviewId", review.reviewId.toString())
                    mBundle.putString("oriStars", review.stars.toString())
                    mBundle.putString("oriComment", review.comment.toString())
                    modifyReviewFragment.arguments = mBundle

//                    addReviewFragment.setReviewSubmissionListener(fragment)

                    fragmentManager.beginTransaction()
                        .replace(R.id.addReviewContainer, modifyReviewFragment)
                        .addToBackStack(null)
                        .commit()
                }


                addReviewContainer?.visibility =
                    if (addReviewContainer?.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }
        }

        // Check if the review is submitted by the current user
        val isCurrentUserReview = review.userId == currentUserId

        // Show/hide delete buttons based on whether it's the current user's review
        if (isCurrentUserReview) {
            holder.deleteButton.visibility = View.VISIBLE

            // Set click listener for delete button
            holder.deleteButton.setOnClickListener {
                // Show the delete confirmation dialog
                showDeleteConfirmationDialog(review)
            }

        } else {
            holder.deleteButton.visibility = View.GONE
        }
    }


    override fun getItemCount(): Int {
        return mReviews.size
    }

    private fun showDeleteConfirmationDialog(review: Review) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.apply {
            setTitle("Delete Review")
            setMessage("Are you sure you want to delete this review?")
            setPositiveButton("Delete") { _, _ ->
                onDeleteClickListener.invoke(review)
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}

