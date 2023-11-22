package hk.hku.cs.toiletinator1000

import android.content.Context
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


class AddReviewFragment : Fragment() {
    interface ReviewSubmissionListener {
        fun onSubmitReview()
    }

    private lateinit var viewModel: ReviewDataViewModel
    private var submissionListener: ReviewSubmissionListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_review, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(ReviewDataViewModel::class.java)

        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
        val reviewDescEditText = view.findViewById<EditText>(R.id.reviewDesc)
        val submitReviewButton = view.findViewById<Button>(R.id.submitReview)


        submitReviewButton.setOnClickListener {
            val rating = ratingBar.rating
            val reviewDescription = reviewDescEditText.text.toString()

            Log.d("Rating", "Rating: $rating")
            Log.d("Review", "Review: $reviewDescription")

            viewModel.addReview(rating, reviewDescription)

            // Clear input fields after submission
            ratingBar.rating = 0f
            reviewDescEditText.text = null

            hideKeyboard()

            submissionListener?.onSubmitReview()

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

