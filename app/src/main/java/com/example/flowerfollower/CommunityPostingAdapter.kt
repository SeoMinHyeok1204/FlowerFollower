package com.example.flowerfollower

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.Image
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.bumptech.glide.Glide
import com.example.flowerfollower.databinding.CommunityRecyclerviewItemBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import java.io.ByteArrayInputStream
import kotlin.experimental.or

class CommunityPostingAdapter(private val postingList:ArrayList<CommunityPosting>, private val uid:String, private val nickname:String) : Adapter<CommunityPostingAdapter.CustomViewHolder>() {

    inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.community_item_title)
        val contentPreview: TextView = itemView.findViewById(R.id.community_item_preview)
        val time: TextView = itemView.findViewById(R.id.community_item_time)
        val commentNum: TextView = itemView.findViewById(R.id.community_item_commentNum)
        val commentImage: ImageView = itemView.findViewById(R.id.community_item_comment_image)
        val who: TextView = itemView.findViewById(R.id.community_item_nickname)
        val image: ImageView = itemView.findViewById(R.id.community_item_flower)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.community_recyclerview_item, parent, false)
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val title = postingList[position].title.replace('\n', ' ')
        when { // 제목이 너무 길면 잘라서 일부만 보이게하기
            title.length > 30 -> {
                val tmp = title.substring(0..29) + ".."
                holder.title.text = tmp
            }
            else -> holder.title.text = title
        }
        val preView = postingList[position].content.replace('\n',' ')
        when { // 내용이 너무 길면 잘라서 일부만 보이게 하기
            preView.length > 46 -> {
                val tmp = preView.substring(0..45) + ".."
                holder.contentPreview.text = tmp
            }
            else -> holder.contentPreview.text = preView
        }
        holder.time.text = postingList[position].time
        holder.commentNum.text = postingList[position].commentNum
        holder.commentImage.setImageResource(R.drawable.comment)
        holder.who.text = postingList[position].nickname

        holder.image.visibility = View.VISIBLE

        val imageUrl = postingList[position].imageUrl
        if(imageUrl == "0") { // 이미지가 없는 글들은 이미지 뷰가 안보이게 하기
            holder.image.visibility = View.GONE
        }
        else { // 이미지가 있는 글은 파이어베이스 스토리지에서 이미지 가져오기
            Glide.with(holder.image.context).load(imageUrl).into(holder.image)
        }

        holder.itemView.setOnClickListener { // 글 클릭시 글 안으로 이동
            val intent = Intent(holder.itemView.context, InPostingActivity::class.java)
            intent.putExtra("writer", postingList[position].nickname)
            intent.putExtra("time", postingList[position].time)
            intent.putExtra("title", postingList[position].title)
            intent.putExtra("content", postingList[position].content)
            intent.putExtra("postingID", postingList[position].postingID)
            intent.putExtra("imageUrl", postingList[position].imageUrl)
            intent.putExtra("writerUID", postingList[position].uid)
            intent.putExtra("currentUserUID", uid)
            intent.putExtra("currentUserNickname", nickname)
            startActivity(holder.itemView.context, intent, null)
        }
    }

    override fun getItemCount(): Int {
        return postingList.size
    }
}