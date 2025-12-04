package com.example.memori2.network

import com.example.memori2.MemoryGame
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import java.io.*
import java.net.ServerSocket
import java.net.Socket

class GameHost(
    private val game: MemoryGame,
    private val onStateChanged: (GameState) -> Unit // Callback para actualizar UI del Host
) {
    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private val PORT = 5003 // Puerto arbitrario
    private val gson = Gson()

    // CoroutineScope para operaciones de red y delays sin bloquear la UI
    private val scope = CoroutineScope(Dispatchers.IO)

    // Para saber qué carta voltear de regreso
    private var firstFlippedIndex: Int? = null

    fun startHosting() {
        scope.launch {
            try {
                // 1. Abrir puerto
                serverSocket = ServerSocket(PORT)
                println("Servidor iniciado en puerto $PORT. Esperando cliente...")

                // 2. Esperar conexión (Bloqueante)
                clientSocket = serverSocket?.accept()

                // 3. Al conectarse, enviar el estado inicial
                broadcastState()

                // 4. Escuchar mensajes del cliente
                listenToClient()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    var onDisconnect: (() -> Unit)? = null

    private fun listenToClient() {
        clientSocket?.let { socket ->
            try {
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                var jsonLine: String?

                while (true) {
                    jsonLine = reader.readLine()
                    if (jsonLine == null) {
                        println("Cliente desconectado")
                        onDisconnect?.invoke()
                        break
                    }
                    // Procesar la acción recibida
                    processIncomingAction(jsonLine)
                }
            } catch (e: Exception) {
                e.printStackTrace() // Cliente se desconectó abruptamente
            }
        }
    }

    // Esta función la llama tanto el Cliente (vía socket) como el Host (localmente)
    fun processIncomingAction(jsonAction: String) {
        scope.launch {
            try {
                val map = gson.fromJson(jsonAction, Map::class.java)
                val type = map["type"] as String

                if (type == "FLIP") {
                    val pos = (map["position"] as Double).toInt()
                    handleFlipLogic(pos)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Lógica CENTRAL del juego para el modo Online
    private suspend fun handleFlipLogic(position: Int) {
        // Verificar validez básica
        val card = game.cards.getOrNull(position) ?: return
        if (card.isFlipped || card.isMatched) return

        // 1. ANTES de voltear, verificamos si es la segunda carta.
        val isSecondCard = firstFlippedIndex != null

        // 2. Ejecutamos la lógica del juego. game.flipCard() se encarga de todo:
        //    - Voltea la carta.
        //    - Si es la segunda carta, comprueba si hay par.
        //    - Si hay par, suma puntos.
        //    - Si no hay par, cambia el turno (porque el 'mode' del juego es > 1).
        val matchFound = game.flipCard(position)

        // 3. Inmediatamente después de cualquier volteo, enviamos el estado.
        //    Así ambos jugadores ven la carta volteada al instante.
        broadcastState()

        // 4. Si fue la segunda carta...
        if (isSecondCard) {
            if (!matchFound) {
                // NO HUBO PAR: Esperamos para que vean el error.
                delay(1000)

                // La lógica de game.flipCard() ya cambió el turno. Ahora solo
                // necesitamos voltear las cartas de regreso.
                val first = firstFlippedIndex!!
                game.flipBack(first, position) // Usamos el método de la lógica de juego.

                // Enviamos el estado final con las cartas ocultas de nuevo.
                broadcastState()
            }
            // Si SÍ hubo par, el turno no cambia y las cartas quedan visibles. No hacemos nada más.

            // Limpiamos el índice para la siguiente jugada.
            firstFlippedIndex = null
        } else {
            // FUE LA PRIMERA CARTA: solo guardamos su posición.
            firstFlippedIndex = position
        }
    }


    private fun broadcastState() {
        val gameOver = game.cards.all { it.isMatched }
        val winner =
            if (gameOver) {
                if (game.scores[0] > game.scores[1]) 0
                else if (game.scores[1] > game.scores[0]) 1
                else -1  // empate
            } else null

        val state = GameState(
            cards = game.cards.map { it.copy() },
            scores = game.scores.toList(),
            currentPlayer = game.currentPlayer,
            isGameOver = gameOver,
            winner = winner,
            level = game.level
        )

        val type = object : TypeToken<GameState>() {}.type
        val jsonState = gson.toJson(state, type)

        // 1. Notificar al Host (UI Local)
        onStateChanged(state)

        // 2. Notificar al Cliente (Red)
        clientSocket?.let { socket ->
            scope.launch(Dispatchers.IO) {
                try {
                    val writer = PrintWriter(BufferedWriter(OutputStreamWriter(socket.getOutputStream())), true)
                    writer.println(jsonState)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Método helper para que la Activity del Host envíe su propia jugada
    fun sendActionToSelf(json: String) {
        processIncomingAction(json)
    }

    fun stopHosting() {
        try {
            scope.cancel() // Cancela coroutines activas

            clientSocket?.close()
            clientSocket = null

            serverSocket?.close()
            serverSocket = null

            println("Servidor detenido y puertos liberados.")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
