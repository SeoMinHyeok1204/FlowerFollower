package com.example.flowerfollower

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.flowerfollower.databinding.ActivityWritingBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

class WritingActivity : AppCompatActivity() {

    private lateinit var binding : ActivityWritingBinding
    private lateinit var database : DatabaseReference
    private  val  storage = Firebase.storage
    private val storageRef = storage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWritingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        binding.backButton.setOnClickListener {
            finish()
        }
        binding.imageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_GALLERY)
        }
        binding.imageChangeButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_GALLERY)
        }
        binding.writeButton.setOnClickListener {
            sendFBDB()
        }
    }

    private fun sendFBDB() {
        closeKeyboard()
        binding.progressBar5.visibility = View.VISIBLE
        if(binding.inputTitle.text.toString().trim() == "") {
            Toast.makeText(applicationContext, "제목을 입력해 주세요", Toast.LENGTH_SHORT).show()
        }
        else if(binding.inputContent.text.toString().trim() == "") {
            Toast.makeText(applicationContext, "내용을 입력해 주세요", Toast.LENGTH_SHORT).show()
        }
        else {
            val uid = intent.getStringExtra("uid")!!
            val title = binding.inputTitle.text.toString()
            val content = binding.inputContent.text.toString()
            val sdf = SimpleDateFormat("yyyy/MM/dd hh:mm:ss")
            val time : String = sdf.format(Date())
            val commentNum = "0"
            val nickname = intent.getStringExtra("nickname")
            val epoch = System.currentTimeMillis().toString()
            val postingID = uid + "@" + time.replace('/', '-').trim()
            var downloadUri : Uri
            if(binding.writingImage.visibility == View.VISIBLE) {
                val postRef = storageRef.child("postingImages/$uid/$postingID")

                val bitmap = (binding.writingImage.drawable as BitmapDrawable).bitmap
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()
                val uploadTask = postRef.putBytes(data)

                val urlTask = uploadTask.continueWithTask { task ->
                    if(!task.isSuccessful) {
                        Log.d("####", "error")
                        binding.progressBar5.visibility = View.GONE
                    }
                    postRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if(task.isSuccessful) {
                        downloadUri = task.result
                        Log.d("####", "성공 : $downloadUri")
                        val item = CommunityPosting(uid, title, content, time, commentNum, nickname!!, downloadUri.toString(), epoch, postingID)
                        database = FirebaseDatabase.getInstance().getReference("Posting")
                        database.child(postingID).setValue(item).addOnSuccessListener {
                            binding.progressBar5.visibility = View.GONE
                            Toast.makeText(applicationContext, "글을 업로드 하였습니다", Toast.LENGTH_SHORT).show()
                            finish()
                        }.addOnFailureListener {
                            binding.progressBar5.visibility = View.GONE
                            Toast.makeText(applicationContext, "업로드에 실패 했습니다", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.d("####", "error")
                        binding.progressBar5.visibility = View.GONE
                    }
                }
            }
            else {
                val item = CommunityPosting(uid, title, content, time, commentNum, nickname!!, "0",epoch, postingID)
                database = FirebaseDatabase.getInstance().getReference("Posting")
                database.child(postingID).setValue(item).addOnSuccessListener {
                    Toast.makeText(applicationContext, "글을 업로드 하였습니다", Toast.LENGTH_SHORT).show()
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(applicationContext, "업로드에 실패 했습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun closeKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm : InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_GALLERY) {
            val image = data?.data
            binding.writingImage.setImageURI(image)
            binding.writingImage.visibility = View.VISIBLE
            binding.imageChangeButton.visibility = View.VISIBLE
            binding.imageButton.visibility = View.GONE

            binding.writingImage.layoutParams.height = 300
            binding.writingImage.layoutParams.width = 300
            binding.writingImage.requestLayout()
        }
    }
}