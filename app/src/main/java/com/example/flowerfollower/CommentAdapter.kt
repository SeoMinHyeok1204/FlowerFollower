package com.example.flowerfollower

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class CommentAdapter(private val commentList : ArrayList<Comment>, private val uid : String, private val postingID : String) : RecyclerView.Adapter<CommentAdapter.CustomViewHolder>() {

    private lateinit var database : DatabaseReference

    inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val who: TextView = itemView.findViewById(R.id.comment_who)
        val content : TextView = itemView.findViewById(R.id.comment_content)
        val time : TextView = itemView.findViewById(R.id.comment_time)
        val eraseButton : ImageView = itemView.findViewById(R.id.comment_erase_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comment_recyclerview_item, parent, false)
        database = FirebaseDatabase.getInstance().getReference("Posting").child(postingID)
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.who.text = commentList[position].writer
        holder.content.text = commentList[position].content
        holder.time.text = commentList[position].time
        holder.eraseButton.visibility = View.VISIBLE
        if (commentList[position].id != uid) { // 자기가 쓴 댓글만 지울 수 있도록 댓글 쓴 사람의 uid와 현재 유저의 uid 비교
            holder.eraseButton.visibility = View.GONE
        }
        holder.eraseButton.setOnClickListener {
            commentDeleteAlertDlg(commentList[position].commentID, holder.eraseButton.context)
        }
    }

    private fun commentDeleteAlertDlg(commentID : String, context : Context) { // 댓글을 삭제할지 묻는 다이얼로그
        val builder = AlertDialog.Builder(context)
        builder.setMessage("댓글을 삭제하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                var commentNum = 0
                database.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        commentNum = snapshot.child("comment").childrenCount.toInt()
                    }
                    override fun onCancelled(error: DatabaseError) {

                    }
                })
                database.child("comment").child(commentID).removeValue().addOnCompleteListener {
                    database.child("commentNum").setValue(commentNum.toString())
                }
            }
            .setNegativeButton("취소") { dlg, _ ->
                dlg.dismiss()
            }
        val dlg = builder.create()
        dlg.show()
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

}