package com.example.flowerfollower

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.flowerfollower.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

const val REQUEST_GALLERY = 100
const val REQUEST_CAMERA = 200
const val IMAGE_MEAN = 127.5f
const val IMAGE_STD = 127.5f

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding : ActivityMainBinding
    private lateinit var user : FirebaseUser
    private lateinit var reference: DatabaseReference
    private lateinit var uid : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
//        user = FirebaseAuth.getInstance().currentUser!!
//        reference = FirebaseDatabase.getInstance().getReference("User")
//        uid = user.uid
//
//        reference.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val profile = snapshot.getValue(UserInfo::class.java)
//
//                if(profile != null) {
//                    val nickname = profile.nickName
//                    val email = profile.email
//                    val password = profile.password
//
//                    binding.apply {
//                        tvNickname.text = nickname
//                        tvEmail.text = email
//                        tvPassword.text = password
//                    }
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(this@MainActivity, "오류가 발생했습니다. 다시 시도해 주세요", Toast.LENGTH_SHORT).show()
//            }
//        })

        binding.bottomNav.setOnItemSelectedListener(this)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        when {
            (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) -> {
                startActivityForResult(intent, REQUEST_CAMERA)
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA) -> {
                cameraAlertDlg()
            }
            else -> {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), REQUEST_CAMERA)
            }
        }
    }

    private fun cameraAlertDlg() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("반드시 카메라 권한이 허용 되어야 합니다")
            .setTitle("권한 체크")
            .setPositiveButton("확인") { _, _ ->
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), REQUEST_CAMERA)
            }
            .setNegativeButton("취소") {dlg, _ ->
                dlg.dismiss()
            }
        val dlg = builder.create()
        dlg.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_GALLERY) {
//            bitmapImage =
//                MediaStore.Images.Media.getBitmap(contentResolver, data?.data)
//            predict()
//            val intent2 = Intent(this, FoodCustomActivity::class.java)
//            intent2.putExtra("result", predictResult)
//            intent2.putExtra("date", date)
//            intent2.putExtra("time", time)
//            startActivity(intent2)
            Toast.makeText(this, "갤러리에서 돌아옴", Toast.LENGTH_SHORT).show()

        }

        else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CAMERA) {
//            bitmapImage =
//                data?.extras?.get("data") as Bitmap
//            predict()
//            val intent2 = Intent(this, FoodCustomActivity::class.java)
//            intent2.putExtra("result", predictResult)
//            intent2.putExtra("date", date)
//            intent2.putExtra("time", time)
//            startActivity(intent2)
            Toast.makeText(this, "카메라에서 돌아옴", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    private fun openMyGarden() {
        Toast.makeText(this, "마이 가든", Toast.LENGTH_SHORT).show()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.bottom_camera -> {
                openCamera()
            }
            R.id.bottom_gallery -> {
                openGallery()
            }
            R.id.bottom_myGarden -> {
                openMyGarden()
            }
        }

        return false
    }
}