package hk.hku.cs.toiletinator1000

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    //for displaying ratings and descriptions
    private lateinit var viewModel: ReviewDataViewModel
    private lateinit var displayRatingTextView: TextView
    private lateinit var displayReviewDescTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_reviews, container, false)
        val buttonAddReview = view.findViewById<Button>(R.id.button_add_review)
        val addReviewContainer = view.findViewById<FrameLayout>(R.id.addReviewContainer)

        viewModel = ViewModelProvider(requireActivity()).get(ReviewDataViewModel::class.java)

        viewModel.reviewDescriptionsLiveData.observe(viewLifecycleOwner, { reviewDescriptions ->
            val displayReviewDescTextView = view.findViewById<TextView>(R.id.displayReviewDesc)
            val formattedData = formatReviewData(viewModel.ratingsLiveData.value, reviewDescriptions)
            Log.d("Data2", "Data2:$formattedData")
            displayReviewDescTextView.text = formattedData ?: "No reviews available"
        })

        buttonAddReview.setOnClickListener {
            Log.d("Button Click","Add Review Button Click")

            val fragmentManager = childFragmentManager

            if (addReviewContainer.childCount == 0) {
                val addReviewFragment = AddReviewFragment()
                addReviewFragment.setReviewSubmissionListener(this)

                fragmentManager.beginTransaction()
                    .replace(R.id.addReviewContainer, addReviewFragment)
                    .addToBackStack(null)
                    .commit()
            }

            addReviewContainer.visibility =
                if (addReviewContainer.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        return view
    }
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
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Reviews.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ReviewsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}