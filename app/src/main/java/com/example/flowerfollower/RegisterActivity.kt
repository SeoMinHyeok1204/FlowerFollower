package com.example.flowerfollower

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.example.flowerfollower.databinding.ActivityRegisterBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        init()
    }

    private fun init() {
        binding.progressBar.visibility = View.GONE
        binding.AssignButton.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val nickName = binding.EnterNickName.text.toString().trim()
        val email = binding.EnterEmail.text.toString().trim()
        val password = binding.EnterPassword.text.toString().trim()
        // 빈 칸이 아니고 규칙에 맞는지 확인
        if(nickName.isEmpty()) {
            binding.EnterNickName.error = "닉네임을 입력해주세요"
            binding.EnterNickName.requestFocus()
            return
        }
        else if(email.isEmpty()) {
            binding.EnterEmail.error = "이메일을 입력해주세요"
            binding.EnterEmail.requestFocus()
            return
        }
        else if(password.isEmpty()) {
            binding.EnterPassword.error = "비밀번호를 입력해주세요"
            binding.EnterPassword.requestFocus()
            return
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.EnterEmail.error = "유효한 이메일 형식이 아닙니다"
            binding.EnterEmail.requestFocus()
            return
        }
        else if(password.length < 8) {
            binding.EnterPassword.error = "비밀번호는 8자리 이상으로 입력해주세요"
            binding.EnterPassword.requestFocus()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        // 규칙에 맞으면 회원가입 진행
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(it.isSuccessful) { // 회원가입 성공
                    val user = UserInfo(nickName, email, password)
                    FirebaseDatabase.getInstance().getReference("User")
                        .child(FirebaseAuth.getInstance().currentUser!!.uid)
                        .setValue(user).addOnCompleteListener(object : OnCompleteListener<Void> {
                            override fun onComplete(task: Task<Void>) {
                                if(task.isSuccessful) { // 사용자 정보 파이어베이스에 업로드 성공
                                    Toast.makeText(this@RegisterActivity, "회원가입 성공", Toast.LENGTH_SHORT).show()
                                    binding.progressBar.visibility = View.GONE
                                    finish()
                                }
                                else { // 사용자 정보 파이어베이스에 업로드 실패
                                    Toast.makeText(this@RegisterActivity, "회원가입에 실패했습니다. 다시 시도해주세요", Toast.LENGTH_SHORT).show()
                                    binding.progressBar.visibility = View.GONE
                                }
                            }
                        })
                }
                else { // 회원가입 실패
                    Toast.makeText(this@RegisterActivity, "회원가입에 실패했습니다. 다시 시도해주세요", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                }
            }
    }
}