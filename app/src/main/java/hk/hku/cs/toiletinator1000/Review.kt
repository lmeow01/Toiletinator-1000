package hk.hku.cs.toiletinator1000

import com.google.firebase.firestore.DocumentId
import java.io.Serializable

public class Review() : Serializable {
    @DocumentId
    var reviewId: String? = ""
    var userId: String? = ""
    var toiletId: String? = ""
    var stars: Double? = 0.0
    var comment: String? = ""


    fun Review(userId: String, toiletId: String, stars: Double, comment: String) {
        this.userId = userId
        this.toiletId = toiletId
        this.stars = stars
        this.comment = comment
    }

}

