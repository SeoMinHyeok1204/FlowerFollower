package com.example.flowerfollower

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flowerfollower.databinding.ActivityMainBinding
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import org.tensorflow.lite.Interpreter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.experimental.or

const val REQUEST_GALLERY = 100
const val REQUEST_CAMERA = 200
const val REQUEST_EXTERNAL_STORAGE = 300
const val IMAGE_MEAN = 127.5f
const val IMAGE_STD = 127.5f

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding : ActivityMainBinding
    private lateinit var user : FirebaseUser
    private lateinit var reference: DatabaseReference
    private lateinit var uid : String
    private val flowerNames = arrayOf("아카시아", "아네모네", "철쭉", "도라지", "베르가모트", "금낭화", "동백", "카네이션", "개박하", "수레국화",
                                                    "코스모스", "크로커스", "데이지", "달리아", "민들레", "물망초", "개나리", "프리지아", "아이리스", "자스민",
                                                    "라벤더", "라일락", "연꽃", "마리골드", "미모사", "나팔꽃", "장미", "해바라기", "튤립", "제비꽃")
    private lateinit var inputBuffer: ByteBuffer
    private var predictResult: String? = null
    private var probability : Float? = null
    private var bitmapImage: Bitmap? = null
    private var pixelArray = IntArray(224 * 224)
    private lateinit var interpreter: Interpreter
    private lateinit var nickname : String
    private lateinit var database : DatabaseReference
    private lateinit var usermail : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        user = FirebaseAuth.getInstance().currentUser!!
        reference = FirebaseDatabase.getInstance().getReference("User")
        usermail = user.email.toString()
        uid = user.uid

        reference.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profile = snapshot.getValue(UserInfo::class.java)
                if(profile != null) {
                    nickname = profile.nickName
                    setRecyclerView()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "오류가 발생했습니다. 다시 시도해 주세요", Toast.LENGTH_SHORT).show()
            }
        })
        interpreter = Interpreter(loadModel(), null)
        binding.bottomNav.setOnItemSelectedListener(this)

        binding.myinfobtn.setOnClickListener {
            openMyInfo()
        }
    }

    //open Myinfo2Activity
    private fun openMyInfo(){
        val intent = Intent(this, MyInfo2Activity::class.java)
        intent.putExtra("nickname", nickname)
        intent.putExtra("usermail",usermail)
        intent.putExtra("uid", uid)
        startActivity(intent)
    }

    private fun setRecyclerView() {
        val array : ArrayList<CommunityPosting> = ArrayList()

        database = FirebaseDatabase.getInstance().getReference("Posting")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.progressBar4.visibility = View.VISIBLE
                array.clear()
                for (data in snapshot.children) {
                    val uid = data.child("uid").value as String
                    val title = data.child("title").value as String
                    val content = data.child("content").value as String
                    val time = data.child("time").value as String
                    val commentNum = data.child("commentNum").value as String
                    val nickname = data.child("nickname").value as String
                    val imageUrl = data.child("imageUrl").value as String
                    val epoch = data.child("epoch").value as String
                    val postingID = data.child("postingID").value as String


                    val item = CommunityPosting(uid, title, content, time, commentNum, nickname, imageUrl, epoch, postingID)
                    array.add(item)
                }
                if(array.size > 1) {
                    array.sortWith(Comparator { p0, p1 -> p0!!.epoch!!.toLong().compareTo(p1!!.epoch!!.toLong()) * -1})
                }
                binding.communityRecyclerview.adapter?.notifyDataSetChanged()
                binding.progressBar4.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "게시글을 불러 오지못했습니다", Toast.LENGTH_SHORT).show()
            }
        })


        binding.communityRecyclerview.layoutManager = LinearLayoutManager(null)
        binding.communityRecyclerview.setHasFixedSize(true)
        binding.communityRecyclerview.adapter = CommunityPostingAdapter(array, uid, nickname)
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

    private fun getRealPathFromURI(context: Context, contentUri : Uri) : String{
        var cursor : Cursor?
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        cursor = context.contentResolver.query(contentUri, proj, null, null, null)
        val column_index = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)!!
        cursor.moveToFirst()
        return cursor.getString(column_index)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_GALLERY) {
            bitmapImage = MediaStore.Images.Media.getBitmap(contentResolver, data?.data)
            val image = data?.data
            val tmp = getRealPathFromURI(this, image!!)
            Log.d("!@#$", tmp)
            predict()

            val intent = Intent(this, FlowerInfoActivity::class.java)
            intent.putExtra("flowerName", predictResult)
            intent.putExtra("probability", probability)
            intent.putExtra("imageUri", image.toString())
            intent.putExtra("uid", uid)
            intent.putExtra("path", tmp)
            startActivity(intent)
        }

        else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CAMERA) {
            bitmapImage = data?.extras?.get("data") as Bitmap
            val image = data.extras?.get("data") as Bitmap
            val baos = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val path = MediaStore.Images.Media.insertImage(contentResolver, image, "Title", null)
            val uri = Uri.parse(path)
            val tmp = getRealPathFromURI(this, uri)
            Log.d("!@#$", tmp)
            predict()

            val intent = Intent(this, FlowerInfoActivity::class.java)
            intent.putExtra("flowerName", predictResult)
            intent.putExtra("probability", probability)
            intent.putExtra("imageBitmap", image)
            intent.putExtra("uid", uid)
            intent.putExtra("path", tmp)
            startActivity(intent)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    private fun openMyGarden() {
        val intent = Intent(this, MyGardenActivity::class.java)
        intent.putExtra("nickname", nickname)
        intent.putExtra("uid", uid)
        startActivity(intent)
    }

    private fun openWrite() {
        val intent = Intent(this, WritingActivity::class.java)
        intent.putExtra("nickname", nickname)
        intent.putExtra("uid", uid)
        startActivity(intent)
    }

    private fun loadModel(): ByteBuffer {
        val assetManager = resources.assets
        val assetFileDescriptor = assetManager.openFd("MyModel_30.tflite")
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

        val output = Array(1) { FloatArray(30) }

        interpreter.run(inputBuffer, output)

        var maxIndex = 0
        for(index in 0..29) {
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