package dev.fslab.comunicacao.escolar.network

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dev.fslab.comunicacao.escolar.MainActivity
import dev.fslab.comunicacao.escolar.R
import dev.fslab.comunicacao.escolar.model.FcmTokenRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Novo token recebido: $token")
        TokenManager.saveFcmToken(token)
        if (TokenManager.isAuthenticated()) {
            enviarTokenParaServidor(token)
        }
    }

    private fun enviarTokenParaServidor(token: String) {
        scope.launch {
            try {
                val request = FcmTokenRequest(token)
                RetrofitClient.userApi.updateFcmToken(request)
                Log.d("FCM", "Token sincronizado com o servidor com sucesso")
            } catch (e: Exception) {
                Log.e("FCM", "Erro ao sincronizar token com servidor: ${e.message}")
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "Mensagem recebida de: ${remoteMessage.from}")

        val data = remoteMessage.data
        val type = data["type"]

        when (type) {
            "announcement" -> {
                val postId = data["postId"] ?: return
                scope.launch { FCMEventManager.emitNewPost(postId) }
            }
            "delete_post" -> {
                val postId = data["postId"] ?: return
                scope.launch { FCMEventManager.emitDeletePost(postId) }
            }
            "new_message" -> {
                val conversationId = data["conversationId"] ?: return
                val messageId = data["messageId"] ?: return
                scope.launch { FCMEventManager.emitNewMessage(conversationId, messageId) }
                // Não notifica se o usuário já está visualizando essa conversa.
                if (conversationId != FCMEventManager.activeConversationId) {
                    mostrarNotificacaoChat(conversationId, data)
                }
            }
        }
    }

    private fun mostrarNotificacaoChat(conversationId: String, data: Map<String, String>) {
        val titulo = data["senderName"] ?: "Nova mensagem"
        val corpo = data["messageText"] ?: "Você recebeu uma nova mensagem"

        val tapIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_CONVERSATION_ID, conversationId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, conversationId.hashCode(), tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_CHAT)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(titulo)
            .setContentText(corpo)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(conversationId.hashCode(), notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    companion object {
        const val CHANNEL_CHAT = "chat_messages"
        const val EXTRA_CONVERSATION_ID = "conversationId"
    }
}
