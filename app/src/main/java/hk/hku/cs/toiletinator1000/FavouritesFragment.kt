package hk.hku.cs.toiletinator1000

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage

/**
 * A simple [Fragment] subclass.
 * Use the [FavouritesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FavouritesFragment : Fragment() {
    private var favourites = ArrayList<Toilet>()
    private var favouritesCurr = ArrayList<Toilet>()
    private lateinit var favouriteToiletsAdapter: FavouriteToiletsAdapter
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favourites, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.favourites_recycler_view)
        favouriteToiletsAdapter = FavouriteToiletsAdapter(favouritesCurr)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        recyclerView.adapter = favouriteToiletsAdapter

        // Fetch the current user's UID
        val currentUserUID = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Fetch the user's document from Firestore based on UID
        val userDocumentRef = db.collection("User").document(currentUserUID)
        userDocumentRef.get().addOnSuccessListener { documentSnapshot ->
            val user = documentSnapshot.toObject(User::class.java)
            if (user != null) {
                val favList = documentSnapshot.toObject(User::class.java)?.Fav ?: emptyList()

                if (favList.isEmpty()) {
                    view.findViewById<TextView>(R.id.no_favourites_text).visibility = View.VISIBLE
                }

                // Fetch details of each toilet from the Fav list
                for (toiletId in favList) {
                    Log.d("ToiletId in favList", "Wishlist: $toiletId")
                    val toiletDocumentRef = db.collection("Toilet").document(toiletId)
                    toiletDocumentRef.get().addOnSuccessListener { toiletSnapshot ->
                        if (toiletSnapshot.exists()) {
                            val toilet = toiletSnapshot.toObject(Toilet::class.java)
                            Log.d("Check toilet", "Toilet database: $toilet")
                            toilet?.let {
                                favourites.add(it)
                                favouritesCurr.add(it)
                                favouriteToiletsAdapter.notifyDataSetChanged()
                                Log.d("FavouritesFragment", "Toilet added: $it")
                            }
                        } else {
                            Log.e("FavouritesFragment", "Toilet document does not exist")
                        }
                    }
                }
            } else {
                view.findViewById<TextView>(R.id.no_favourites_text).visibility = View.VISIBLE
            }
        }.addOnFailureListener { e ->
            Log.e("FavouritesFragment", "Error fetching user document: $e")
        }
        return view
    }

    public fun filterToilets(minStars: Int, maxStars: Int, status: String, building: String) {
        val filteredToilets = ArrayList<Toilet>()
        for (toilet in favourites) {
            val matchStars = toilet.stars >= minStars && toilet.stars <= maxStars
            val matchStatus = status == "All" || toilet.status == status
            val matchBuilding = building == "All" || toilet.building == building
            if (matchStars && matchStatus && matchBuilding) {
                filteredToilets.add(toilet)
            }
        }
        Log.d("FavouritesFragment", "Filtered toilets: $filteredToilets")
        favouritesCurr.clear()
        favouritesCurr.addAll(filteredToilets)
        favouriteToiletsAdapter.notifyDataSetChanged()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment FavouritesFragment.
         */
        @JvmStatic
        fun newInstance() = FavouritesFragment().apply {
            arguments = Bundle().apply {}
        }
    }
}

class FavouriteToiletsAdapter(private val favourites: ArrayList<Toilet>) :
    RecyclerView.Adapter<FavouriteToiletsAdapter.ViewHolder>() {

    private val storage = Firebase.storage

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val location: TextView
        val stars: TextView
        val status: TextView
        val image: ImageView

        init {
            location = view.findViewById(R.id.fav_toilet_location)
            stars = view.findViewById(R.id.fav_toilet_stars)
            status = view.findViewById(R.id.fav_toilet_status)
            image = view.findViewById(R.id.fav_toilet_image)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.favourite_toilet_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val toilet = favourites[position]
        holder.location.text = toilet.floor + " " + toilet.building
        holder.stars.text = toilet.stars.toString() + " / 5"
        holder.status.text = toilet.status

        // Download image
        if (toilet.images.size > 0 && toilet.images[0] != "") {
            val imageRef = storage.reference.child(toilet.images[0])
            val ONE_MEGABYTE: Long = 1024 * 1024
            imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener { bytes ->
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                holder.image.setImageBitmap(bitmap)
            }.addOnFailureListener {
                Log.e("ToiletDetailsActivity", "Failed to get image")
            }
        }

        //display the page when user clicks in the toilet from wishlist
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ToiletDetailsActivity::class.java)
            intent.putExtra(
                "toiletId", toilet.toiletId
            ) // Pass the toiletId to ToiletDetailsActivity
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return favourites.size
    }
}