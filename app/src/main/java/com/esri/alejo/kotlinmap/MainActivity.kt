package com.esri.alejo.kotlinmap

import android.content.Context
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import kotlinx.android.synthetic.main.activity_main.*
import com.esri.arcgisruntime.mapping.view.LocationDisplay
import android.widget.Toast
import android.view.View

class MainActivity : AppCompatActivity(), View.OnClickListener{
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.navigation_cerca -> {
                this.toast("autopanmode")
                println("nea")
                //locationDisplay?.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER)
                //locationDisplay?.startAsync()

            }
        }
    }

    var locationDisplay: LocationDisplay? = null
    //var view: View? = null

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
        myLocationButton.setOnClickListener(View.OnClickListener {
            this.toast("tocado")
            locationDisplay?.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER)
            locationDisplay?.startAsync()
        })
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        // create a map with the BasemapType topographic
        val map = ArcGISMap(Basemap.Type.TOPOGRAPHIC, 4.6097100, -74.0817500, 16)
        // set the map to be displayed in the layout's MapView
        mapView2.map = map
        locationDisplay = mapView2.locationDisplay
        //geoLocalizacion()
    }

    override fun onPause() {
        super.onPause()
        mapView2.pause()
    }

    override fun onResume() {
        super.onResume()
        mapView2.resume()
    }

    private fun geoLocalizacion() {
        try {
            locationDisplay?.addDataSourceStatusChangedListener(LocationDisplay.DataSourceStatusChangedListener { dataSourceStatusChangedEvent ->
                if (dataSourceStatusChangedEvent.isStarted)
                    return@DataSourceStatusChangedListener

                if (dataSourceStatusChangedEvent.error == null)
                    return@DataSourceStatusChangedListener
            })
            locationDisplay?.startAsync()
        } catch (e: Exception) {
            Toast.makeText(mapView2.context, e.message.toString(), Toast.LENGTH_LONG).show()
        }

    }
    fun Context.toast(message: CharSequence) =
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
