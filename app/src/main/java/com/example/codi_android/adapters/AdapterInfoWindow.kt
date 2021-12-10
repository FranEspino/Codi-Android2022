package com.example.codi_android.adapters

import android.app.Activity
import android.content.Context
import android.service.autofill.TextValueSanitizer
import android.view.View
import android.widget.TextView
import com.example.codi_android.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class AdapterInfoWindow(context: Context): GoogleMap.InfoWindowAdapter {
    private  val contexView =
        (context as Activity).layoutInflater.inflate(R.layout.custom_info_window,null)
    override fun getInfoWindow(marker: Marker?): View {
        renderViews(marker,contexView)
        return contexView
    }

    override fun getInfoContents(marker: Marker?): View {
        renderViews(marker,contexView)
        return contexView

    }

    private fun renderViews(marker: Marker?,contentView:View){
        val title = marker?.title
        val description = marker?.snippet
        val titleTextView = contentView.findViewById<TextView>(R.id.tv_title_windowsinfo)
        titleTextView.text = title
        val desciprtionView = contentView.findViewById<TextView>(R.id.tv_description_windowsinfo)
        desciprtionView.text = description
    }
}