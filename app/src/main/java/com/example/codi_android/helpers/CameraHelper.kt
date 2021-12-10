package com.example.codi_android.helpers

import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

class CameraHelper {
    val Chepen : CameraPosition = CameraPosition.Builder()
        .target(LatLng(-7.227031, -79.429717))
        .zoom(20f)
        .tilt(40f)
        .bearing(40f)
        .build()

    val melbourneBounds = LatLngBounds(
        LatLng(-19.12868371063585, -78.13735730196997),
        LatLng(-0.029375000658624254, -70.53432602124855),
    )
}