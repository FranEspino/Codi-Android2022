package com.example.codi_android.helpers
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Address
import androidx.core.content.ContextCompat
import com.example.codi_android.LocationUtils
import com.example.codi_android.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*


class HelpersMap {

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    fun requestEnabledGPS(context: Context){
        LocationUtils(context).requestLocationStatus(object :
            LocationUtils.OnLocationOnListener {
            override fun locationStatus(isLocationOn: Boolean) {}
        })
    }

     fun createMarker(map: GoogleMap, context: Context, latLng: LatLng, address: String) {
        val coordinate = latLng
        val marker = MarkerOptions().position(coordinate).title(address).snippet("CodiDrive obtuvo esta direccion \uD83D\uDE01 ")
            .icon(bitmapDescriptorFromVector(context, R.drawable.marker))
        map.addMarker(marker)
       /* map.animateCamera(
         CameraUpdateFactory.newLatLngZoom(coordinate,18f),4000,null
        )*/
    }







}