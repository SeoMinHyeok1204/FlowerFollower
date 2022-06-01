package com.example.flowerfollower

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MyGardenAdapter(private val gardenList:ArrayList<gardenClass>) : RecyclerView.Adapter<MyGardenAdapter.CustomViewHolder>() {


    inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val flowerName : TextView = itemView.findViewById(R.id.MyGardenFlowerName)
        val plantDate : TextView = itemView.findViewById(R.id.MyGardenDate)
        val flowerImage : ImageView = itemView.findViewById(R.id.MyGardenImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mygarden_recyclerview_item, parent, false)
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.flowerName.text = gardenList[position].flowerName
        holder.plantDate.text = gardenList[position].plantDate
        Glide.with(holder.flowerImage.context).load(gardenList[position].imageUrl).into(holder.flowerImage)

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, InGardenActivity::class.java)
            intent.putExtra("flowerName", gardenList[position].flowerName)
            intent.putExtra("plantDate", gardenList[position].plantDate)
            intent.putExtra("imageDate", gardenList[position].imageDate)
            intent.putExtra("latitude", gardenList[position].latitude)
            intent.putExtra("longitude", gardenList[position].longitude)
            intent.putExtra("imageUrl", gardenList[position].imageUrl)
            startActivity(holder.itemView.context, intent, null)
        }
    }

    override fun getItemCount(): Int {
        return gardenList.size
    }
}