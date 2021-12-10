package com.example.codi_android.helpers
import android.content.Context
import android.location.Geocoder
import android.view.View
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import java.util.*

class PlacesHelper {
    fun instancieAutocomplete(context: Context, autocompleteSupportFragment: AutocompleteSupportFragment?){
        val apiKey = "AIzaSyAJ8gmST9pT3gTj1N0MzTkhIubCMOLAOEY"
        if (!Places.isInitialized()) {
            Places.initialize(context, apiKey)
            Places.createClient(context)
        }
        val bounds = RectangularBounds.newInstance(
            LatLng(-6.811692, -79.892815),
            LatLng(-6.747710, -79.803870),
        )
        autocompleteSupportFragment!!.setPlaceFields(
            listOf(
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.PHONE_NUMBER,
                Place.Field.LAT_LNG,
                Place.Field.OPENING_HOURS,
                Place.Field.RATING,
                Place.Field.USER_RATINGS_TOTAL,
            )
        )
        autocompleteSupportFragment.setCountries("PE").setLocationBias(bounds)
    }



}