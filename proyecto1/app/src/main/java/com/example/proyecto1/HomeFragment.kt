package com.example.proyecto1

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireActivity().getSharedPreferences("AUTH_PREFS", Context.MODE_PRIVATE)

        val tvEmail = view.findViewById<TextView>(R.id.tvEmail)
        val savedUsername = prefs.getString("USER_NAME", "Usuario")
        val email = activity?.intent?.getStringExtra("USER_EMAIL") ?: savedUsername
        tvEmail.text = email

        val tvPhrase = view.findViewById<TextView>(R.id.tvPhrase)
        val btnNextWord = view.findViewById<Button>(R.id.btnNextWord)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

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

        // Lógica de cerrar sesión
        btnLogout.setOnClickListener {
            prefs.edit().clear().apply()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }
}