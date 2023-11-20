package hk.hku.cs.toiletinator1000

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.model.Marker

class CustomInfoWindow(context: Context) : InfoWindowAdapter {
    private val layout: View = LayoutInflater.from(context).inflate(R.layout.info_window, null)

    private fun setInfoWindow(marker: Marker) {
        val tvTitle: TextView = layout.findViewById(R.id.info_window_title)
        val tvSnippet: TextView = layout.findViewById(R.id.info_window_snippet)
        tvTitle.text = marker.title
        tvSnippet.text = marker.snippet
    }

    override fun getInfoWindow(p0: Marker): View {
        setInfoWindow(p0)
        return layout
    }

    override fun getInfoContents(p0: Marker): View {
        setInfoWindow(p0)
        return layout
    }
}