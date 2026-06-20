package com.example.logist_tech.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import android.util.Log

object WebSocketManager {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    fun connect(userId: String, onMessageReceived: (String) -> Unit) {
        val request = Request.Builder()
            .url("ws://38.250.116.214:8080/ws/$userId")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                onMessageReceived(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Error: ${t.message}")
                // Intentar reconectar en 5 segundos si falla
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "App cerrada")
        webSocket = null
    }
}
