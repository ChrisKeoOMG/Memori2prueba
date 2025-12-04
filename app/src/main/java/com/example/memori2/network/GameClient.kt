package com.example.memori2.network

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import java.io.*
import java.net.ConnectException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException

class GameClient(
    private val hostIp: String,
    private val onStateReceived: (GameState) -> Unit
) {
    private var socket: Socket? = null
    private val PORT = 5003
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO)

    var onDisconnect: (() -> Unit)? = null
    var onConnectionFailed: ((String) -> Unit)? = null
    fun startConnecting() {
        scope.launch {
            val maxRetries = 10 // Número de reintentos
            val retryDelay = 1000L // 1 segundo entre reintentos
            var success = false

            for (attempt in 1..maxRetries) {
                if (!isActive) return@launch

                try {
                    // Intentar conectar al servidor
                    socket = Socket()
                    socket?.connect(
                        InetSocketAddress(hostIp, PORT), 5000)

                    success = true

                    listenForState()
                    break


                } catch (e: ConnectException) {
                    Log.w("GameClient", "Intento $attempt fallido: El Host no está listo. Reintentando en $retryDelay ms...")
                    delay(retryDelay) // Esperar antes del siguiente intento
                } catch (e: SocketTimeoutException) {
                    Log.w("GameClient", "Intento $attempt fallido: Timeout. Reintentando...")
                    delay(retryDelay)
                } catch (e: Exception) {
                    Log.e("GameClient", "Error inesperado al conectar: ${e.message}")

                    disconnect()
                    return@launch
                }
            }

            if (!success) {
                Log.e("GameClient", "No se pudo conectar al host después de $maxRetries intentos.")
                // Notificar a la UI que la conexión falló definitivamente
                onConnectionFailed?.invoke("No se pudo encontrar al Host en la IP: $hostIp")
                disconnect()
            }
        }
    }

    private fun listenForState() {
        // Escuchar mensajes del servidor
        val reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
        var line: String?

        val type = object : TypeToken<GameState>() {}.type

        while (socket!!.isConnected) {
            line = reader.readLine()
            if (line != null) {
                // Convertir JSON a GameState
                val state: GameState = gson.fromJson(line, type)
                onStateReceived(state)
            } else {
                onDisconnect?.invoke()
                break // Servidor cerró conexión
            }
        }
    }

    fun sendAction(jsonAction: String) {
        scope.launch {
            try {
                socket?.let {
                    val writer = PrintWriter(BufferedWriter(OutputStreamWriter(it.getOutputStream())), true)
                    writer.println(jsonAction)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun disconnect() {
        try {
            scope.cancel()
            socket?.close()
            socket = null
            println("Cliente desconectado.")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}