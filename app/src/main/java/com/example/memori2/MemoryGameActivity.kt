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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.memori2.adapter.GameAdapter
import com.example.memori2.network.GameClient
import com.example.memori2.network.GameHost
import com.example.memori2.network.GameState

class MemoryGameActivity : AppCompatActivity() {
    private lateinit var game: MemoryGame
    private lateinit var adapter: GameAdapter
    private lateinit var rv: RecyclerView

    private lateinit var txtScore1: TextView
    private lateinit var txtScore2: TextView
    private lateinit var txtTurn: TextView

    private var lastFlippedIndex: Int? = null

    private var mode: Int = 1
    private var isHost = false
    private var hostIp = ""

    private var gameHost: GameHost? = null
    private var gameClient: GameClient? = null

    private var isLayoutInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.memory_game_activity)
        supportActionBar?.hide()

        val nivelSeleccionado = intent.getIntExtra("nivel", 1)
        mode = intent.getIntExtra("mode", 1)

        if (mode == 3) {
            isHost = intent.getBooleanExtra("isHost", false)
            if (!isHost) hostIp = intent.getStringExtra("targetIp") ?: ""
        }

        // Música
        startService(Intent(this, Music::class.java).apply {
            putExtra("track", "laberinto")
        })

        txtScore1 = findViewById(R.id.txtPlayer1)
        txtScore2 = findViewById(R.id.txtPlayer2)
        txtTurn = findViewById(R.id.txtTurn)
        rv = findViewById(R.id.recyclerCards)

        game = MemoryGame(mode, nivelSeleccionado)

        if (mode == 1) {
            txtScore2.visibility = View.GONE
            txtTurn.visibility = View.GONE
            isLayoutInitialized = true;
        }

        if (mode == 3) {
            if (isHost) {
                gameHost = GameHost(game, this::onNetworkStateReceived)
                gameHost?.startHosting()
                gameHost?.onDisconnect = {
                    runOnUiThread {
                        Toast.makeText(this, "El Jugador 2 se desconectó", Toast.LENGTH_LONG).show()
                        closeEverything()
                        finish()
                    }
                }
            } else {
                gameClient = GameClient(hostIp, this::onNetworkStateReceived)

                gameClient?.onConnectionFailed = { errorMessage ->
                    runOnUiThread {
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                        finish()
                    }
                }

                gameClient?.onDisconnect = {
                    runOnUiThread {
                        Toast.makeText(this, "El Jugador 1 se desconectó", Toast.LENGTH_LONG).show()
                        closeEverything()
                        finish()
                    }
                }


                gameClient?.startConnecting()

                Toast.makeText(this, "Conectando a la partida...", Toast.LENGTH_SHORT).show()

            }
        }

        adapter = GameAdapter(this, mutableListOf()) { pos -> onCardClicked(pos) }
        rv.adapter = adapter

        if (mode != 3 || isHost) {
            val columns = if (nivelSeleccionado == 2) 7 else 4
            rv.layoutManager = GridLayoutManager(this, columns)
            isLayoutInitialized = true
        }

        adapter.updateFromRealList(game.cards)

        findViewById<ImageButton>(R.id.btnMenu).setOnClickListener {
            closeEverything()
            finish()
        }

        updateUI()
    }

    override fun onDestroy() {
        closeEverything()
        super.onDestroy()
    }

    private fun closeEverything() {
        try { Music.stopMusic() } catch (_: Exception) {}

        if (mode == 3) {
            if (isHost) gameHost?.stopHosting()
            else gameClient?.disconnect()
        }
    }

    private fun onCardClicked(position: Int) {
        if (mode == 3) {
            val esMiTurno =
                (isHost && game.currentPlayer == 0) ||
                        (!isHost && game.currentPlayer == 1)

            if (!esMiTurno) {
                Toast.makeText(this, "No es tu turno", Toast.LENGTH_SHORT).show()
                return
            }

            val json = """{"type":"FLIP","position":$position}"""

            if (isHost) gameHost?.sendActionToSelf(json)
            else gameClient?.sendAction(json)

            return
        }

        // — Modo local —
        val wasSecond = lastFlippedIndex != null
        val match = game.flipCard(position)

        adapter.updateFromRealList(game.cards)

        if (!match && wasSecond) {
            val first = lastFlippedIndex!!
            rv.isEnabled = false

            Handler(Looper.getMainLooper()).postDelayed({
                game.flipBack(first, position)
                adapter.updateFromRealList(game.cards)
                updateUI()
                rv.isEnabled = true
            }, 700)
        }

        lastFlippedIndex = if (wasSecond) null else position
        updateUI()

        if (game.cards.all { it.isMatched }) {
            val winner = if (game.scores[0] > game.scores[1]) "Jugador 1"
            else if (game.scores[1] > game.scores[0]) "Jugador 2"
            else "Empate"

            Toast.makeText(this, "Juego terminado: $winner", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateUI() {
        if (mode == 1) {
            txtScore1.text = "Pares: ${game.scores[0]}"
        } else {
            txtScore1.text = "Jugador 1: ${game.scores[0]}"
            txtScore2.text = "Jugador 2: ${game.scores[1]}"

            txtTurn.text = "Turno: Jugador ${game.currentPlayer + 1}"
        }
    }

    private fun onNetworkStateReceived(state: GameState) {
        runOnUiThread {
            if (!isLayoutInitialized) {
                val columns = if (state.level == 2) 7 else 4
                rv.layoutManager = GridLayoutManager(this, columns)
                isLayoutInitialized = true
            }
            adapter.updateCards(state.cards.map { it.copy() })
            game.currentPlayer = state.currentPlayer
            game.scores[0] = state.scores[0]
            game.scores[1] = state.scores[1]
            updateUI()

            if (state.isGameOver) {
                val msg = when (state.winner) {
                    0 -> { // Ganador es el Host/Jugador 1
                        if (isHost) "¡Felicidades, ganaste!" else "El Jugador 1ha ganado."
                    }
                    1 -> { // Ganador es el Cliente/Jugador 2
                        if (!isHost) "¡Felicidades, ganaste!" else "El Jugador 2 ha ganado."
                    }
                    -1 -> "¡Es un empate!"
                    else -> "Fin del juego"
                }

                AlertDialog.Builder(this)
                    .setTitle("Fin del juego")
                    .setMessage(msg)
                    .setCancelable(false)
                    .setPositiveButton("Volver al menu") { dialog, _ ->
                        dialog.dismiss()
                        closeEverything()
                        finish()
                    }
                    .show()
            }
        }
    }
}

