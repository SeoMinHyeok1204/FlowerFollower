package com.example.flowerfollower

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CommentAdapter(val commentList : ArrayList<Comment>) : RecyclerView.Adapter<CommentAdapter.CustomViewHolder>() {

    inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val who: TextView = itemView.findViewById(R.id.comment_who)
        val content : TextView = itemView.findViewById(R.id.comment_content)
        val time : TextView = itemView.findViewById(R.id.comment_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comment_recyclerview_item, parent, false)
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.who.text = commentList[position].writer
        holder.content.text = commentList[position].content
        holder.time.text = commentList[position].time
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

}