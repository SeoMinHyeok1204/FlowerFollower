package com.example.flowerfollower

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.flowerfollower.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var user : FirebaseUser
    private lateinit var reference: DatabaseReference
    private lateinit var uid : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        user = FirebaseAuth.getInstance().currentUser!!
        reference = FirebaseDatabase.getInstance().getReference("User")
        uid = user.uid

        reference.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profile = snapshot.getValue(UserInfo::class.java)

                if(profile != null) {
                    val nickname = profile.nickName
                    val email = profile.email
                    val password = profile.password

                    binding.apply {
                        tvNickname.text = nickname
                        tvEmail.text = email
                        tvPassword.text = password
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "오류가 발생했습니다. 다시 시도해 주세요", Toast.LENGTH_SHORT).show()
            }
        })
    }
}