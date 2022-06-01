package com.example.flowerfollower

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.flowerfollower.databinding.ActivityGoogleMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class GoogleMapActivity : AppCompatActivity() {

    private lateinit var binding : ActivityGoogleMapBinding
    private lateinit var googleMap : GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoogleMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        val latitude = intent.getStringExtra("latitude")!!
        val longitude = intent.getStringExtra("longitude")!!
        val location = LatLng(latitude.toDouble(), longitude.toDouble())
        val options = MarkerOptions()
        options.position(location)
        //options.icon()
        mapFragment.getMapAsync {
            googleMap = it
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 16.0f))
            googleMap.addMarker(options)
        }
    }
}