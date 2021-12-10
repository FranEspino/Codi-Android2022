package com.example.codi_android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class SignInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)
        val login = findViewById<Button>(R.id.btn_login)
        login.setOnClickListener {
            val intent = Intent(this, PermissionFineLocationActivity::class.java)
            startActivity(intent)
        }
    }





}