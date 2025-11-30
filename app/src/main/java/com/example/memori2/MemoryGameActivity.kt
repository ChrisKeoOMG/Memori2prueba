package com.example.memori2

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import com.example.memori2.adapter.GameAdapter

class MemoryGameActivity : AppCompatActivity() {


    private lateinit var game: MemoryGame
    private lateinit var adapter: GameAdapter

    private lateinit var txtScore1: TextView

    private lateinit var txtScore2: TextView
    private lateinit var txtTurn: TextView

    private var lastFlippedIndex: Int? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.memory_game_activity)
        supportActionBar?.hide()
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        val nivelSeleccionado = intent.getIntExtra("nivel", 1)
        val mode = intent.getIntExtra("mode", 1)

        val intent = Intent(this, Music::class.java)
        intent.putExtra("track", "laberinto")
        startService(intent)

        txtScore1 = findViewById(R.id.txtPlayer1)
        txtScore2 = findViewById(R.id.txtPlayer2)
        txtTurn = findViewById(R.id.txtTurn)




        game = MemoryGame(mode, nivelSeleccionado)

        if (mode == 1) { // Modo Solitario
            txtScore2.visibility = View.GONE
            txtTurn.visibility = View.GONE
            txtScore1.text = "Pares: 0"
        }

        val rv = findViewById<RecyclerView>(R.id.recyclerCards)

        val spanCount = if (nivelSeleccionado == 2) 7 else 4

        rv.layoutManager = GridLayoutManager(this, spanCount)

        adapter = GameAdapter(this, game.cards) { position ->
            onCardClicked(position)
        }

        rv.adapter = adapter
        updateUI()

        rv.adapter = adapter
        updateUI()

        val btnMenu: ImageButton = findViewById(R.id.btnMenu)

        btnMenu.setOnClickListener {
            Music.stopMusic()

            val prefs = getSharedPreferences("settings", MODE_PRIVATE)
            if (prefs.getBoolean("music_enabled", true)) {
                val i = Intent(this, Music::class.java)
                i.putExtra("track", "menu")
                startService(i)
            }

            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            startActivity(intent)
            finish()
        }

    }

    private fun onCardClicked(position: Int) {
        val isMatchAttempt = lastFlippedIndex != null

        val result = game.flipCard(position)
        adapter.notifyItemChanged(position)

        if (!result && isMatchAttempt) {
            val first = lastFlippedIndex!!
            Handler(Looper.getMainLooper()).postDelayed({
                game.flipBack(first, position)
                adapter.notifyItemChanged(first)
                adapter.notifyItemChanged(position)
                updateUI()
            }, 700)
        }

        lastFlippedIndex = if (isMatchAttempt) null else position

        updateUI()
    }

    private fun updateUI() {
        if (game.modo == 1) { // Modo Solitario
            txtScore1.text = "Pares: ${game.scores[0]}"
        } else { // Modo Multijugador
            txtScore1.text = "P1: ${game.scores[0]}"
            txtScore2.text = "P2: ${game.scores[1]}"
            txtTurn.text = "Turno: Jugador ${game.currentPlayer + 1}"
        }
    }


}