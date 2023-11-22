package hk.hku.cs.toiletinator1000

import com.google.firebase.firestore.DocumentId

data class Toilet (
    @DocumentId
    val toiletId: String = "",
    val floor: String = "",
    val building: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    var stars: Double = 0.0,
    var status: String = "Available",
    var images: ArrayList<String> = ArrayList()
)