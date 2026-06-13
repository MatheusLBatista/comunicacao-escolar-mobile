package dev.fslab.comunicacao.escolar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.comunicacao.escolar.model.MuralResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dev.fslab.comunicacao.escolar.network.RetrofitClient
import retrofit2.HttpException
import android.util.Log
import dev.fslab.comunicacao.escolar.navigation.Screen
import kotlinx.coroutines.delay


sealed class MuralState {
    object Idle: MuralState()
    object Loading: MuralState()
    data class Success(val posts:MuralResponse) : MuralState()
    data class Error(val message: String) : MuralState()
}

class MuralViewModel : ViewModel() {
    companion object {
        private const val TAG = "MuralViewModel"
        private const val CACHE_DURATION_MILLIS = 5 * 60 * 1000 // 5 minutos
        private const val MAX_CACHE_SIZE = 10
    }
    private val _muralState = MutableStateFlow<MuralState>(MuralState.Idle)
    val muralState: StateFlow<MuralState> = _muralState.asStateFlow()

    private val _posts = MutableStateFlow<MuralResponse?>(null)
    val posts: StateFlow<MuralResponse?> = _posts.asStateFlow()

    private val _authors = MutableStateFlow<Map<String, dev.fslab.comunicacao.escolar.model.ApiUser>>(emptyMap())
    val authors: StateFlow<Map<String, dev.fslab.comunicacao.escolar.model.ApiUser>> = _authors.asStateFlow()

    private var hasNextPage = true
    private var isPaginationLoading = false
    private var lastFetchTime: Long = 0

    init {
        // Observa eventos de novos posts vindos do FCM
        viewModelScope.launch {
            dev.fslab.comunicacao.escolar.network.FCMEventManager.newPostEvent.collect { postId ->
                Log.d(TAG, "Evento de novo post recebido via FCM: $postId. Buscando detalhes...")
                fetchNewPost(postId)
            }
        }
    }

    private fun fetchNewPost(postId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.muralApi.getPost(postId)
                val newPost = response.data
                
                // Aproveita para buscar o autor do novo post
                fetchAuthor(newPost.authorId)

                val currentResponse = _posts.value
                if (currentResponse != null) {
                    val currentDocs = currentResponse.data.docs.toMutableList()
                    
                    // Verifica se o post já não está na lista
                    if (currentDocs.none { it.id == newPost.id }) {
                        currentDocs.add(0, newPost) // Adiciona ao topo
                        
                        // Mantém apenas os 10 mais recentes se exceder o limite
                        val limitedDocs = currentDocs.take(MAX_CACHE_SIZE)
                        
                        val updatedResponse = currentResponse.copy(
                            data = currentResponse.data.copy(docs = limitedDocs)
                        )
                        _posts.value = updatedResponse
                        
                        // Atualiza o estado para Success para refletir a mudança na UI se necessário
                        if (_muralState.value is MuralState.Success) {
                            _muralState.value = MuralState.Success(updatedResponse)
                        }
                        
                        Log.d(TAG, "Novo post adicionado via FCM. Cache limitado a $MAX_CACHE_SIZE itens.")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao buscar post individual após notificação FCM", e)
            }
        }
    }

    fun fetchAuthor(authorId: String) {
        if (authorId.isEmpty() || _authors.value.containsKey(authorId)) return

        viewModelScope.launch {
            try {
                val response = RetrofitClient.userApi.getById(authorId)
                if (response.isSuccess()) {
                    response.data?.let { user ->
                        val currentMap = _authors.value.toMutableMap()
                        currentMap[authorId] = user
                        _authors.value = currentMap
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao buscar autor $authorId", e)
            }
        }
    }

    fun getPosts(schoolId: String, loadMore: Boolean = false, forceRefresh: Boolean = false) {
        if (loadMore && (!hasNextPage || isPaginationLoading)) return

        // Regra de persistência: se não for loadMore, não for forceRefresh e houver cache válido (< 5 min), não recarrega
        val currentTime = System.currentTimeMillis()
        if (!loadMore && !forceRefresh && _posts.value != null && (currentTime - lastFetchTime < CACHE_DURATION_MILLIS)) {
            Log.d(TAG, "Usando cache do mural (${_posts.value?.data?.docs?.size} posts).")
            _muralState.value = MuralState.Success(_posts.value!!)
            return
        }

        viewModelScope.launch {
            val beforeDate = if (loadMore) {
                _posts.value?.data?.docs?.lastOrNull()?.createdAt
            } else null

            if (loadMore) {
                val currentCount = _posts.value?.data?.docs?.size ?: 0
                Log.d(TAG, "Chegou ao fim da tela. Fazendo nova requisição para API (loadMore=true). posts atuais: $currentCount, buscando posts anteriores a: $beforeDate")
                isPaginationLoading = true
            } else {
                _muralState.value = MuralState.Loading
            }

            try {
                val response = RetrofitClient.muralApi.getPosts(schoolId, beforeDate)
                
                if (loadMore) {
                    val currentDocs = _posts.value?.data?.docs ?: emptyList()
                    val newDocs = response.data.docs
                    
                    // Adicionamos os novos (mais antigos) ao final da lista atual
                    val updatedDocs = (currentDocs + newDocs).distinctBy { it.id }
                    val updatedResponse = response.copy(
                        data = response.data.copy(docs = updatedDocs)
                    )
                    _posts.value = updatedResponse
                    _muralState.value = MuralState.Success(updatedResponse)
                    Log.d(TAG, "Novos posts adicionados. Qtd recebida: ${newDocs.size}. Total agora: ${updatedDocs.size}")
                } else {
                    // Carga inicial ou Refresh
                    lastFetchTime = System.currentTimeMillis()
                    
                    // Limitamos aos 10 itens iniciais para o cache de persistência
                    val limitedDocs = response.data.docs.take(MAX_CACHE_SIZE)
                    val limitedResponse = response.copy(
                        data = response.data.copy(docs = limitedDocs)
                    )

                    _posts.value = limitedResponse
                    _muralState.value = MuralState.Success(limitedResponse)
                    Log.d(TAG, "Carga inicial concluída. Cache de $MAX_CACHE_SIZE itens persistido.")
                }

                hasNextPage = response.data.hasNextPage

            } catch (e: Exception) {
                if (!loadMore) {
                    _muralState.value = MuralState.Error(e.localizedMessage ?: "Erro ao carregar")
                }
                Log.e(TAG, "Erro ao carregar posts", e)
            } finally {
                isPaginationLoading = false
                if (!loadMore && _muralState.value is MuralState.Loading) {
                    _posts.value?.let { _muralState.value = MuralState.Success(it) }
                }
            }
        }
    }

    suspend fun getAttachment(id: String): ByteArray? {
        return try {
            val responseBody = RetrofitClient.muralApi.getAttachment(id)
            responseBody.bytes()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar anexo: ${e.message}")
            null
        }
    }

    fun clearError() {
        if(_muralState.value is MuralState.Error) {
            _muralState.value = MuralState.Idle
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.muralApi.deletePost(postId)
                if (!response.error) {
                    // Remove o post da lista local
                    val currentResponse = _posts.value
                    if (currentResponse != null) {
                        val updatedDocs = currentResponse.data.docs.filter { it.id != postId }
                        val updatedResponse = currentResponse.copy(
                            data = currentResponse.data.copy(docs = updatedDocs)
                        )
                        _posts.value = updatedResponse
                        
                        // Se o estado for Success, atualiza a UI
                        if (_muralState.value is MuralState.Success) {
                            _muralState.value = MuralState.Success(updatedResponse)
                        }
                    }
                    Log.d(TAG, "Post $postId deletado com sucesso.")
                } else {
                    Log.e(TAG, "Erro ao deletar post: ${response.message}")
                    _muralState.value = MuralState.Error(response.message ?: "Erro ao deletar post")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exceção ao deletar post", e)
                _muralState.value = MuralState.Error(e.localizedMessage ?: "Erro ao processar exclusão")
            }
        }
    }
}
