package com.example.codi_android.helpers

import android.graphics.Color
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.SphericalUtil

class PolylineHelper {

    fun drawCurveOnMap(googleMap: GoogleMap, latLng1: LatLng, latLng2: LatLng, colorRoute:String) {
        val k = 0.5
        var h = SphericalUtil.computeHeading(latLng1, latLng2)
        var d = 0.0
        val p: LatLng?
        if (h < 0) {
            d = SphericalUtil.computeDistanceBetween(latLng2, latLng1)
            h = SphericalUtil.computeHeading(latLng2, latLng1)
            p = SphericalUtil.computeOffset(latLng2, d * 0.5, h)
        } else {
            d = SphericalUtil.computeDistanceBetween(latLng1, latLng2)
            p = SphericalUtil.computeOffset(latLng1, d * 0.5, h)
        }
        val x = (1 - k * k) * d * 0.5 / (2 * k)
        val r = (1 + k * k) * d * 0.5 / (2 * k)
        val c = SphericalUtil.computeOffset(p, x, h + 90.0)
        val h1 = SphericalUtil.computeHeading(c, latLng1)
        val h2 = SphericalUtil.computeHeading(c, latLng2)
        val numberOfPoints = 1000
        val step = (h2 - h1) / numberOfPoints
        val polygon = PolygonOptions()
        val temp = arrayListOf<LatLng>()
        for (i in 0 until numberOfPoints) {
            val latlng = SphericalUtil.computeOffset(c, r, h1 + i * step)
            polygon.add(latlng)
            temp.add(latlng)
        }

        for (i in (temp.size - 1) downTo 1) {
            polygon.add(temp[i])
        }

        if(colorRoute== "cyan"){
            polygon.strokeColor(Color.rgb(73,231,192))
        }

        if(colorRoute=="yellow"){
            polygon.strokeColor(Color.rgb(255,196,12))
        }

        polygon.strokeWidth(8f)
        polygon.strokePattern(listOf(Dash(25f), Gap(35f)))
        googleMap.addPolygon(polygon)
        temp.clear()
    }

}