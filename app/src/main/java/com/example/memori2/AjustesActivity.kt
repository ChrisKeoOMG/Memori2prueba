package com.example.memori2

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.view.View

class AjustesActivity : AppCompatActivity() {

    private lateinit var switchMusic: Switch
    private lateinit var seekbarVolume: SeekBar
    private lateinit var btnEnglish: Button
    private lateinit var btnSpanish: Button
    private lateinit var btnRegresar: ImageButton

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ajustes)

        // Ocultar barra superior y activar pantalla completa
        supportActionBar?.hide()
        window.decorView.apply {
            systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }

        // Inicializar vistas
        switchMusic = findViewById(R.id.switch_music)
        seekbarVolume = findViewById(R.id.seekbar_volume)
        btnEnglish = findViewById(R.id.btnEnglish)
        btnSpanish = findViewById(R.id.btnSpanish)
        btnRegresar = findViewById(R.id.btnRegresar)


       // mediaPlayer = MediaPlayer.create(this, R.raw.musicafondo)
        mediaPlayer?.isLooping = true


        switchMusic.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                mediaPlayer?.start()
                Toast.makeText(this, "Música activada", Toast.LENGTH_SHORT).show()
            } else {
                mediaPlayer?.pause()
                Toast.makeText(this, "Música pausada", Toast.LENGTH_SHORT).show()
            }
        }

        seekbarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val volume = progress / 100f
                mediaPlayer?.setVolume(volume, volume)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnEnglish.setOnClickListener {
            Toast.makeText(this, "Idioma cambiado a Inglés (demo)", Toast.LENGTH_SHORT).show()
        }

        btnSpanish.setOnClickListener {
            Toast.makeText(this, "Idioma cambiado a Español (demo)", Toast.LENGTH_SHORT).show()
        }

        btnRegresar.setOnClickListener {
            mediaPlayer?.stop()
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
