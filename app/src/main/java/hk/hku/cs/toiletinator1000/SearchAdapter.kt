package hk.hku.cs.toiletinator1000

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SearchAdapter(private val onItemClick: (Toilet) -> Unit) :
    RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    private var searchResults: List<Toilet> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_toilet, parent, false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val toilet = searchResults[position]
        holder.bind(toilet)
    }

    override fun getItemCount(): Int {
        return searchResults.size
    }

    fun updateResults(results: List<Toilet>) {
        searchResults = results
        notifyDataSetChanged()
    }

    inner class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Bind views and handle item click events
        fun bind(toilet: Toilet) {
            val toiletNameTextView: TextView = itemView.findViewById(R.id.toiletNameTextView)
            toiletNameTextView.text = "${toilet.building} ${toilet.floor}"

            // Set an onClickListener on the whole item view
            itemView.setOnClickListener {
                onItemClick.invoke(toilet)
            }
        }
    }
}
