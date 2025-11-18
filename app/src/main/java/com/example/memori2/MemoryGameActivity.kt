package com.example.memori2

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.memory_game_activity)

        val nivelSeleccionado = intent.getIntExtra("nivel", 1)
        val mode = intent.getIntExtra("mode", 1)

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