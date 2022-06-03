package com.example.flowerfollower

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.ExifInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.flowerfollower.databinding.ActivityFlowerInfoBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

class FlowerInfoActivity : AppCompatActivity() {

    private lateinit var binding : ActivityFlowerInfoBinding
    private lateinit var uid : String
    private  val  storage = Firebase.storage // 파이어베이스 스토리지
    private val storageRef = storage.reference // 파이어베이스 스토리지
    private lateinit var database : DatabaseReference // 파이어베이스 리얼타임 데이터베이스
    private var date : String = "0" // 사진이 찍힌 날짜
    private var latitude : Float = 0f // 사진이 찍힌 위도
    private var longitude : Float = 0f // 사진이 찍힌 경도

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlowerInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        uid = intent.getStringExtra("uid")!!
        database = FirebaseDatabase.getInstance().getReference("User")
        setInfo()
        binding.PlantButton.setOnClickListener {
            requestExternalPermission()
        }
    }

    private fun requestExternalPermission() { // 저장 공간 접근 권한 받기
        when {
            (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) -> {
                Plant()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA) -> {
                externalAlertDlg()
            }
            else -> {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_EXTERNAL_STORAGE)
            }
        }
    }

    private fun externalAlertDlg() { // 저장 공간 접근 권한을 명시적으로 거부한 경우 다시 묻기
        val builder = AlertDialog.Builder(this)
        builder.setMessage("반드시 저장 공간 권한이 허용 되어야 합니다")
            .setTitle("권한 체크")
            .setPositiveButton("확인") { _, _ ->
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_EXTERNAL_STORAGE)
            }
            .setNegativeButton("취소") {dlg, _ ->
                dlg.dismiss()
            }
        val dlg = builder.create()
        dlg.show()
    }

    private fun setLatLang() { // 사진에서 위도 경도 읽기
        val path = intent.getStringExtra("path")!!
        val exif = ExifInterface(path)
        date = if(exif.getAttribute(ExifInterface.TAG_GPS_DATESTAMP) == null) "0" else exif.getAttribute(ExifInterface.TAG_GPS_DATESTAMP)!!
        val lat = if(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE) == null) "0" else exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)!!
        val lang = if(exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE) == null) "0" else exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)!!
        if(lat != "0" && lang != "0") {
            val latRef = if(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF) == null) "0" else exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)!!
            val langRef = if(exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF) == null) "0" else exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)!!
            Log.d("####", lat)
            Log.d("####", lang)
            if(latRef == "N") {
                latitude = convertToDegree(lat)
            } else {
                latitude = 0 - convertToDegree(lat)
            }

            if(langRef == "E") {
                longitude = convertToDegree(lang)
            } else {
                longitude = 0 - convertToDegree(lang)
            }
        }
    }

    private fun convertToDegree(str: String): Float { // 사진에서 읽은 위도, 경도는 형식이 달라서 일반적인 형식으로 변경하는 함수
        val dms = str.split(",")

        val d = dms[0].split("/")
        val d0 = d[0].toFloat()
        val d1 = d[1].toFloat()
        val floatD = d0 / d1

        val m = dms[1].split("/")
        val m0 = m[0].toFloat()
        val m1 = m[1].toFloat()
        val floatM = m0 / m1

        val s = dms[2].split("/")
        val s0 = s[0].toFloat()
        val s1 = s[1].toFloat()
        val floatS = s0 / s1

        return floatD + (floatM / 60) + (floatS / 3600)
    }

    private fun Plant() { // 마이 가든으로 보내기
        setLatLang()
        Log.d("####", latitude.toString())
        Log.d("####", longitude.toString())
        val flowerName = intent.getStringExtra("flowerName")!!
        val sdf = SimpleDateFormat("yyyy/MM/dd")
        val time : String = sdf.format(Date())
        val epoch = System.currentTimeMillis().toString()

        val sdf2 = SimpleDateFormat("yyyy/MM/dd hh:mm:ss")
        val now : String = sdf2.format(Date())
        val postingID = uid + "@" + now.replace('/', '-').trim()
        val postRef = storageRef.child("MyGardenImages/$uid/$postingID")
        var downloadUri : Uri

        val bitmap = (binding.ivFlowerimage.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val uploadTask = postRef.putBytes(data)

        binding.progressBar6.visibility = View.VISIBLE
        // 파이어베이스 스토리지에 꽃 사진 올리기
        val urlTask = uploadTask.continueWithTask { task ->
            if(!task.isSuccessful) {
                Log.d("####" , "마이 가든 에러1")
                Toast.makeText(applicationContext, "마이 가든에 심기를 실패했습니다. 잠시 후 다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
                binding.progressBar6.visibility = View.GONE
            }
            postRef.downloadUrl
        }.addOnCompleteListener { task ->
            if(task.isSuccessful) {
                downloadUri = task.result
                val item = gardenClass(flowerName, time, epoch, downloadUri.toString(), date, latitude.toString(), longitude.toString())
                //리얼타임 데이터베이스에 올리기
                database.child(uid).child("Garden").child(postingID).setValue(item).addOnSuccessListener {
                    Toast.makeText(applicationContext, "마이 가든에 " + flowerName+ "을/를 심었습니다", Toast.LENGTH_SHORT).show()
                    binding.progressBar6.visibility = View.GONE
                }.addOnFailureListener {
                    Toast.makeText(applicationContext, "마이 가든에 심기를 실패했습니다. 잠시 후 다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
                    binding.progressBar6.visibility = View.GONE
                }
            } else {
                Log.d("####" , "마이 가든 에러2")
                Toast.makeText(applicationContext, "마이 가든에 심기를 실패했습니다. 잠시 후 다시 시도해 주세요.", Toast.LENGTH_SHORT).show()
                binding.progressBar6.visibility = View.GONE
            }
        }
    }

    private fun setInfo() { // 꽃 사진 설정, 꽃에 대한 정보는 DB가 만들어지면 추가할 예정
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