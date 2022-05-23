package com.example.flowerfollower

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
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
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

const val REQUEST_GALLERY = 100
const val REQUEST_CAMERA = 200
const val IMAGE_MEAN = 127.5f
const val IMAGE_STD = 127.5f

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding : ActivityMainBinding
    private lateinit var user : FirebaseUser
    private lateinit var reference: DatabaseReference
    private lateinit var uid : String
    private val flowerNames = arrayOf("부겐빌레아", "데이지", "장미", "치자나무", "히비스커스", "수국", "백합", "호접란", "작약", "튤립")
    private lateinit var inputBuffer: ByteBuffer
    private var predictResult: String? = null
    private var probability : Float? = null
    private var bitmapImage: Bitmap? = null
    private var pixelArray = IntArray(224 * 224)
    private lateinit var interpreter: Interpreter

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
        interpreter = Interpreter(loadModel(), null)
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
            bitmapImage = MediaStore.Images.Media.getBitmap(contentResolver, data?.data)
            val image = data?.data
            predict()

            val intent = Intent(this, FlowerInfoActivity::class.java)
            intent.putExtra("flowerName", predictResult)
            intent.putExtra("probability", probability)
            intent.putExtra("imageUri", image.toString())
            startActivity(intent)
        }

        else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CAMERA) {
            bitmapImage = data?.extras?.get("data") as Bitmap
            val image = data.extras?.get("data") as Bitmap
            predict()

            val intent = Intent(this, FlowerInfoActivity::class.java)
            intent.putExtra("flowerName", predictResult)
            intent.putExtra("probability", probability)
            intent.putExtra("imageBitmap", image)
            startActivity(intent)
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

    private fun openWrite() {
        Toast.makeText(this, "글쓰기", Toast.LENGTH_SHORT).show()
    }

    private fun loadModel(): ByteBuffer {
        val assetManager = resources.assets
        val assetFileDescriptor = assetManager.openFd("MyModel.tflite")
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val length = assetFileDescriptor.length

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, length)
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap) {
        inputBuffer.rewind()

        bitmap.getPixels(pixelArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        var index = 0
        for (i in 0 until 224) {
            for (j in 0 until 224) {
                val value = pixelArray[index++]
                inputBuffer.putFloat(((value shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                inputBuffer.putFloat(((value shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                inputBuffer.putFloat(((value and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
            }
        }
    }

    private fun predict() {
        val inputShape = interpreter.getInputTensor(0).shape()
        val inputWidth = inputShape[1] //224
        val inputHeight = inputShape[2] //224

        val resizedBitmap = Bitmap.createScaledBitmap(bitmapImage!!, inputWidth, inputHeight, true)

        inputBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3)
        inputBuffer.order(ByteOrder.nativeOrder())
        convertBitmapToByteBuffer(resizedBitmap)

        val output = Array(1) { FloatArray(10) }

        interpreter.run(inputBuffer, output)

        var maxIndex = 0
        for(index in 0..9) {
            if(output[0][maxIndex] < output[0][index])
                maxIndex = index
        }
        predictResult = flowerNames[maxIndex]
        probability = output[0][maxIndex]
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.bottom_camera -> {
                val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_layout, null)
                val bottomSheetDialog = BottomSheetDialog(this)
                bottomSheetDialog.setContentView(bottomSheetView)
                bottomSheetDialog.show()

                bottomSheetDialog.findViewById<LinearLayout>(R.id.camera_section)?.setOnClickListener {
                    openCamera()
                    bottomSheetDialog.dismiss()
                }
                bottomSheetDialog.findViewById<LinearLayout>(R.id.gallery_section)?.setOnClickListener {
                    openGallery()
                    bottomSheetDialog.dismiss()
                }
            }
            R.id.bottom_write -> {
                openWrite()
            }
            R.id.bottom_myGarden -> {
                openMyGarden()
            }
        }

        return false
    }
}