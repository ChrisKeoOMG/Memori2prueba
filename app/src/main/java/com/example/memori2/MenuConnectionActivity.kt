package com.example.memori2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.net.Inet4Address
import java.net.NetworkInterface

class MenuConnectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu_connection_activity)
        supportActionBar?.hide()

        val nivel = intent.getIntExtra("nivel", 1)

        // Referencias UI
        val tvIp = findViewById<TextView>(R.id.tvIpAddress)
        val etIpInput = findViewById<EditText>(R.id.etIpInput)
        val btnHost = findViewById<Button>(R.id.btnHost)
        val btnConnect = findViewById<Button>(R.id.btnConnect)
        val btnBack = findViewById<ImageButton>(R.id.btn_atras)

        // 1. Mostrar IP Local automáticamente
        tvIp.text = getLocalIpAddress()

        // 2. Botón para ser ANFITRIÓN
        btnHost.setOnClickListener {
            val intent = Intent(this, MemoryGameActivity::class.java)
            intent.putExtra("mode", 3)      // Modo Online
            intent.putExtra("isHost", true) // Es Servidor
            intent.putExtra("nivel", nivel)
            startActivity(intent)
        }

        // 3. Botón para CONECTAR (Cliente)
        btnConnect.setOnClickListener {
            var targetIp = etIpInput.text.toString().trim()
            targetIp = "192.168.3.22"
            if (targetIp.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa una IP", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, MemoryGameActivity::class.java)
                intent.putExtra("mode", 3)       // Modo Online
                intent.putExtra("isHost", false) // Es Cliente
                intent.putExtra("targetIp", targetIp) // IP a conectar
                intent.putExtra("nivel", nivel)
                startActivity(intent)
            }
        }

        btnBack.setOnClickListener { finish() }
    }

    // Función auxiliar para obtener la IP del Wi-Fi
    private fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                // Filtrar interfaces loopback o inactivas
                if (iface.isLoopback || !iface.isUp) continue

                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    // Queremos la IPv4 (ej. 192.168.1.5)
                    if (addr is Inet4Address) {
                        return addr.hostAddress ?: "Error"
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "No encontrada"
    }
}