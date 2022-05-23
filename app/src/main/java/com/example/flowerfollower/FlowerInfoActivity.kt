package com.example.flowerfollower

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.flowerfollower.databinding.ActivityFlowerInfoBinding
import kotlin.math.round

class FlowerInfoActivity : AppCompatActivity() {

    private lateinit var binding : ActivityFlowerInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlowerInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        setInfo()
    }

    private fun setInfo() {
        val flowerName = intent.getStringExtra("flowerName")
        val probability = intent.getFloatExtra("probability", 0f)
        val nameAndProbability = flowerName + ", " + round(probability * 100).toInt().toString() + "%"
        binding.tvFlowername.text = nameAndProbability

        val imageUri = intent.getStringExtra("imageUri")
        if(imageUri != null)
            binding.ivFlowerimage.setImageURI(Uri.parse(imageUri))

        val imageBitmap = intent.getParcelableExtra<Bitmap>("imageBitmap")
        if(imageBitmap != null)
            binding.ivFlowerimage.setImageBitmap(imageBitmap)
    }
}