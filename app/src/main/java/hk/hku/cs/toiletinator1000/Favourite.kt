package hk.hku.cs.toiletinator1000

import com.google.firebase.firestore.DocumentId

data class Favourite(
    @DocumentId
    val favId: String = "",
    val userId: String = "",
    val toiletId: String = "",

)