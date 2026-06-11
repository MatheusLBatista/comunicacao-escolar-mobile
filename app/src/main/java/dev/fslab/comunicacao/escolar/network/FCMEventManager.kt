package dev.fslab.comunicacao.escolar.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Gerenciador de eventos globais recebidos via FCM (Firebase Cloud Messaging).
 * Permite que diferentes ViewModels ou Telas reajam a notificações em tempo real.
 */
object FCMEventManager {
    
    // Representa o ID de um novo post criado
    private val _newPostEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val newPostEvent: SharedFlow<String> = _newPostEvent.asSharedFlow()

    /**
     * Emite um evento de novo post.
     */
    suspend fun emitNewPost(postId: String) {
        _newPostEvent.emit(postId)
    }
}
