package com.yunussevimli.mapsprojesi

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.yunussevimli.mapsprojesi.databinding.ActivityMapsBinding
import java.util.Locale

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager: LocationManager // LocationManager tanımlaması. GPS ve İnternet bağlantısını kontrol eder.
    private lateinit var locationListener: LocationListener // LocationListener tanımlaması. Konum değişikliklerini dinler.
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    var takipBoolean : Boolean? = null
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        registerLauncher() // İzinlerin alınması için registerLauncher fonksiyonu çağrılır.

        sharedPreferences = getSharedPreferences("com.yunussevimli.mapsprojesi", MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("takipBoolean", false).apply() // Başlangıçta sıfırlayın
        takipBoolean = false
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(this)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager // LocationManager tanımlaması.
        locationListener = object : LocationListener { // LocationListener tanımlaması.
            override fun onLocationChanged(location: Location) {
                takipBoolean = sharedPreferences.getBoolean("takipBoolean",false)
                if(!takipBoolean!!){
                    mMap.clear()
                    val userLocation = LatLng(location.latitude, location.longitude) // Kullanıcının konumu alınır.
                    mMap.addMarker(MarkerOptions().position(userLocation).title("Konumunuz")) // Kullanıcının konumu haritada işaretlenir.
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f)) // Kullanıcının konumu haritada gösterilir.
                    sharedPreferences.edit().putBoolean("takipBoolean",true).apply()
                    takipBoolean = sharedPreferences.getBoolean("takipBoolean",true)
                }
            }
        }

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                Snackbar.make(binding.root,"Konumunuzu almak için izin gerekli!",Snackbar.LENGTH_INDEFINITE).setAction(
                    "İzin Ver"
                ) {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }.show()
            } else {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1,1f,locationListener)
            val sonBilinenKonum = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (sonBilinenKonum != null){
                val sonBilinenLatLng = LatLng(sonBilinenKonum.latitude,sonBilinenKonum.longitude)
            }
        }
    }

    private fun registerLauncher(){
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                if (ContextCompat.checkSelfPermission(this@MapsActivity,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1,1f,locationListener)
                    val sonBilinenKonum = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (sonBilinenKonum != null){
                        val sonBilinenLatLng = LatLng(sonBilinenKonum.latitude,sonBilinenKonum.longitude)
                    }
                } else {
                    Toast.makeText(this@MapsActivity,"İzne ihtiyacımız var.",Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onMapLongClick(p0: LatLng) {
        mMap.clear()

        val geocoder = Geocoder(this, Locale.getDefault())
        var adres = ""

        try {
             // val liste = geocoder.getFromLocation(p0.latitude,p0.longitude,1) eski sürümlerde geçerli hali
            // adres = liste[0]
            geocoder.getFromLocation(p0.latitude,p0.longitude,1, Geocoder.GeocodeListener { adresListesi ->
                val ilkAdres = adresListesi.first()
                val ulkeAdi = ilkAdres.countryName
                val sehirAdi = ilkAdres.adminArea
                val ilceAdi = ilkAdres.subAdminArea
                val mahalleAdi = ilkAdres.subLocality
                val sokakAdi = ilkAdres.thoroughfare
                val kapiNo = ilkAdres.featureName

                adres = "$ulkeAdi, $sehirAdi, $ilceAdi, $mahalleAdi, $sokakAdi, $kapiNo"
                println(adres)
            })
        } catch (e: Exception){
            e.printStackTrace()
        }
    }
}