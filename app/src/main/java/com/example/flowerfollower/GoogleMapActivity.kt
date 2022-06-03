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

    private fun init() { // 위도 경도 정보 받아서 그 위치로 카메라 옮기고 옮기기만 하면 허전해서 마커도 찍어줌
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        //위도 경도 정보 받기
        val latitude = intent.getStringExtra("latitude")!!
        val longitude = intent.getStringExtra("longitude")!!
        val location = LatLng(latitude.toDouble(), longitude.toDouble())
        val options = MarkerOptions()
        options.position(location)
        mapFragment.getMapAsync {
            googleMap = it
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 16.0f))
            googleMap.addMarker(options)
        }
    }
}