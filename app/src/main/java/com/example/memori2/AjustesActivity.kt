package com.example.memori2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.view.View

class AjustesActivity : AppCompatActivity() {

    private lateinit var switchMusic: Switch
    private lateinit var seekbarVolume: SeekBar
    private lateinit var btnAvisoPadres: ImageButton

    private lateinit var btnRegresar: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ajustes)

        supportActionBar?.hide()
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        switchMusic = findViewById(R.id.switch_music)
        seekbarVolume = findViewById(R.id.seekbar_volume)
        btnAvisoPadres = findViewById(R.id.btnAvisoPadres)
        btnRegresar = findViewById(R.id.btnRegresar)
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        val musicEnabled = prefs.getBoolean("music_enabled", true)
        val volumeSaved = prefs.getFloat("volume", 1f)

        switchMusic.isChecked = musicEnabled
        seekbarVolume.progress = (volumeSaved * 100).toInt()

        switchMusic.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val i = Intent(this, Music::class.java)
                i.putExtra("track", "menu")
                startService(i)

                prefs.edit().putBoolean("music_enabled", true).apply()

            } else {
                stopService(Intent(this, Music::class.java))
                prefs.edit().putBoolean("music_enabled", false).apply()
            }
        }

        seekbarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val volume = progress / 100f
                editor.putFloat("volume", volume).apply()

                Music.setVolume(volume)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnAvisoPadres.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Aviso para padres")
                .setMessage(
                    "Esta aplicación es segura para niños.\n\n" +
                            "• No contiene anuncios.\n" +
                            "• No recopila datos personales.\n" +
                            "• Diseñada solo con fines educativos.\n\n" +
                            "Gracias por confiar en nosotros "
                )
                .setPositiveButton("Entendido") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
        btnRegresar.setOnClickListener {
            finish()
        }
    }
}
