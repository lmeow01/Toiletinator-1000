package hk.hku.cs.toiletinator1000

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue

data class Review(
    @DocumentId
    val reviewId: String = "",
    val userId: String = "",
    val toiletId: String = "",
    var stars: Double = 0.0,
    var comment: Double = 0.0,
    val timestamp: FieldValue = FieldValue.serverTimestamp()
)