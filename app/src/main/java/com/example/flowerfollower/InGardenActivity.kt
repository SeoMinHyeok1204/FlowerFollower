package com.example.flowerfollower

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.flowerfollower.databinding.ActivityInGardenBinding

class InGardenActivity : AppCompatActivity() {

    lateinit var binding : ActivityInGardenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInGardenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        val flowerName = intent.getStringExtra("flowerName")!!
        val latitude = intent.getStringExtra("latitude")!!
        val longitude = intent.getStringExtra("longitude")!!
        val imageUrl = intent.getStringExtra("imageUrl")!!

        binding.flowerName.text = flowerName
        Glide.with(binding.flowerImage.context).load(imageUrl).into(binding.flowerImage)
        binding.LocationButton.setOnClickListener {
            if(latitude == "0.0" && longitude == "0.0") {
                Toast.makeText(this, "위치 정보를 알 수 없는 사진입니다", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, GoogleMapActivity::class.java)
                intent.putExtra("latitude", latitude)
                intent.putExtra("longitude", longitude)
                startActivity(intent)
            }
        }
    }
}