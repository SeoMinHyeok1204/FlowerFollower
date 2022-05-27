package com.example.flowerfollower

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.flowerfollower.databinding.ActivityInPostingBinding
import com.google.firebase.database.*
import java.lang.System.currentTimeMillis
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class InPostingActivity : AppCompatActivity() {

    private lateinit var binding : ActivityInPostingBinding
    private lateinit var database : DatabaseReference
    private lateinit var array : ArrayList<Comment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInPostingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        database = FirebaseDatabase.getInstance().getReference("Posting").child(intent.getStringExtra("postingID")!!)

        val writer = intent.getStringExtra("writer")
        val time = intent.getStringExtra("time")
        val title = intent.getStringExtra("title")
        val content = intent.getStringExtra("content")
        val imageUrl = intent.getStringExtra("imageUrl")

        val display = windowManager.defaultDisplay
        val rvParams = binding.rvComment.layoutParams
        rvParams.width = display.width - display.width/12
        binding.rvComment.layoutParams = rvParams

        binding.apply {
            postWriter.text = writer
            postTime.text = time
            postTitle.text = title
            postContent.text = content
            if(imageUrl == "0") {
                postImage.visibility = View.GONE
            }
            else {
                Glide.with(this@InPostingActivity).load(imageUrl).into(postImage)
            }
            array = ArrayList()
            showComment()
            postCommentButton.setOnClickListener {
                closeKeyboard()
                uploadComment()
            }
        }
    }

    private fun showComment() {
        database.child("comment").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                array.clear()
                for(data in snapshot.children) {
                    //(val writer : String, val id : String, val content : String, val time : String, val epoch : String, val commentID : String)
                    val writer = data.child("writer").value as String
                    val id = data.child("id").value as String
                    val content = data.child("content").value as String
                    val time = data.child("time").value as String
                    val epoch = data.child("epoch").value as String
                    val commentID = data.child("commentID").value as String

                    array.add(Comment(writer, id, content, time, epoch, commentID))
                }
                if(array.size > 1) {
                    array.sortWith(Comparator { p0, p1 -> p0!!.epoch!!.toLong().compareTo(p1!!.epoch!!.toLong())})
                }
                binding.rvComment.adapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "댓글을 불러 오지못했습니다", Toast.LENGTH_SHORT).show()
            }
        })

        binding.rvComment.layoutManager = LinearLayoutManager(null)
        binding.rvComment.setHasFixedSize(true)
        binding.rvComment.adapter = CommentAdapter(array)
    }

    private fun uploadComment() {
        if(binding.postComment.text.trim() == "") {
            Toast.makeText(applicationContext, "내용을 입력해 주세요", Toast.LENGTH_SHORT).show()
        }
        else {
            val sdf = SimpleDateFormat("yyyy/MM/dd hh:mm:ss")
            val time : String = sdf.format(Date())
            val currentUID = intent.getStringExtra("currentUserUID")!!
            val currentNickname = intent.getStringExtra("currentUserNickname")!!
            val commentID = currentUID + "@" + time.replace('/', '-').trim()
            var commentNum = 0

            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    commentNum = snapshot.child("comment").childrenCount.toInt()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

            val comment = Comment(currentNickname, currentUID, binding.postComment.text.toString(), time, currentTimeMillis().toString(), commentID)
            database.child("comment").child(commentID).setValue(comment).addOnSuccessListener {
                database.child("commentNum").setValue(commentNum.toString())
                binding.postComment.text.clear()
            }.addOnFailureListener {
                Toast.makeText(applicationContext, "댓글을 게시하지 못했습니다", Toast.LENGTH_SHORT).show()
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
}