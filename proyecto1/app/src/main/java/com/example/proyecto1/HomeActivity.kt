package com.example.proyecto1

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val email = intent.getStringExtra("USER_EMAIL")
        tvEmail.text = email

        val tvPhrase = findViewById<TextView>(R.id.tvPhrase)
        val btnNextWord = findViewById<Button>(R.id.btnNextWord)

        val phrase = "El éxito consiste en ir de fracaso en fracaso sin perder el entusiasmo"
        val words = phrase.split(" ")
        var currentIndex = 0

        btnNextWord.setOnClickListener {
            if (currentIndex < words.size) {
                tvPhrase.text = words[currentIndex]
                currentIndex++
            } else {
                tvPhrase.text = "¡Frase terminada!"
                currentIndex = 0
            }
        }
    }
}