package com.example.flowerfollower

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.example.flowerfollower.databinding.ActivityMyInfo2Binding
import com.google.firebase.database.DatabaseReference
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MyInfo2Activity : AppCompatActivity() {

    private lateinit var binding: ActivityMyInfo2Binding
    private lateinit var rdb : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyInfo2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun logout() {
        val intent = Intent(this,LoginActivity::class.java)
        startActivity(intent)
    }

    private fun init(){

        val uid = intent.getStringExtra("uid")!!
        val usermail = intent.getStringExtra("usermail")!!
        val nickname = intent.getStringExtra("nickname")

        //rdb = Firebase.database.getReference("User/$uid")
        rdb = Firebase.database.getReference("User")

        binding.pEmail.text = usermail
        binding.pNickname.text = nickname

        val newNickname = binding.AfterNickname
        val changebtn = binding.changebtn
        newNickname.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                val str = p0.toString()
                changebtn.isEnabled = str.isNotBlank()
            }

        })
        binding.changebtn.setOnClickListener {
            val builder = AlertDialog.Builder(this)
                .setTitle("닉네임 변경")
                .setMessage(nickname + " 에서 " + newNickname.text + " 로 닉네임을 변경하시겠습니까?")
                .setPositiveButton("예", DialogInterface.OnClickListener { dialogInterface, i ->
                    rdb.child(uid).child("nickName").setValue(newNickname.text.toString())
                    Toast.makeText(this, "변경되었습니다.", Toast.LENGTH_SHORT).show()
                    logout()
                })
                .setNegativeButton("아니오", DialogInterface.OnClickListener { dialogInterface, i ->
                    Toast.makeText(this, "취소되었습니다.", Toast.LENGTH_SHORT).show()
                })
                .show()
        }
        binding.logoutbtn.setOnClickListener {
            val builder = AlertDialog.Builder(this)
                .setTitle("로그아웃")
                .setMessage("로그아웃 하시겠습니까?")
                .setPositiveButton("예", DialogInterface.OnClickListener { dialogInterface, i ->
                    Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
                    logout()
                })
                .setNegativeButton("아니오", DialogInterface.OnClickListener { dialogInterface, i ->
                    Toast.makeText(this, "취소되었습니다.", Toast.LENGTH_SHORT).show()
                })
                .show()
        }

    }


}