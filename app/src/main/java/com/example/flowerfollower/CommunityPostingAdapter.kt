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

//class CommunityPostingAdapter(options : FirebaseRecyclerOptions<CommunityPosting>)
//    : FirebaseRecyclerAdapter<CommunityPosting, CommunityPostingAdapter.CustomViewHolder>(options) {
//
//    inner class CustomViewHolder(val binding : CommunityRecyclerviewItemBinding) : RecyclerView.ViewHolder(binding.root) {
//
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
//        val view = CommunityRecyclerviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return CustomViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: CustomViewHolder, position: Int, model: CommunityPosting) {
//        holder.binding.apply {
//            communityItemTitle.text = model.title
//            val preView = model.content.replace('\n',' ')
//            when {
//                preView.length > 46 -> {
//                    val tmp = preView.substring(0..45) + ".."
//                    communityItemPreview.text = tmp
//                }
//                else -> communityItemPreview.text = preView
//            }
//            communityItemTime.text = model.time
//            communityItemCommentNum.text = model.commentNum
//            communityItemCommentImage.setImageResource(R.drawable.comment)
//            communityItemNickname.text = model.nickname
//            val binaryImage = model.image
//            if(binaryImage == "0") {
////                communityItemFlower.visibility = View.GONE
//            }
//            else {
//                val b = binaryStringToByteArray(model.image)
//                val stream = ByteArrayInputStream(b)
//                val image = Drawable.createFromStream(stream, "image")
//                communityItemFlower.setImageDrawable(image)
//            }
//        }
//    }
//
//    private fun binaryStringToByteArray(s : String) : ByteArray {
//        val count = s.length / 8
//        val b = ByteArray(count)
//        for(i in 1..count) {
//            val t = s.substring((i-1)*8, i*8)
//            b[i-1] = binaryStringToByte(t)
//        }
//        return b
//    }
//
//    private fun binaryStringToByte(s : String) : Byte {
//        var ret: Byte?
//        var total : Byte = 0
//        for (i in 0..7) {
//            ret = if(s[7-i]=='1') (1.shl(i)).toByte() else 0
//            total = ret.or(total)
//        }
//        return total
//    }
//}

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
        when {
            title.length > 30 -> {
                val tmp = title.substring(0..29) + ".."
                holder.title.text = tmp
            }
            else -> holder.title.text = title
        }
        val preView = postingList[position].content.replace('\n',' ')
        when {
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

//        val params = holder.commentNum.layoutParams as ConstraintLayout.LayoutParams
//        params.topMargin = 0
//        params.leftMargin = 0
//        params.bottomMargin = 8
//        params.rightMargin = 260
//        holder.commentNum.layoutParams = params
        holder.image.visibility = View.VISIBLE

        val imageUrl = postingList[position].imageUrl
        if(imageUrl == "0") {
            holder.image.visibility = View.GONE
//            params.topMargin = 0
//            params.leftMargin = 0
//            params.bottomMargin = 8
//            params.rightMargin = 48
//            holder.commentNum.layoutParams = params
        }
        else {
            Glide.with(holder.image.context).load(imageUrl).into(holder.image)
        }

        holder.itemView.setOnClickListener {
//            Log.d("####", params.rightMargin.toString())
            val intent = Intent(holder.itemView.context, InPostingActivity::class.java)
            intent.putExtra("writer", postingList[position].nickname)
            intent.putExtra("time", postingList[position].time)
            intent.putExtra("title", postingList[position].title)
            intent.putExtra("content", postingList[position].content)
            intent.putExtra("postingID", postingList[position].postingID)
            intent.putExtra("imageUrl", postingList[position].imageUrl)
            intent.putExtra("currentUserUID", uid)
            intent.putExtra("currentUserNickname", nickname)
            startActivity(holder.itemView.context, intent, null)
        }
    }

    override fun getItemCount(): Int {
        return postingList.size
    }
}