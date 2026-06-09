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

        // Se a mensagem contiver dados
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM", "Dados da mensagem: ${remoteMessage.data}")
        }

        // Se a mensagem contiver uma notificação
        remoteMessage.notification?.let {
            Log.d("FCM", "Corpo da notificação: ${it.body}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // Cancela corrotinas pendentes quando o serviço for destruído
    }
}
