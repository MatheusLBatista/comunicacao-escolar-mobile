package dev.fslab.comunicacao.escolar.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gerenciador de eventos globais recebidos via FCM (Firebase Cloud Messaging).
 * Permite que diferentes ViewModels ou Telas reajam a notificações em tempo real.
 */
object FCMEventManager {

    // Representa o ID de um novo post criado
    private val _newPostEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val newPostEvent: SharedFlow<String> = _newPostEvent.asSharedFlow()

    // Representa o ID de um post deletado
    private val _deletePostEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val deletePostEvent: SharedFlow<String> = _deletePostEvent.asSharedFlow()

    // Representa uma nova mensagem de chat recebida via FCM
    data class NewMessageFcmEvent(val conversationId: String, val messageId: String)
    private val _newMessageEvent = MutableSharedFlow<NewMessageFcmEvent>(extraBufferCapacity = 1)
    val newMessageEvent: SharedFlow<NewMessageFcmEvent> = _newMessageEvent.asSharedFlow()

    // Solicita navegação para uma conversa específica (disparado ao tocar na notificação).
    // É um StateFlow para que o pedido sobreviva ao cold start: quando o usuário toca na
    // notificação com o app fechado, o valor é emitido antes de a tela registrar o coletor.
    // Com StateFlow o último valor é retido e entregue ao coletor quando ele assina.
    private val _navigateToConversationEvent = MutableStateFlow<String?>(null)
    val navigateToConversationEvent: StateFlow<String?> = _navigateToConversationEvent.asStateFlow()

    // ID da conversa atualmente aberta na UI. Usado pelo service para não exibir
    // notificação de mensagens da conversa que o usuário já está visualizando.
    @Volatile
    var activeConversationId: String? = null

    suspend fun emitNewPost(postId: String) {
        _newPostEvent.emit(postId)
    }

    suspend fun emitDeletePost(postId: String) {
        _deletePostEvent.emit(postId)
    }

    suspend fun emitNewMessage(conversationId: String, messageId: String) {
        _newMessageEvent.emit(NewMessageFcmEvent(conversationId, messageId))
    }

    fun emitNavigateToConversation(conversationId: String) {
        _navigateToConversationEvent.value = conversationId
    }

    // Deve ser chamado após a navegação ser tratada, para não re-disparar em
    // recomposições/reassinaturas (ex.: mudança de configuração).
    fun consumeNavigateToConversation() {
        _navigateToConversationEvent.value = null
    }
}
