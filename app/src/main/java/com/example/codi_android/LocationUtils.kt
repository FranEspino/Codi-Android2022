package com.example.codi_android

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

class LocationUtils(private val context: Context) {
        companion object {
        const val PRIORITY_HIGH_ACCURACY_REQ = 0x600
            fun openDetailAppSettings(context: Context) {
                val packageName = context.packageName
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                context.startActivity(intent)
            }
        }
        private val settingsClient: SettingsClient = LocationServices.getSettingsClient(context)
        private val locationSettingsRequest: LocationSettingsRequest?
        private var locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        init {
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            locationSettingsRequest = builder.build()
            builder.setAlwaysShow(true)
        }

        fun requestLocationStatus(OnGpsListener: OnLocationOnListener?) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                OnGpsListener?.locationStatus(true)
            } else {
                settingsClient
                        .checkLocationSettings(locationSettingsRequest)
                        .addOnSuccessListener(context as Activity) {
                    OnGpsListener?.locationStatus(true)
                }
                .addOnFailureListener(context) { e ->
                        when ((e as ApiException).statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                    try {
                        val rae = e as ResolvableApiException
                        rae.startResolutionForResult(context, PRIORITY_HIGH_ACCURACY_REQ)
                    } catch (sie: IntentSender.SendIntentException) {
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    }
                }
                }
            }
        }
        interface OnLocationOnListener {
            fun locationStatus(isLocationOn: Boolean)
        }
    }