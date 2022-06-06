package com.example.flowerfollower

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.flowerfollower.databinding.ActivityInGardenBinding
import com.google.firebase.database.*

class InGardenActivity : AppCompatActivity() {

    lateinit var binding : ActivityInGardenBinding
    lateinit var flowerDB : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInGardenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        flowerDB = FirebaseDatabase.getInstance().getReference("Flower")
        init()
    }

    private fun init() { // 화면 설정, 위도 경도 정보 있는 경우 버튼 누르면 구글 맵으로 이동
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

        flowerDB.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(data in snapshot.children) {
                    val name = data.child("name").value as String
                    if(name == flowerName) {
                        val flowerLanguage = data.child("flowerlanguage").value as String
                        val propagation = data.child("propagation").value as String
                        val habitat = data.child("habitat").value as String
                        val scientificName = data.child("scientificName").value as String
                        val controlLevel = data.child("controlLevel").value as String
                        val controlRequest = data.child("controlRequest").value as String
                        val lightDemand = data.child("lightDemand").value as String

                        binding.flowerLanguage.text = flowerLanguage
                        binding.propagation.text = propagation
                        binding.habitat.text = habitat
                        binding.scientificName.text = scientificName
                        binding.controlLevel.text = controlLevel
                        binding.controlRequest.text = controlRequest
                        binding.lightDemand.text = lightDemand

                        return
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@InGardenActivity, "정보를 불러오지 못했습니다. 잠시 후 다시 시도해주세요", Toast.LENGTH_SHORT).show()
            }
        })
    }
}