package com.example.memori2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class GameMenuActivity : AppCompatActivity() {
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
        setContentView(R.layout.game_menu_activity)

        // Referencias a los botones del XML
        val btnJugarMemorama = findViewById<ImageButton>(R.id.btn_memorama)
        val btnJugarLaberinto = findViewById<ImageButton>(R.id.btn_laberinto)
        val btnRegresar = findViewById<ImageButton>(R.id.btn_atras)


        btnJugarMemorama.setOnClickListener {
            val intent = Intent(this, MenuMemoramaActivity::class.java)
            startActivity(intent)
        }

        btnJugarLaberinto.setOnClickListener {
            val intent = Intent(this, SensorActivity::class.java)
            startActivity(intent)
        }

        btnRegresar.setOnClickListener {
            val intent = Intent(this, GameMenuActivity::class.java)
            startActivity(intent)
        }


    }
}