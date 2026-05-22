package dev.fslab.comunicacao.escolar.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.comunicacao.escolar.model.ApiMessage
import dev.fslab.comunicacao.escolar.model.Conversation
import dev.fslab.comunicacao.escolar.model.CreateConversationRequest
import dev.fslab.comunicacao.escolar.model.Message
import dev.fslab.comunicacao.escolar.model.SendMessageRequest
import dev.fslab.comunicacao.escolar.model.toConversation
import dev.fslab.comunicacao.escolar.model.toMessage
import dev.fslab.comunicacao.escolar.network.RetrofitClient
import dev.fslab.comunicacao.escolar.network.SocketManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConversaViewModel : ViewModel() {

    companion object {
        private const val TAG = "ConversaViewModel"
        private const val PAGE_LIMIT = "50"
    }

    // Conversations list
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _conversationsLoading = MutableStateFlow(false)
    val conversationsLoading: StateFlow<Boolean> = _conversationsLoading.asStateFlow()

    // Messages for the currently open conversation
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _messagesLoading = MutableStateFlow(false)
    val messagesLoading: StateFlow<Boolean> = _messagesLoading.asStateFlow()

    // Currently open conversation id
    private val _currentConversationId = MutableStateFlow<String?>(null)
    val currentConversationId: StateFlow<String?> = _currentConversationId.asStateFlow()

    // Send state
    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    // Unread counts per conversation (updated by socket events)
    private val _unreadCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val unreadCounts: StateFlow<Map<String, Int>> = _unreadCounts.asStateFlow()

    // Last message preview cache (conversationId → preview text)
    private val _lastMessages = MutableStateFlow<Map<String, String>>(emptyMap())
    val lastMessages: StateFlow<Map<String, String>> = _lastMessages.asStateFlow()

    // Potential contacts for Nova Conversa
    private val _potentialContacts = MutableStateFlow<List<dev.fslab.comunicacao.escolar.model.ApiSchoolUser>>(emptyList())
    val potentialContacts: StateFlow<List<dev.fslab.comunicacao.escolar.model.ApiSchoolUser>> = _potentialContacts.asStateFlow()

    private val _contactsLoading = MutableStateFlow(false)
    val contactsLoading: StateFlow<Boolean> = _contactsLoading.asStateFlow()

    // Errors
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var currentUserId: String = ""
    private val seenMessageIds = mutableSetOf<String>()
    private var socketJob: Job? = null

    fun loadConversations(schoolId: String, currentUserId: String) {
        if (schoolId.isBlank()) return
        this.currentUserId = currentUserId
        viewModelScope.launch {
            _conversationsLoading.value = true
            try {
                val response = RetrofitClient.conversaApi.list(
                    schoolId,
                    mapOf("limit" to PAGE_LIMIT, "page" to "1")
                )
                val docs = response.data?.docs ?: emptyList()
                _conversations.value = docs.map { it.toConversation(currentUserId) }
                val previews = docs
                    .filter { !it.lastMessageText.isNullOrBlank() }
                    .associate { it.id to it.lastMessageText!! }
                _lastMessages.value = previews
                val unreadFromApi = docs
                    .filter { it.unreadCount > 0 }
                    .associate { it.id to it.unreadCount }
                _unreadCounts.value = unreadFromApi
            } catch (e: Exception) {
                Log.e(TAG, "loadConversations error", e)
                _error.value = "Não foi possível carregar as conversas."
            } finally {
                _conversationsLoading.value = false
            }
        }
    }

    fun loadMessages(conversationId: String, currentUserId: String) {
        this.currentUserId = currentUserId
        viewModelScope.launch {
            _messagesLoading.value = true
            seenMessageIds.clear()
            _messages.value = emptyList()
            try {
                val response = RetrofitClient.conversaApi.listMessages(
                    conversationId,
                    mapOf("limit" to PAGE_LIMIT, "page" to "1")
                )
                val docs = response.data?.docs ?: emptyList()
                // API returns newest-first; reverse to show oldest first in UI
                val msgs = docs.map { it.toMessage(currentUserId) }.reversed()
                seenMessageIds.addAll(msgs.map { it.id })
                _messages.value = msgs
                msgs.lastOrNull()?.let { last ->
                    _lastMessages.value = _lastMessages.value + (conversationId to last.text)
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadMessages error", e)
            } finally {
                _messagesLoading.value = false
            }
        }
    }

    fun sendMessage(conversationId: String, text: String) {
        if (text.isBlank() || _isSending.value) return
        viewModelScope.launch {
            _isSending.value = true
            try {
                val response = RetrofitClient.conversaApi.send(
                    conversationId,
                    SendMessageRequest(text.trim())
                )
                val msg = response.data?.toMessage(currentUserId) ?: return@launch
                // Optimistic add with dedup — socket may deliver the same message
                if (seenMessageIds.add(msg.id)) {
                    _messages.value = _messages.value + msg
                    _lastMessages.value = _lastMessages.value + (conversationId to msg.text)
                }
            } catch (e: Exception) {
                Log.e(TAG, "sendMessage error", e)
                _error.value = "Falha ao enviar mensagem."
            } finally {
                _isSending.value = false
            }
        }
    }

    fun markRead(conversationId: String) {
        viewModelScope.launch {
            try {
                RetrofitClient.conversaApi.markRead(conversationId)
                _messages.value = _messages.value.map { msg ->
                    if (!msg.readBy.contains(currentUserId)) {
                        msg.copy(readBy = msg.readBy + currentUserId)
                    } else msg
                }
                _unreadCounts.value = _unreadCounts.value - conversationId
            } catch (e: Exception) {
                Log.e(TAG, "markRead error", e)
            }
        }
    }

    fun openConversation(conversationId: String) {
        _currentConversationId.value = conversationId
        SocketManager.joinConversation(conversationId)
        startObservingSocket()
    }

    fun closeConversation() {
        _currentConversationId.value?.let { SocketManager.leaveConversation(it) }
        _currentConversationId.value = null
        _messages.value = emptyList()
        seenMessageIds.clear()
        socketJob?.cancel()
        socketJob = null
    }

    fun findOrCreateConversation(schoolId: String, participantId: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.conversaApi.findOrCreate(
                    schoolId,
                    CreateConversationRequest(participantId)
                )
                val id = response.data?.id ?: return@launch
                onResult(id)
            } catch (e: Exception) {
                Log.e(TAG, "findOrCreate error", e)
                _error.value = "Não foi possível iniciar a conversa."
            }
        }
    }

    // Carrega todos os usuários não-estudantes da escola, excluindo o próprio usuário.
    // UI filtra por role conforme o papel do usuário logado.
    fun loadPotentialContacts(schoolId: String, excludeUserId: String) {
        viewModelScope.launch {
            _contactsLoading.value = true
            try {
                val response = RetrofitClient.adminApi.listUsers(
                    schoolId,
                    mapOf("limit" to "100", "page" to "1")
                )
                val docs = response.data?.docs ?: emptyList()
                _potentialContacts.value = docs.filter { u ->
                    u.id != excludeUserId &&
                    u.memberships.any { m -> m.role != "student" && m.schoolId == schoolId }
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadPotentialContacts error", e)
            } finally {
                _contactsLoading.value = false
            }
        }
    }

    fun clearError() { _error.value = null }

    fun startListening() {
        if (socketJob?.isActive == true) return
        startObservingSocket()
    }

    fun stopListening() {
        if (_currentConversationId.value == null) {
            socketJob?.cancel()
            socketJob = null
        }
    }

    private fun startObservingSocket() {
        socketJob?.cancel()
        socketJob = viewModelScope.launch {
            launch {
                SocketManager.newMessages.collect { apiMsg ->
                    handleIncomingMessage(apiMsg)
                }
            }
            launch {
                SocketManager.readReceipts.collect { receipt ->
                    if (receipt.conversationId == _currentConversationId.value) {
                        _messages.value = _messages.value.map { msg ->
                            if (!msg.readBy.contains(receipt.userId)) {
                                msg.copy(readBy = msg.readBy + receipt.userId)
                            } else msg
                        }
                    }
                }
            }
        }
    }

    private fun handleIncomingMessage(apiMsg: ApiMessage) {
        val msg = apiMsg.toMessage(currentUserId)
        if (!seenMessageIds.add(msg.id)) return // dedup

        if (msg.conversationId == _currentConversationId.value) {
            _messages.value = _messages.value + msg
        } else {
            val current = _unreadCounts.value[msg.conversationId] ?: 0
            _unreadCounts.value = _unreadCounts.value + (msg.conversationId to current + 1)
        }
        _lastMessages.value = _lastMessages.value + (msg.conversationId to msg.text)
    }

    override fun onCleared() {
        super.onCleared()
        socketJob?.cancel()
    }
}
