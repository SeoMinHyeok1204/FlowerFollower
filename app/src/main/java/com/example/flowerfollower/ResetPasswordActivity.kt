package com.example.flowerfollower

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.example.flowerfollower.databinding.ActivityFindPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding : ActivityFindPasswordBinding
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFindPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        binding.apply {
            progressBar3.visibility = View.GONE
            auth = FirebaseAuth.getInstance()

            ResetPasswordButton.setOnClickListener {
                resetPassword()
            }
        }
    }

    private fun resetPassword() {
        val email = binding.targetEmail.text.toString().trim()

        if(email.isEmpty()) {
            binding.targetEmail.error = "이메일을 입력해주세요"
            binding.targetEmail.requestFocus()
            return
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.targetEmail.error = "유효한 이메일 형식이 아닙니다"
            binding.targetEmail.requestFocus()
            return
        }

        binding.progressBar3.visibility = View.VISIBLE

        auth.sendPasswordResetEmail(email).addOnCompleteListener {
            if(it.isSuccessful) {
                Toast.makeText(this, "비밀번호를 변경하기 위한 이메일을 발송했습니다.", Toast.LENGTH_SHORT).show()
                binding.progressBar3.visibility = View.GONE
            }
            else {
                Toast.makeText(this, "오류가 발생했습니다. 다시 시도해주세요", Toast.LENGTH_SHORT).show()
                binding.progressBar3.visibility = View.GONE
            }
        }
    }
}