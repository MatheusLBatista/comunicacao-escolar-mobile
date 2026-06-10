package dev.fslab.comunicacao.escolar.network

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dev.fslab.comunicacao.escolar.model.FcmTokenRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // Escopo de corrotina vinculado ao ciclo de vida do serviço
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Novo token recebido: $token")
        
        // Salva o token localmente de forma segura
        TokenManager.saveFcmToken(token)
        
        // Se o usuário já estiver logado, envia para a API imediatamente
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
        val postId = data["postId"]

        // Se for um novo post, notificamos o sistema interno
        if (type == "announcement" && postId != null) {
            scope.launch {
                FCMEventManager.emitNewPost(postId)
            }
        }

        // Se a mensagem contiver uma notificação, e NÃO for um post (ou quisermos mostrar sempre no sistema)
        // No Android, se o app está em foreground, a notificação do sistema NÃO aparece automaticamente.
        // Se quisermos que apareça, teríamos que criar uma notificação manualmente aqui.
        // Como o usuário quer que seja "internamente sem aparecer na notificação", não fazemos nada se for post.
        
        remoteMessage.notification?.let {
            Log.d("FCM", "Corpo da notificação: ${it.body}")
            // Se não for um anúncio, ou se você quiser mostrar outras notificações mesmo em foreground:
            // if (type != "announcement") { ... mostrar notificação manual ... }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // Cancela corrotinas pendentes quando o serviço for destruído
    }
}
