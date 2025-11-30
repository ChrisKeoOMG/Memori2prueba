package com.example.memori2

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var musicPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        // Ocultar barra
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        setContentView(R.layout.activity_main)

        val btnPlay = findViewById<ImageButton>(R.id.btn_play)
        val btnTutorial = findViewById<ImageButton>(R.id.btn_tutorial)
        val btnSettings = findViewById<ImageButton>(R.id.btn_settings)
        val btnExit = findViewById<ImageButton>(R.id.btn_exit)
        val btnSocial = findViewById<ImageButton>(R.id.btn_social)

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val musicEnabled = prefs.getBoolean("music_enabled", true)
        val volumeSaved = prefs.getFloat("volume", 1f)

        val i = Intent(this, Music::class.java)
        i.putExtra("track", "menu")
        startService(i)




        btnPlay.setOnClickListener {
            startActivity(Intent(this, GameMenuActivity::class.java))
        }
        btnTutorial.setOnClickListener {
            startActivity(Intent(this, AjustesActivity::class.java))
        }
        btnSettings.setOnClickListener {
            val mensaje = "¡Juega este increíble juego! Descárgalo aquí: https://www.MemoriInglish.mx"

            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, mensaje)
                type = "text/plain"
            }

            startActivity(Intent.createChooser(sendIntent, "Compartir con:"))
        }

        btnSocial.setOnClickListener {
            startActivity(Intent(this, TutorialActivity::class.java))
        }

        btnExit.setOnClickListener {
            finishAffinity()
            onDestroy()
        }
    }



    override fun onPause() {
        super.onPause()
        if (::musicPlayer.isInitialized && musicPlayer.isPlaying) {
            musicPlayer.pause()
        }
    }

    override fun onResume() {
        super.onResume()

        try {
            val prefs = getSharedPreferences("settings", MODE_PRIVATE)
            val musicEnabled = prefs.getBoolean("music_enabled", true)

            if (musicEnabled) {
                musicPlayer.start()
            }
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::musicPlayer.isInitialized) {
            musicPlayer.release()
        }
    }
}
