package com.example.memori2

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder

class Music : Service() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        val trackName = intent?.getStringExtra("track") ?: "menu"

        val trackRes = when (trackName) {
            "laberinto" -> R.raw.musicjuego
            "menu" -> R.raw.musicamenu
            else -> R.raw.musicamenu
        }

        // Crear el reproductor
        mediaPlayer = MediaPlayer.create(this, trackRes)
        mediaPlayer?.isLooping = true

        // Cargar volumen guardado
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val vol = prefs.getFloat("volume", 1f)
        mediaPlayer?.setVolume(vol, vol)

        // Si la musica está desactivada, no reproducir
        val enabled = prefs.getBoolean("music_enabled", true)
        if (enabled) {
            mediaPlayer?.start()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private var instance: Music? = null

        fun setVolume(volume: Float) {
            instance?.mediaPlayer?.setVolume(volume, volume)
        }

        fun stopMusic() {
            instance?.mediaPlayer?.stop()
        }
    }
}
