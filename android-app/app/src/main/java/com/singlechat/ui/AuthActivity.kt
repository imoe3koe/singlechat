package com.singlechat.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.singlechat.MainActivity
import com.singlechat.R
import java.util.UUID

class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etGroupToken = findViewById<EditText>(R.id.etGroupToken)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        val sharedPreferences = getSharedPreferences("SingleChatPrefs", Context.MODE_PRIVATE)

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            var token = etGroupToken.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (token.isEmpty()) {
                // Jika token kosong, buat token kelompok baru otomatis (Status Ketua)
                token = "SC-" + UUID.randomUUID().toString().take(6).uppercase()
                Toast.makeText(this, "Kelompok Baru Dibuat! Token Anda: $token", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Mendaftar ke kelompok: $token", Toast.LENGTH_SHORT).show()
            }

            // SIMPAN SESI KE SharedPreferences AGAR TERHUBUNG DENGAN ChatFragment
            val dummyUserId = UUID.randomUUID().toString() // Nanti bisa disesuaikan dengan UUID Auth Supabase
            sharedPreferences.edit().apply {
                putString("GROUP_TOKEN", token)
                putString("USER_EMAIL", email)
                putString("USER_ID", dummyUserId)
                apply()
            }

            // Pindah ke MainActivity setelah daftar
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val token = etGroupToken.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                if (token.isEmpty()) {
                    Toast.makeText(this, "Masukkan token kelompok untuk masuk", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // SIMPAN SESI SAAT LOGIN
                val dummyUserId = UUID.randomUUID().toString()
                sharedPreferences.edit().apply {
                    putString("GROUP_TOKEN", token)
                    putString("USER_EMAIL", email)
                    putString("USER_ID", dummyUserId)
                    apply()
                }

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Isi email dan password untuk masuk", Toast.LENGTH_SHORT).show()
            }
        }
    }
}