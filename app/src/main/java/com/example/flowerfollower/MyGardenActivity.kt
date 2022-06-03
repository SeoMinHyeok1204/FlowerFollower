package com.example.flowerfollower

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flowerfollower.databinding.ActivityMyGardenBinding
import com.google.firebase.database.*

class MyGardenActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMyGardenBinding
    private lateinit var uid : String
    private lateinit var database : DatabaseReference
    private lateinit var array : ArrayList<gardenClass>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyGardenBinding.inflate(layoutInflater)
        uid = intent.getStringExtra("uid")!!
        setContentView(binding.root)

        init()
    }

    private fun init() {
        database = FirebaseDatabase.getInstance().getReference("User").child(uid).child("Garden") // 파이어베이스 연결
        setRecyclerView()
    }

    private fun setRecyclerView() { // 사용자가 마이 가든에 올린 것들 읽어오기
        array = ArrayList()
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                array.clear()
                for (data in snapshot.children) {
                    val flowerName = data.child("flowerName").value as String
                    val plantDate = data.child("plantDate").value as String
                    val epoch = data.child("epoch").value as String
                    val imageUrl = data.child("imageUrl").value as String
                    val imageDate = data.child("imageDate").value as String
                    val latitude = data.child("latitude").value as String
                    val longitude = data.child("longitude").value as String

                    val item = gardenClass(flowerName, plantDate, epoch, imageUrl, imageDate, latitude, longitude)
                    array.add(item)
                }
                if(array.size > 1) { // 최근게 위로 오게 정렬
                    array.sortWith(Comparator { p0, p1 -> p0!!.epoch!!.toLong().compareTo(p1!!.epoch!!.toLong()) * -1})
                }
                binding.MyGardenRecyclerView.adapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MyGardenActivity, "오류가 발생했습니다. 잠시 후 다시 시도해 주세요", Toast.LENGTH_SHORT).show()
            }
        })

        binding.MyGardenRecyclerView.layoutManager = LinearLayoutManager(null)
        binding.MyGardenRecyclerView.setHasFixedSize(true)
        binding.MyGardenRecyclerView.adapter = MyGardenAdapter(array)
    }
}