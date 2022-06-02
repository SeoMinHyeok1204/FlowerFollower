package com.example.flowerfollower

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.flowerfollower.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        auth = FirebaseAuth.getInstance()
        binding.progressBar2.visibility = View.GONE
        binding.Register.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        binding.LoginButton.setOnClickListener {
            closeKeyboard()
            logIn()
        }
        binding.forgetPassword.setOnClickListener {
            val intent = Intent(this, ResetPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    private fun logIn() { // 로그인
        val email = binding.EditEmail.text.toString().trim()
        val password = binding.EditPassword.text.toString().trim()
        // 빈 칸이 아니고 규칙에 맞게 입력 했는지 확인
        if(email.isEmpty()) {
            binding.EditEmail.error = "이메일을 입력해주세요"
            binding.EditEmail.requestFocus()
            return
        }
        else if(password.isEmpty()) {
            binding.EditPassword.error = "비밀번호를 입력해주세요"
            binding.EditPassword.requestFocus()
            return
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.EditEmail.error = "유효한 이메일 형식이 아닙니다"
            binding.EditEmail.requestFocus()
            return
        }
        else if(password.length < 8) {
            binding.EditPassword.error = "비밀번호는 8자리 이상 이어야 합니다"
            binding.EditPassword.requestFocus()
            return
        }

        binding.progressBar2.visibility = View.VISIBLE
        // 규칙에 다 맞으면 로그인
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(it.isSuccessful) { // 파이어베이스에 등록된 이메일, 비밀번호임
                    val user = auth.currentUser
                    if(user!!.isEmailVerified) { // 이메일 인증도 했으면 통과
                        val intent = Intent(this, MainActivity::class.java)
                        binding.progressBar2.visibility = View.GONE
                        finish()
                        startActivity(intent)
                    }
                    else { // 이메일 인증을 안한 계정이면 이메일 발송
                        user.sendEmailVerification()
                        Toast.makeText(this, "계정을 활성화 하기 위한 메일을 발송했습니다. 메일을 확인해 주세요", Toast.LENGTH_SHORT).show()
                        binding.progressBar2.visibility = View.GONE
                    }
                }else { // 파이어베이스에 등록되지 않은 이메일, 비밀번호임
                    Toast.makeText(this, "로그인에 실패했습니다. 다시 확인해 주세요", Toast.LENGTH_SHORT).show()
                    binding.progressBar2.visibility = View.GONE
                }
            }
    }

    private fun closeKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm : InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}