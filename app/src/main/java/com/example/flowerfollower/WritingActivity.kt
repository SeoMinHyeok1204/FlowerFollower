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
        binding.backButton.setOnClickListener { // 나가기
            finish()
        }
        binding.imageButton.setOnClickListener { // 갤러리로 보내서 글에 첨부할 사진 고르게 하기
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_GALLERY)
        }
        binding.imageChangeButton.setOnClickListener { // 첨부한 사진 변경
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_GALLERY)
        }
        binding.writeButton.setOnClickListener { // 글 쓰기
            sendFBDB()
        }
    }

    private fun sendFBDB() { // 작성한 글을 파이어베이스에 업로드
        closeKeyboard()
        binding.progressBar5.visibility = View.VISIBLE
        if(binding.inputTitle.text.toString().trim() == "") { // 제목이 빈 칸이면 안됨
            Toast.makeText(applicationContext, "제목을 입력해 주세요", Toast.LENGTH_SHORT).show()
        }
        else if(binding.inputContent.text.toString().trim() == "") { // 내용이 빈 칸이면 안됨
            Toast.makeText(applicationContext, "내용을 입력해 주세요", Toast.LENGTH_SHORT).show()
        }
        else { // 빈 칸 아니면 업로드
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
                // 글에 첨부한 사진은 스토리지에 저장
                val urlTask = uploadTask.continueWithTask { task ->
                    if(!task.isSuccessful) {
                        Log.d("####", "error")
                        binding.progressBar5.visibility = View.GONE
                    }
                    postRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if(task.isSuccessful) { // 스토리지에 사진 올렸으면 사진 외의 내용은 리얼타임 데이터베이스에 올리기
                        downloadUri = task.result
                        Log.d("####", "성공 : $downloadUri")
                        val item = CommunityPosting(uid, title, content, time, commentNum, nickname!!, downloadUri.toString(), epoch, postingID)
                        database = FirebaseDatabase.getInstance().getReference("Posting")
                        database.child(postingID).setValue(item).addOnSuccessListener { // 글 올리기 성공
                            binding.progressBar5.visibility = View.GONE
                            Toast.makeText(applicationContext, "글을 업로드 하였습니다", Toast.LENGTH_SHORT).show()
                            finish()
                        }.addOnFailureListener { // 글 올리기 실패
                            binding.progressBar5.visibility = View.GONE
                            Toast.makeText(applicationContext, "업로드에 실패 했습니다", Toast.LENGTH_SHORT).show()
                        }
                    } else { // 스토리지에 사진 올리기 실패
                        Log.d("####", "error")
                        binding.progressBar5.visibility = View.GONE
                    }
                }
            }
            else { // 글에 사진이 없는 경우 스토리지에 사진을 안올려도 되므로 간단해짐
                val item = CommunityPosting(uid, title, content, time, commentNum, nickname!!, "0",epoch, postingID)
                database = FirebaseDatabase.getInstance().getReference("Posting")
                database.child(postingID).setValue(item).addOnSuccessListener { // 글 올리기 성공
                    Toast.makeText(applicationContext, "글을 업로드 하였습니다", Toast.LENGTH_SHORT).show()
                    finish()
                }.addOnFailureListener { // 글 올리기 실패
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

    // 갤러리에서 사진 골랐으면 사진 첨부하고, '사진 첨부' 버튼을 '사진 변경' 버튼으로 바꾼다
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