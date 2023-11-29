package hk.hku.cs.toiletinator1000

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ReviewsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */


class ReviewsFragment : Fragment(), AddReviewFragment.ReviewSubmissionListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    //for displaying ratings and descriptions
    private lateinit var viewModel: ReviewDataViewModel
    private lateinit var displayRatingTextView: TextView
    private lateinit var displayReviewDescTextView: TextView
    private var isAddReviewVisible = false
    private var reviews = ArrayList<Review>()
    private val db = Firebase.firestore
    private lateinit var auth: FirebaseAuth
    private lateinit var parentActivity: ToiletDetailsActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
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



        buttonAddReview.setOnClickListener {
            Log.d("Button Click","Add Review Button Click")

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
        val reviewsAdapter = ReviewsAdapter(this, reviews)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        recyclerView.adapter = reviewsAdapter

        db.collection("Review")
            .whereEqualTo("toiletId", arguments?.getString("toiletId").toString())
            .get()
            .addOnSuccessListener { documents ->
                val reviewList = documents.toObjects(Review::class.java)
                for (review in reviewList){
                    reviews.add(review)
                    reviewsAdapter.notifyDataSetChanged()
                }
            }





        return view
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

    override fun onSubmitReview() {
        val addReviewContainer = view?.findViewById<FrameLayout>(R.id.addReviewContainer)
        addReviewContainer?.visibility = View.GONE
    }

    private fun formatReviewData(ratings: List<Float>?, reviewDescriptions: List<String>?): String {
        val formattedReviews = StringBuilder()

        if (ratings != null && reviewDescriptions != null) {
            val size = minOf(ratings.size, reviewDescriptions.size)
            for (i in 0 until size) {
                formattedReviews.append("Rating: ${ratings[i]}\nReview: ${reviewDescriptions[i]}\n\n")
            }
        }

        return formattedReviews.toString()
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() = ReviewsFragment().apply {
            arguments = Bundle().apply {}
        }
    }
}

class ReviewsAdapter(val fragment: Fragment, private val mReviews: ArrayList<Review>) :
    RecyclerView.Adapter<ReviewsAdapter.ViewHolder>() {


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rating: TextView
        val comment: TextView
        val userId: TextView
        val auth: FirebaseAuth
        init {
            rating = view.findViewById(R.id.displayRating)
            comment = view.findViewById(R.id.displayReviewDesc)
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
                Log.d("Button Click","Modify Review Button Click")

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
    }


    override fun getItemCount(): Int {
        return mReviews.size
    }
}

