package com.example.codi_android.objects

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.vmadalin.easypermissions.EasyPermissions


object Permissions {

    const val PERMISSION_FINE_LOCATION_CODE = 1
    const val PERMISSION_BACKGROUND_LOCATION_CODE = 2

    fun hasFineLocationPermission(context: Context) =
        EasyPermissions.hasPermissions(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

    fun requestFineLocationPermission(activity: Activity){
        EasyPermissions.requestPermissions(
            activity,
            "Necesitamos que otorgues permisos de tu ubicacion para una mejor experiencia",
            PERMISSION_FINE_LOCATION_CODE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    fun hasBackgroundLocationPermission(context: Context) : Boolean{
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.Q ){
            return EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                )
        }
        return true
    }


    fun requestBackgroundLocationPermission(activity: Activity){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.Q ){
            EasyPermissions.requestPermissions(
                activity,
                "Codi Necesita acceso a tu ubicacion en segundo plano!",
                PERMISSION_BACKGROUND_LOCATION_CODE,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }

    }



}