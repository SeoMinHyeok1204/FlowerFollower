package com.example.flowerfollower

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
    private lateinit var array : ArrayList<Comment> // 댓글을 저장할 배열
    private lateinit var uid : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInPostingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        database = FirebaseDatabase.getInstance().getReference("Posting").child(intent.getStringExtra("postingID")!!)
        // 글에 대한 기본적인 정보 받기
        val writer = intent.getStringExtra("writer")
        val time = intent.getStringExtra("time")
        val title = intent.getStringExtra("title")
        val content = intent.getStringExtra("content")
        val imageUrl = intent.getStringExtra("imageUrl")
        val writerUid = intent.getStringExtra("writerUID")
        uid = intent.getStringExtra("currentUserUID")!!
        // 현재 유저의 uid와 글 쓴 사람의 uid가 같은 경우만 삭제 버튼이 보이게 함
        if(writerUid == uid) {
            binding.postingEraseButton.visibility = View.VISIBLE
        }

        val display = windowManager.defaultDisplay
        val rvParams = binding.rvComment.layoutParams
        rvParams.width = display.width - display.width/12
        binding.rvComment.layoutParams = rvParams

        binding.apply { // 위에서 받은 기본 정보대로 설정
            postWriter.text = writer
            postTime.text = time
            postTitle.text = title
            postContent.text = content
            if(imageUrl == "0") { // 이미지가 없으면 이미지 뷰 안보이게 하기
                postImage.visibility = View.GONE
            }
            else { // 이미지가 있으면 스토리지에서 가져오기
                Glide.with(this@InPostingActivity).load(imageUrl).into(postImage)
            }
            array = ArrayList()
            showComment()
            postCommentButton.setOnClickListener {
                closeKeyboard()
                uploadComment()
            }
            postingEraseButton.setOnClickListener {
                postingDeleteAlertDlg()
            }
        }
    }

    private fun postingDeleteAlertDlg() { // 글 삭제할지 묻는 다이얼로그
        val builder = AlertDialog.Builder(this)
        builder.setMessage("글을 삭제하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                database.removeValue()
                finish()
            }
            .setNegativeButton("취소") { dlg, _ ->
                dlg.dismiss()
            }
        val dlg = builder.create()
        dlg.show()
    }

    private fun showComment() { // 파이어베이스에서 댓글들 읽어와서 보여주기
        // 파이어베이스에서 읽기
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
                if(array.size > 1) { // 먼저 써진 댓글이 위로 오도록 정렬
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
        binding.rvComment.adapter = CommentAdapter(array, uid, intent.getStringExtra("postingID")!!)
    }

    private fun uploadComment() { // 댓글 작성하기
        if(binding.postComment.text.trim() == "") {
            Toast.makeText(applicationContext, "내용을 입력해 주세요", Toast.LENGTH_SHORT).show()
        }
        else {
            val sdf = SimpleDateFormat("yyyy/MM/dd hh:mm:ss")
            val time : String = sdf.format(Date())
            val currentUID = uid
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

    private fun closeKeyboard() { // 키보드 닫기
        val view = this.currentFocus
        if (view != null) {
            val imm : InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}