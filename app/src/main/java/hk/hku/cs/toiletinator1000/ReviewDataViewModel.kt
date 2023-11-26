package hk.hku.cs.toiletinator1000

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ReviewDataViewModel : ViewModel() {
    private val _reviewDescriptions = mutableListOf<String>()
    private val _ratings = mutableListOf<Float>()

    private val _ratingsLiveData = MutableLiveData<List<Float>>()
    val ratingsLiveData: LiveData<List<Float>> = _ratingsLiveData

    private val _reviewDescriptionsLiveData = MutableLiveData<List<String>>()
    val reviewDescriptionsLiveData: LiveData<List<String>> = _reviewDescriptionsLiveData

    private val reviewList = MutableLiveData<List<Review>>()
    fun addReview(review: Review) {

    }
}
