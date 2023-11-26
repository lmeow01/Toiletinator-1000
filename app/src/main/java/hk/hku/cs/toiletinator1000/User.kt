package hk.hku.cs.toiletinator1000

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    //always assume that userId is already created upon user sign up for the app
    val userId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    //used to store the array of favourite toilet Id
    val Fav: List<String> = listOf(),
    )

//Firebase dataset example
//-User
//    -User1_id(insert upon user sign up)
//        - fav
//            -0:toiletId (from Toilet collection)
//    -User2_id
//        - fav
//            -0:toiletId
//            -1:toiletId