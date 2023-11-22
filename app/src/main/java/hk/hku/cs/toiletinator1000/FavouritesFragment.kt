package hk.hku.cs.toiletinator1000

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng

/**
 * A simple [Fragment] subclass.
 * Use the [FavouritesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FavouritesFragment : Fragment() {
    private var favourites = ArrayList<Toilet>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_favourites, container, false)

        if (favourites.size == 0) {
            view.findViewById<TextView>(R.id.no_favourites_text).visibility = View.VISIBLE
            return view
        }

        val recyclerView: RecyclerView = view.findViewById(R.id.favourites_recycler_view)
        val favouriteToiletsAdapter = FavouriteToiletsAdapter(favourites)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        recyclerView.adapter = favouriteToiletsAdapter

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment FavouritesFragment.
         */
        @JvmStatic
        fun newInstance() =
            FavouritesFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}

class FavouriteToiletsAdapter(private val favourites: ArrayList<Toilet>) :
    RecyclerView.Adapter<FavouriteToiletsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val location: TextView
        val stars: TextView

        init {
            location = view.findViewById(R.id.fav_toilet_location)
            stars = view.findViewById(R.id.fav_toilet_stars)
            view.setOnClickListener {
                val intent = Intent(view.context, ToiletDetailsActivity::class.java)
                intent.putExtra("toiletLocation", location.text)
                intent.putExtra("toiletStars", stars.text)
                intent.putExtra("toiletId", "1")
                view.context.startActivity(intent)
            }
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
        holder.stars.text = toilet.stars.toString()
    }

    override fun getItemCount(): Int {
        return favourites.size
    }
}