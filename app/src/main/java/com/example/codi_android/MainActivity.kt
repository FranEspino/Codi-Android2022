package com.example.codi_android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btn_login = findViewById<Button>(R.id.btn_login)
        val btn_register = findViewById<Button>(R.id.btn_register)
        btn_login.setOnClickListener {
            val intent = Intent(applicationContext, SignInActivity::class.java)
            startActivity(intent)
        }

        btn_register.setOnClickListener{
            val intent = Intent(applicationContext, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}