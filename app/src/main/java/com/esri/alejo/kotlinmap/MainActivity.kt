package com.esri.alejo.kotlinmap

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import kotlinx.android.synthetic.main.activity_main.*
import com.esri.arcgisruntime.mapping.view.LocationDisplay
import android.widget.Toast
import android.view.View
import android.widget.Button
import android.widget.ImageButton

class MainActivity : AppCompatActivity(){
    var pos = 0
    var locationDisplay: LocationDisplay? = null
    private var mbut: ImageButton? = null
    //var view: View? = null
    private val requestCode = 2

    internal var reqPermissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission
            .ACCESS_COARSE_LOCATION)

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_cerca -> {
                println("cerca")
                this.toast("cerca")
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_buscar -> {
                println("buscar")
                this.toast("buscar")
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //myLocationButton.setOnClickListener(View.OnClickListener {
        //    geoLocalizacion()
        //})
        mbut = findViewById(R.id.myLocationButton) as ImageButton
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        // create a map with the BasemapType topographic
        val map = ArcGISMap(Basemap.Type.TOPOGRAPHIC, 4.6097100, -74.0817500, 16)
        // set the map to be displayed in the layout's MapView
        mapView2!!.map = map
        // get the MapView's LocationDisplay

        locationDisplay = mapView2!!.locationDisplay
        // Listen to changes in the status of the location data source.
        locationDisplay!!.addDataSourceStatusChangedListener(LocationDisplay.DataSourceStatusChangedListener { dataSourceStatusChangedEvent ->
            // If LocationDisplay started OK, then continue.
            if (dataSourceStatusChangedEvent.isStarted)
                return@DataSourceStatusChangedListener

            // No error is reported, then continue.
            if (dataSourceStatusChangedEvent.error == null)
                return@DataSourceStatusChangedListener

            // If an error is found, handle the failure to start.
            // Check permissions to see if failure may be due to lack of permissions.
            val permissionCheck1 = ContextCompat.checkSelfPermission(this@MainActivity, reqPermissions[0]) ==
                    PackageManager.PERMISSION_GRANTED
            val permissionCheck2 = ContextCompat.checkSelfPermission(this@MainActivity, reqPermissions[1]) ==
                    PackageManager.PERMISSION_GRANTED

            if (!(permissionCheck1 && permissionCheck2)) {
                // If permissions are not already granted, request permission from the user.
                ActivityCompat.requestPermissions(this@MainActivity, reqPermissions, requestCode)
            } else {
                // Report other unknown failure types to the user - for example, location services may not
                // be enabled on the device.
                val message = String.format("Error in DataSourceStatusChangedListener: %s", dataSourceStatusChangedEvent
                        .source.locationDataSource.error.message)
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                // Update UI to reflect that the location display did not actually start
            }
        })

            // Populate the list for the Location display options for the spinner's Adapter
            myLocationButton.setOnClickListener {
                if (pos  >= 5){
                    pos = 0
                }
                pos ++

                when (pos) {
                    0 ->
                        // Stop Location Display
                        if (locationDisplay!!.isStarted)
                            locationDisplay!!.stop()
                    1 ->
                        // Start Location Display
                        if (!locationDisplay!!.isStarted)
                            locationDisplay!!.startAsync()
                    2 -> {
                        // Re-Center MapView on Location
                        // AutoPanMode - Default: In this mode, the MapView attempts to keep the location symbol on-screen by
                        // re-centering the location symbol when the symbol moves outside a "wander extent". The location symbol
                        // may move freely within the wander extent, but as soon as the symbol exits the wander extent, the MapView
                        // re-centers the map on the symbol.
                        locationDisplay!!.autoPanMode = LocationDisplay.AutoPanMode.RECENTER
                        if (!locationDisplay!!.isStarted)
                            locationDisplay!!.startAsync()
                        }
                    3 -> {
                        // Start Navigation Mode
                        // This mode is best suited for in-vehicle navigation.
                        locationDisplay!!.autoPanMode = LocationDisplay.AutoPanMode.NAVIGATION
                        if (!locationDisplay!!.isStarted)
                            locationDisplay!!.startAsync()
                        }
                    4 -> {
                        // Start Compass Mode
                        // This mode is better suited for waypoint navigation when the user is walking.
                        locationDisplay!!.autoPanMode = LocationDisplay.AutoPanMode.COMPASS_NAVIGATION
                        if (!locationDisplay!!.isStarted)
                            locationDisplay!!.startAsync()
                        }
                }
                Toast.makeText(this@MainActivity, "You clicked me. on "+pos, Toast.LENGTH_SHORT).show()
            }
        }
        override fun onPause() {
            super.onPause()
            mapView2.pause()
        }

        override fun onResume() {
            super.onResume()
            mapView2.resume()
        }

        fun Context.toast(message: CharSequence) =
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

        override fun onDestroy() {
            super.onDestroy()
            mapView2!!.dispose()
        }

        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location permission was granted. This would have been triggered in response to failing to start the
                // LocationDisplay, so try starting this again.
                locationDisplay!!.startAsync()
            } else {
                // If permission was denied, show toast to inform user what was chosen. If LocationDisplay is started again,
                // request permission UX will be shown again, option should be shown to allow never showing the UX again.
                // Alternative would be to disable functionality so request is not shown again.
                Toast.makeText(this@MainActivity, resources.getString(R.string.abc_action_bar_home_description), Toast
                        .LENGTH_SHORT).show()
                // Update UI to reflect that the location display did not actually start
            }
        }
    }
