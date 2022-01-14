package com.example.codi_android.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.ContentProvider
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.example.codi_android.objects.MapUtil
import com.example.codi_android.objects.MapUtil.calculateTheDistance
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TrackerService:LifecycleService() {

    @Inject
    lateinit var notification: NotificationCompat.Builder
    @Inject
    lateinit var notificationManager: NotificationManager

    private lateinit var fusedfLocationProviderClient: FusedLocationProviderClient
    companion object {


        val startTime = MutableLiveData<Long>()
        val stopTime = MutableLiveData<Long>()

        const val ACTION_SERVICE_START = "ACTION_SERVICE_START"
        const val ACTION_SERVICE_STOP = "ACTION_SERVICE_STOP"
        val started = MutableLiveData<Boolean>()

        const val LOCATION_UPDATE_INTERVAL = 4000L
        const val LOCATION_FASTEST_UPDATE_INTERVAL = 2000L

        const val NOTIFICATION_CHANNEL_ID = "tracker_notification_id"
        const val NOTIFICATION_CHANNEL_NAME = "tracker_notification"
        const val NOTIFICATION_ID =  3
        val locationList = MutableLiveData<MutableList<LatLng>>()

    }

    private val locationCallback = object: LocationCallback(){
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            result?.locations?.let{ locations ->
                for(location in locations){

                    updateLocationList(location)
                    updateNotificationPeriodically()
                }

            }
        }
    }


    private fun setInitialValues(){
        started.postValue(false)
        locationList.postValue((mutableListOf()))
        startTime.postValue(0L)
        stopTime.postValue(0L)

    }


    private fun updateLocationList(location: Location){
        Log.d("SEGUNDO PLANO: ", locationList.value.toString())

        val newLatLng = LatLng(location.latitude,location.longitude)
        locationList.value?.apply {
            add(newLatLng)
            locationList.postValue(this)
        }
    }




    override fun onCreate() {
        super.onCreate()
        setInitialValues()
        fusedfLocationProviderClient =  LocationServices.getFusedLocationProviderClient(this)


    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                ACTION_SERVICE_START ->{
                    started.postValue(true)
                    startForegroundService()
                    startLocationUpdates()
                }
                ACTION_SERVICE_STOP  ->{
                    started.postValue(false)
                    stopForegroundService()
                }
                else  -> {}
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService(){
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, notification.build())
    }




    @SuppressLint("MissingPermission")
    private fun startLocationUpdates(){
        val locationRequest = LocationRequest().apply {
            interval = LOCATION_UPDATE_INTERVAL
            fastestInterval = LOCATION_FASTEST_UPDATE_INTERVAL
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

       fusedfLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        startTime.postValue(System.currentTimeMillis())
    }
    private fun stopForegroundService(){
        fusedfLocationProviderClient.removeLocationUpdates(locationCallback)
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(
            NOTIFICATION_ID,

            )
        stopForeground(true)
        stopSelf()
        stopTime.postValue(System.currentTimeMillis())

    }


    private fun createNotificationChannel(){
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun  updateNotificationPeriodically(){
        notification.apply {
            setContentTitle( "Distanica recorrida: ")
            setContentText(locationList.value?.let {calculateTheDistance(it) }+"Km ")
        }
        notificationManager.notify(NOTIFICATION_ID,notification.build())
    }


}