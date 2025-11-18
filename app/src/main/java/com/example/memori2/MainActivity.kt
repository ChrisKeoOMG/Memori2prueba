package com.example.memori2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()  // Oculta la barra

        //ocultar barraa
        window.decorView.apply {
            systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }
        setContentView(R.layout.activity_main)

        // Referencias a los botones del XML
        val btnPlay = findViewById<ImageButton>(R.id.btn_play)
        val btnTutorial = findViewById<ImageButton>(R.id.btn_tutorial)
        val btnSettings = findViewById<ImageButton>(R.id.btn_settings)
        val btnExit = findViewById<ImageButton>(R.id.btn_exit)
        val btnSocial = findViewById<ImageButton>(R.id.btn_social)


        btnPlay.setOnClickListener {
            val intent = Intent(this, GameMenuActivity::class.java)
            startActivity(intent)
        }


        btnTutorial.setOnClickListener {
            val intent = Intent(this, AjustesActivity::class.java)
            startActivity(intent)
        }


        btnSettings.setOnClickListener {
            Toast.makeText(this, "Boton de compartir juego", Toast.LENGTH_SHORT).show()
        }


        btnSocial.setOnClickListener {
            Toast.makeText(this, "Tutorial Proximamente", Toast.LENGTH_SHORT).show()
        }

        // Botón "Salir"
        btnExit.setOnClickListener {
            finishAffinity()
        }
    }
}
