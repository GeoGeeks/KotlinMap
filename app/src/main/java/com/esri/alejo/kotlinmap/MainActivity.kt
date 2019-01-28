package com.esri.alejo.kotlinmap

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.esri.arcgisruntime.mapping.ArcGISMap
import kotlinx.android.synthetic.main.activity_main.*
import com.esri.arcgisruntime.mapping.view.LocationDisplay
import android.view.View
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.MapView
import android.view.MotionEvent
import android.widget.*
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.layers.LayerContent
import java.util.concurrent.ExecutionException
import com.koushikdutta.ion.Ion;
import java.net.URLEncoder


class MainActivity : AppCompatActivity(){
    var pos = 0
    var locationDisplay: LocationDisplay? = null
    var lblCategoria: TextView? = null
    var lugarNombre: TextView? = null
    var direccionLugar: TextView? = null
    var fotoLugar: ImageView? = null
    var btnTelefono: Button? = null
    var btnWhatsapp: ImageButton? = null

    private var mbut: ImageButton? = null
    //var view: View? = null
    private val requestCode = 2
    var map: ArcGISMap? = null

    internal var reqPermissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission
            .ACCESS_COARSE_LOCATION,Manifest.permission.CALL_PHONE )

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
        closePopup.setOnClickListener{
            contentPopup?.visibility = View.GONE
        }
        //btnTelefono?.text = "573107746702"
        //btnTelefono?.setOnClickListener {
            //llamar al telefono
        //    callToNumber(btnTelefono?.text.toString())
        //}
        //btnWhatsapp.setOnClickListener{
            //dispara whatsapp
        //    sendMessageToWhatsAppContact("573107746702");
        //}
        mbut = findViewById(R.id.myLocationButton) as ImageButton
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        // create a map with the BasemapType topographic
        map = ArcGISMap("http://geogeeks2.maps.arcgis.com/home/item.html?id=b120d5eeb44448e9a43f508850caa890")
        //val map = ArcGISMap(Basemap.Type.TOPOGRAPHIC, 4.6097100, -74.0817500, 16)
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
        
            //mapView2.setOnTouchListener(new IdentifyFeatureLayerTouchListener(view.getContext(), vistaMap))
            mapView2.setOnTouchListener(IdentifyFeatureLayerTouchListener(this.applicationContext,mapView2))
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

        class IdentifyFeatureLayerTouchListener(context: Context?, mapView: MapView?) : DefaultMapViewOnTouchListener(context, mapView) {
            private val layer: FeatureLayer? = null // reference to the layer to identify features in

            // override the onSingleTapConfirmed gesture to handle a single tap on the MapView
            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                // get the screen point where user tapped
                val screenPoint = Point(e!!.x.toInt(), e!!.y.toInt())
                val identifyFuture = super.mMapView.identifyLayersAsync(screenPoint, 5.0,
                        false)

                // add a listener to the future
                identifyFuture.addDoneListener {
                    try {
                        // get the identify results from the future - returns when the operation is complete
                        val identifyLayersResults = identifyFuture.get()

                        // iterate all the layers in the identify result
                        for (identifyLayerResult in identifyLayersResults) {

                            // each identified layer should find only one or zero results, when identifying topmost GeoElement only
                            if (identifyLayerResult.elements.size > 0) {
                                val topmostElement = identifyLayerResult.elements[0]
                                if (topmostElement is Feature) {
                                    val identifiedFeature = topmostElement as Feature

                                    // Use feature as required, for example access attributes or geometry, select, build a table, etc...
                                    MainActivity().processIdentifyFeatureResult(identifiedFeature, identifyLayerResult.layerContent)
                                }
                            }
                        }
                    } catch (ex: InterruptedException) {
                        //dealWithException(ex); // must deal with exceptions thrown from the async identify operation
                    } catch (ex: ExecutionException) {
                    }
                }
                return super.onSingleTapConfirmed(e)
            }
        }

        private fun mostrarPopup(categoriaNombre: String?, nombre: String?, direccion: String?, telefono: String?){

            //Log.e(MainActivity.TAG, nombre+", "+direccion+", "+foto);
            lblCategoria?.text = categoriaNombre
            lugarNombre?.text = nombre
            direccionLugar?.text = direccion
            btnTelefono?.text = telefono
            //if(foto != null){
            //        Ion.with(fotoLugar).load(foto)
            //}else{
            //    Ion.with(fotoLugar).load("http://geoapps.esri.co/recursos/CCU2017/bogota.jpg")
            //}
            ///disparar Whatsapp
            btnWhatsapp?.setOnClickListener{
                //dispara whatsapp
                println("wp:"+telefono)
                sendMessageToWhatsAppContact("57"+telefono!!)
            }
            ///disparar llamada a celular
            btnTelefono?.setOnClickListener {
                //llamar al telefono
                println("llamar:"+telefono)
                callToNumber("57"+btnTelefono!!.text.toString())
            }
            contentPopup?.visibility = View.VISIBLE
            //popup.setVisibility(View.VISIBLE)
        }

        fun processIdentifyFeatureResult(feature: Feature?, content: LayerContent?){
            var nombre: String?
            var direccion: String?
            var foto: String?
            var telefono: String?
            //valores para obtener de la capa
            val ensayadero = "Nombre"
            val dir = "Dirección"
            val fot = "Foto"
            val tel = "Telefono"
            when (content?.name){
                "Ensayaderos" -> {
                    nombre = feature?.attributes!!.get(ensayadero).toString()
                    direccion = feature?.attributes!!.get(dir).toString()
                    telefono = feature?.attributes!!.get(tel).toString()
                    foto = feature?.attributes!!.get(fot).toString()
                    println(nombre + ':' + direccion + ':' + telefono + ':')
                    mostrarPopup("Ensayadero", nombre, direccion, telefono)
                }
                "TiendaMusica" ->{
                    nombre = feature?.attributes!!.get(ensayadero).toString()
                    direccion = feature?.attributes!!.get(dir).toString()
                    telefono = feature?.attributes!!.get(tel).toString()
                    foto = feature?.attributes!!.get(fot).toString()
                    mostrarPopup("Tienda de Música", nombre, direccion, telefono)
                }
            }
        }

        private fun sendMessageToWhatsAppContact(number: String) {
            val packageManager = this.getPackageManager()
            val i = Intent(Intent.ACTION_VIEW)
            try {
                val url = "https://api.whatsapp.com/send?phone=" + number + "&text=" + URLEncoder.encode("holas", "UTF-8")
                i.setPackage("com.whatsapp")
                i.data = Uri.parse(url)
                if (i.resolveActivity(packageManager) != null) {
                    this.startActivity(i)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        private fun callToNumber(number: String){
            // If an error is found, handle the failure to start.
            // Check permissions to see if failure may be due to lack of permissions.
            val permissionCheck3 = ContextCompat.checkSelfPermission(this@MainActivity, reqPermissions[2]) ==
                    PackageManager.PERMISSION_GRANTED

            if (!(permissionCheck3)) {
                // If permissions are not already granted, request permission from the user.
                ActivityCompat.requestPermissions(this@MainActivity, reqPermissions, requestCode)

            } else {
                val intent = Intent(Intent.ACTION_CALL)
                intent.data = Uri.parse("tel:$number")
                this.startActivity(intent)
            }
        }
    }
