package com.example.memori2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity

class MenuMemoramaActivity : AppCompatActivity() {
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
        setContentView(R.layout.menu_memorama_activity)

        // Referencias a los botones del XML
        val btnSolitario = findViewById<ImageButton>(R.id.btn_solitario)
        val btnMultijugador = findViewById<ImageButton>(R.id.btn_multijugador)
        val btnOnline = findViewById<ImageButton>(R.id.btn_online)
        val btnRegresar = findViewById<ImageButton>(R.id.btn_atras)
        var radioGroupNiveles = findViewById<RadioGroup>(R.id.radioGroup_niveles)



        btnSolitario.setOnClickListener {
            val intent = Intent(this, MemoryGameActivity::class.java)
            val nivel = obtenerNivelSeleccionado(radioGroupNiveles)
            intent.putExtra("nivel", nivel)
            intent.putExtra("mode", 1)
            startActivity(intent)
        }

        btnMultijugador.setOnClickListener {
            val intent = Intent(this, MemoryGameActivity::class.java)
            val nivel = obtenerNivelSeleccionado(radioGroupNiveles)
            intent.putExtra("nivel", nivel)
            intent.putExtra("mode", 2)
            startActivity(intent)
        }

        btnOnline.setOnClickListener {
            val intent = Intent(this, MenuConnectionActivity::class.java)
            val nivel = obtenerNivelSeleccionado(radioGroupNiveles)
            intent.putExtra("nivel", nivel)
            startActivity(intent)
        }

        btnRegresar.setOnClickListener {
            finish()
        }

    }


    fun obtenerNivelSeleccionado(radioGroupNiveles: RadioGroup): Int {
        val idSeleccionado = radioGroupNiveles.checkedRadioButtonId
        val nivel = if (idSeleccionado == R.id.radio_nivel2) {
             2
        } else {
             1
        }
        return nivel
    }
}