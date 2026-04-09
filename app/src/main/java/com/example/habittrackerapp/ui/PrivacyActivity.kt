package com.example.habittrackerapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.habittrackerapp.databinding.ActivityPrivacyBinding

class PrivacyActivity : AppCompatActivity() {
    // Creamos el binding
    private lateinit var binding: ActivityPrivacyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
    }
}
