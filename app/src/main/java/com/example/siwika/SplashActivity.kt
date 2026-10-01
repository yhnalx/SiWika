package com.example.siwika

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.siwika.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Animate logo
        binding.ivLogo.animate().alpha(1f).setDuration(1000).withEndAction {
            // Animate text logo after logo animation
            binding.ivTextLogo.animate().alpha(1f).setDuration(800).withEndAction {
                // Navigate to MainActivity after a delay
                Handler(Looper.getMainLooper()).postDelayed({
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }, 400)
            }
        }
    }
}