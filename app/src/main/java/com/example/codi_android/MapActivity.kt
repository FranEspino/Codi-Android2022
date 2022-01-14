package com.example.codi_android

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import android.content.Intent
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.MutableLiveData

import com.example.codi_android.adapters.AdapterInfoWindow
import com.example.codi_android.helpers.PlacesHelper
import com.example.codi_android.helpers.HelpersMap
import com.example.codi_android.helpers.PolylineHelper
import com.example.codi_android.objects.MapUtil
import com.example.codi_android.objects.MapUtil.setCameraPosition
import com.example.codi_android.service.TrackerService
import com.example.parquecientificouncp.components.MyToolbar
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.ServiceScoped
import java.util.*


class MapActivity : AppCompatActivity()  , OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, GoogleMap.OnMarkerDragListener,GoogleMap.OnPolylineClickListener {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val placesHelper by lazy { PlacesHelper()}

    private lateinit var map:GoogleMap
    private lateinit var destinationMarker: Marker

    private var startTime = 0L
    private var stopTime = 0L

    private var locationList = mutableListOf<LatLng>()
     val started = MutableLiveData(false)
    private val helpersMap by lazy { HelpersMap() }
    private val helperPolyline by lazy {PolylineHelper()}

    private lateinit var container_toolbar: LinearLayout

    private lateinit var currentLocation:LatLng
    private lateinit var currentDestination:LatLng
    private lateinit var colorRoute:String

    private lateinit var lastLocation: Location
    private lateinit var geocoder: Geocoder
    private lateinit var destination_address : String
    private lateinit var direccion: List<Address>
    private lateinit var fat_waze: FloatingActionButton
    private lateinit var fat_googlemap: FloatingActionButton
    private lateinit var fab_travel: FloatingActionButton
    private lateinit var fab_pause: FloatingActionButton


    companion object{
        const val REQUEST_CODE_LOCATION = 0
        const  val  ACTIVITY_SERVICE_START = "ACTION_SERVICE_START"
        const  val  ACTIVITY_SERVICE_STOP = "ACTION_SERVICE_STOP"

    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        MyToolbar().showToolbar(this, "¿A donde quieres ir?", false)
        window.statusBarColor = ContextCompat.getColor(this, R.color.statusbar)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        container_toolbar = findViewById(R.id.container_toolbar)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapfragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fat_waze = findViewById(R.id.fab_waze)
        fat_googlemap = findViewById(R.id.fab_googlemaps)
        fab_travel = findViewById(R.id.fab_travel)
        fab_pause = findViewById(R.id.fab_pause)
        fab_pause.visibility =  View.INVISIBLE

        fab_pause.setOnClickListener{
            sendActionCommandService(ACTIVITY_SERVICE_STOP)


            fab_pause.visibility =  View.INVISIBLE
            fab_travel.visibility =  View.VISIBLE

        }

        fab_travel.setOnClickListener{
            //Iniciamos las inyecciones
            sendActionCommandService(ACTIVITY_SERVICE_START)
            fab_pause.visibility =  View.VISIBLE
            fab_travel.visibility =  View.INVISIBLE

        }

        fat_googlemap.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=${currentDestination.latitude},${currentDestination.longitude}"))
            startActivity(intent)
        }
        fat_waze.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://waze.com/ul?q=66%20Acacia%20Avenue&ll=${currentDestination.latitude},${currentDestination.longitude}&navigate=yes"))
            startActivity(intent)
        }

    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.maplight))
        colorRoute = "yellow"
        enableLocation()
        map.setOnMyLocationButtonClickListener(this)
        map.setOnMyLocationClickListener(this)
        map.setMaxZoomPreference(21f)
        map.uiSettings.apply {
            isZoomControlsEnabled = false
            isMapToolbarEnabled = false
            isCompassEnabled = true
        }

        observedTrackerService()

        map.setInfoWindowAdapter(AdapterInfoWindow(this))
        map.setOnMarkerDragListener(this)

    }



    private fun observedTrackerService(){
        TrackerService.locationList.observe(this,{
            if(it!=null){
                locationList = it
                drawPolyLine()
                followPoline()
            }
        })
        TrackerService.started.observe(this,{
            started.value  = it
        })

        TrackerService.startTime.observe(this,{
            startTime = it
        })
        TrackerService.stopTime.observe(this,{
            stopTime = it
            if(stopTime!=0L){
                //Animar all el trayecto que recoriio
                val bounds = LatLngBounds.Builder()
                for(location in locationList){
                    bounds.include(location)
                }
                map.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(
                        bounds.build(),100
                    ),2000, null
                )
            }
        })

    }
    private fun drawPolyLine(){
        val polyline = map.addPolyline(
            PolylineOptions().apply {
                width(8f)
                color(Color.BLUE)
                jointType(JointType.ROUND)
                startCap(ButtCap())
                endCap(ButtCap())
                addAll(locationList)

            }

        )
    }

    private fun followPoline(){
        if(locationList.isNotEmpty()){
            map.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    setCameraPosition(locationList.last()
                    )
                ), 1000,null)
        }
    }
    private fun sendActionCommandService(action:String){

        Intent(
            getApplicationContext(),
            TrackerService::class.java
        ).apply {
            this.action = action
            getApplicationContext().startService(this)
        }
    }



    //Capturar la respuesta despues de solicitar activacion del GPS
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LocationUtils.PRIORITY_HIGH_ACCURACY_REQ) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                }
                Activity.RESULT_CANCELED -> {
                    helpersMap.requestEnabledGPS(this)
                }
            }
        }
    }

    //Si desactiva los permisos del gps en modo resume
    override fun onResumeFragments() {
        super.onResumeFragments()
        if(!::map.isInitialized) return
        if(!isLocationPermissionGranted()){
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                openSettingsPermission()
                return
            }

            map.isMyLocationEnabled = false
            openSettingsPermission()
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        var status =  LocationUtils.PRIORITY_HIGH_ACCURACY_REQ
        return false
    }

    override fun onMyLocationClick(p0: Location) {

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.map_types_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //Cambiar el estilo del mapa segun la opcion seleccionada
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.search){
            container_toolbar.visibility= View.GONE
            val autocomplete = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment1) as AutocompleteSupportFragment?
            placesHelper.instancieAutocomplete(this, autocomplete)
            //Ahorrar el click del buscador y abrir directamente el teclado
            val root: View? = autocomplete?.view
            if (root != null) {
                root.findViewById<View>(R.id.places_autocomplete_search_input).performClick()
            }
            autocomplete?.setOnPlaceSelectedListener(object : PlaceSelectionListener {
                override fun onPlaceSelected(place: Place) {
                    container_toolbar.visibility= View.VISIBLE
                    map.clear()
                    map.animateCamera(CameraUpdateFactory.zoomBy(-3.5f),2000,null)
                    helperPolyline.drawCurveOnMap(map,currentLocation,place.latLng!!, colorRoute)
                    geocoder =  Geocoder(this@MapActivity, Locale.getDefault());
                    direccion = geocoder.getFromLocation(currentLocation.latitude, currentLocation.longitude,1)
                    var direccion_string = direccion.get(0).getAddressLine(0)
                    helpersMap.createMarker(map,this@MapActivity,currentLocation,direccion_string)
                    val name = place.name
                    val address = place.address
                    val latlng = place.latLng
                    if (latlng != null) {
                        currentDestination = latlng
                    }
                    geocoder =  Geocoder(this@MapActivity, Locale.getDefault());
                    if (latlng != null) {
                        direccion = geocoder.getFromLocation(latlng.latitude, latlng.longitude,1)
                    }
                    destination_address = direccion.get(0).getAddressLine(0).toString()
                    destinationMarker = map.addMarker(place.latLng?.let { MarkerOptions().position(it).title(destination_address).snippet(name+"\uD83D\uDCCD ").draggable(true) })
                }
                override fun onError(status: Status) {
                }
            })
        }

        if(item.itemId == R.id.map_dark){
            colorRoute = "cyan"

            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.mapdark))
        }
        if(item.itemId == R.id.map_light){
            colorRoute = "yellow"

            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.maplight))
        }
        if(item.itemId == R.id.map_stand){
            colorRoute = "cyan"

            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.mapstand))
        }
        if(item.itemId == R.id.map_retro){
            colorRoute = "yellow"

            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.mapretro))
        }
        return super.onOptionsItemSelected(item)
    }

    //Solicitat los permisos de ubicacion
    private fun isLocationPermissionGranted() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun enableLocation(){
        if(!::map.isInitialized) return //Si el mapa no ha sido nicializado no pido permiso
        if(isLocationPermissionGranted()){
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            map.isMyLocationEnabled = true
            helpersMap.requestEnabledGPS(this)
            fusedLocationClient.lastLocation.addOnSuccessListener(this){ location ->
                if(location!=null){
                    lastLocation = location
                    currentLocation = LatLng(location.latitude, location.longitude)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18f))
                    geocoder =  Geocoder(this, Locale.getDefault());
                    direccion = geocoder.getFromLocation(location.latitude, location.longitude,1)
                    var direccion_string = direccion.get(0).getAddressLine(0)
                    helpersMap.createMarker(map,this,currentLocation,direccion_string)
                }
            }

        }else{
            openSettingsPermission()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            REQUEST_CODE_LOCATION -> if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    openSettingsPermission()
                    return
                }
                map.isMyLocationEnabled = true
                helpersMap.requestEnabledGPS(this)
                fusedLocationClient.lastLocation.addOnSuccessListener(this){ location ->
                    if(location!=null){
                        lastLocation = location
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18f))
                        geocoder =  Geocoder(this, Locale.getDefault());
                        direccion = geocoder.getFromLocation(location.latitude, location.longitude,1)
                        var direccion_string = direccion.get(0).getAddressLine(0)
                        helpersMap.createMarker(map,this,currentLatLng,direccion_string)
                    }
                }
            }else{
                openSettingsPermission()
            }
            else -> {
                openSettingsPermission()
            }
        }
    }

    //Abrir la configuracion del disposivito
    private fun openSettingsPermission(){
        Toast.makeText(this, "Porfavor ve a tus accesos y activa el permiso de localizacion.  ", Toast.LENGTH_LONG).show()
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)){
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
        }else{
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION
            )
        }
    }

    override fun onMarkerDragStart(p0: Marker?) {

    }

    override fun onMarkerDrag(marker: Marker?) {

    }

    override fun onMarkerDragEnd(marker: Marker?) {
        Toast.makeText(this, "Nuevo destino establecido", Toast.LENGTH_SHORT).show()

        if (marker != null) {
            map.clear()

            val destinationMarker = LatLng(marker.position.latitude, marker.position.longitude)
            geocoder =  Geocoder(this@MapActivity, Locale.getDefault());
            direccion = geocoder.getFromLocation(currentLocation.latitude, currentLocation.longitude,1)
            var direccion_string = direccion.get(0).getAddressLine(0)
            helpersMap.createMarker(map,this@MapActivity,currentLocation,direccion_string)
            helperPolyline.drawCurveOnMap(map,currentLocation, destinationMarker, colorRoute)
            geocoder =  Geocoder(this, Locale.getDefault());
            val latlong = LatLng(marker.position.latitude,marker.position.longitude)
            geocoder =  Geocoder(this, Locale.getDefault());
            direccion = geocoder.getFromLocation(latlong.latitude, latlong.longitude,1)
            destination_address = direccion.get(0).getAddressLine(0).toString()

            marker.remove()

             map.addMarker(MarkerOptions().position(latlong).title(destination_address).snippet("Esta es tu ubicacion de destino actualmente  \uD83D\uDCCD ").draggable(true))
        }

    }

    override fun onPolylineClick(p0: Polyline?) {
        TODO("Not yet implemented")
    }


}
