package hk.hku.cs.toiletinator1000

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class AddReviewFragment : Fragment() {
    interface ReviewSubmissionListener {
        fun onSubmitReview()
    }

    private lateinit var viewModel: ReviewDataViewModel
    private var submissionListener: ReviewSubmissionListener? = null
    private lateinit var toiletDetailActivity: ToiletDetailsActivity
    private lateinit var ratingBar: RatingBar
    private lateinit var reviewDescEditText: EditText

    private lateinit var auth: FirebaseAuth
    val db = Firebase.firestore


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_review, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(ReviewDataViewModel::class.java)

        ratingBar = view.findViewById(R.id.ratingBar)
        reviewDescEditText = view.findViewById(R.id.reviewDesc)
        val submitReviewButton = view.findViewById<Button>(R.id.submitReview)

        toiletDetailActivity = activity as ToiletDetailsActivity

        val currentUserUID = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        submitReviewButton.setOnClickListener {
            val rating = ratingBar.rating
            val reviewDescription = reviewDescEditText.text.toString()

            Log.d("Rating", "Rating: $rating")
            Log.d("Review", "Review: $reviewDescription")


//            viewModel.addReview(rating, reviewDescription)
            val toiletId = arguments?.getString("toiletId").toString()

            val data = hashMapOf(
                "userId" to currentUserUID,
                "toiletId" to toiletId,
                "stars" to rating,
                "comment" to reviewDescription
            )

            db.collection("Review")
                .add(data)
                .addOnSuccessListener { documents ->
                    Log.d("AddReviewFragment", data.toString())
                    // Clear input fields after submission
                    ratingBar.rating = 0f
                    reviewDescEditText.text = null

                    hideKeyboard()

                    submissionListener?.onSubmitReview()

                    val total = 0
                    val count = 0

                    db.collection("Toilet").document(toiletId)
                        .get()
                        .addOnSuccessListener { document ->
                            val toilet = document.toObject(Toilet::class.java)
                            var stars: Double = toilet!!.stars
                            db.collection("Review")
                                .whereEqualTo("toiletId", toiletId)
                                .get()
                                .addOnSuccessListener { documents->
                                    val reviewList = documents.toObjects(Review::class.java)
                                    stars = stars * (reviewList.size - 1)
                                    stars += rating
                                    stars /= reviewList.size
                                    val formattedStars = String.format("%.2f", stars)


                                    db.collection("Toilet").document(toiletId)
                                        .update("stars", formattedStars.toDouble())
                                        .addOnSuccessListener {
                                            val intent = Intent(toiletDetailActivity, ToiletDetailsActivity::class.java)
                                            intent.putExtra("toiletId", toiletId)
                                            startActivity(intent)
                                        }
                                }
                        }
                }
                .addOnFailureListener { e ->
                    Log.d("AddReviewFragment", "Error adding new review")
                    // Clear input fields after submission
                    ratingBar.rating = 0f
                    reviewDescEditText.text = null

                    hideKeyboard()

                    submissionListener?.onSubmitReview()
                }

        }
        reviewDescEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        return view
    }



    private fun hideKeyboard(){
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    fun setReviewSubmissionListener(listener: ReviewSubmissionListener) {
        submissionListener = listener
    }
}

