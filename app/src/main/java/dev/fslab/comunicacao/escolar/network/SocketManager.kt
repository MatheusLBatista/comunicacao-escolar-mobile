package dev.fslab.comunicacao.escolar.network

import android.util.Log
import com.google.gson.Gson
import dev.fslab.comunicacao.escolar.model.ApiMessage
import dev.fslab.comunicacao.escolar.model.ReadReceiptEvent
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject

object SocketManager {

    private const val TAG = "SocketManager"

    private var socket: Socket? = null
    private val gson = Gson()

    private val _newMessages = MutableSharedFlow<ApiMessage>(extraBufferCapacity = 64)
    val newMessages: SharedFlow<ApiMessage> = _newMessages.asSharedFlow()

    private val _readReceipts = MutableSharedFlow<ReadReceiptEvent>(extraBufferCapacity = 64)
    val readReceipts: SharedFlow<ReadReceiptEvent> = _readReceipts.asSharedFlow()

    val isConnected: Boolean get() = socket?.connected() == true

    fun connect(accessToken: String) {
        if (socket?.connected() == true) return
        try {
            val url = RetrofitClient.BASE_URL.trimEnd('/')
            val opts = IO.Options().apply {
                auth = mapOf("token" to accessToken)
                reconnection = true
                reconnectionAttempts = Int.MAX_VALUE
                reconnectionDelay = 1000L
                reconnectionDelayMax = 5000L
            }
            socket = IO.socket(url, opts).also { s ->
                s.on(Socket.EVENT_CONNECT) {
                    Log.d(TAG, "Connected")
                }
                s.on(Socket.EVENT_DISCONNECT) { args ->
                    Log.d(TAG, "Disconnected: ${args.firstOrNull()}")
                }
                s.on(Socket.EVENT_CONNECT_ERROR) { args ->
                    Log.w(TAG, "Connect error: ${args.firstOrNull()}")
                }
                s.on("message:new") { args ->
                    val json = args.toJson() ?: return@on
                    try {
                        val msg = gson.fromJson(json, ApiMessage::class.java)
                        _newMessages.tryEmit(msg)
                    } catch (e: Exception) {
                        Log.e(TAG, "Parsing message:new failed", e)
                    }
                }
                s.on("message:read") { args ->
                    val json = args.toJson() ?: return@on
                    try {
                        val receipt = gson.fromJson(json, ReadReceiptEvent::class.java)
                        _readReceipts.tryEmit(receipt)
                    } catch (e: Exception) {
                        Log.e(TAG, "Parsing message:read failed", e)
                    }
                }
                s.connect()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Socket connect failed", e)
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
        Log.d(TAG, "Disconnected (manual)")
    }

    fun joinConversation(conversationId: String) {
        socket?.emit("join", "conversation:$conversationId")
        Log.d(TAG, "Joined conversation:$conversationId")
    }

    fun leaveConversation(conversationId: String) {
        socket?.emit("leave", "conversation:$conversationId")
        Log.d(TAG, "Left conversation:$conversationId")
    }

    private fun Array<out Any>.toJson(): String? {
        val raw = firstOrNull() ?: return null
        return when (raw) {
            is JSONObject -> raw.toString()
            is String -> raw
            else -> raw.toString()
        }
    }
}
