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
    }
    private val _muralState = MutableStateFlow<MuralState>(MuralState.Idle)
    val muralState: StateFlow<MuralState> = _muralState.asStateFlow()

    private val _posts = MutableStateFlow<MuralResponse?>(null)
    val posts: StateFlow<MuralResponse?> = _posts.asStateFlow()

    private var hasNextPage = true
    private var isPaginationLoading = false

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
                
                val currentResponse = _posts.value
                if (currentResponse != null) {
                    val currentDocs = currentResponse.data.docs.toMutableList()
                    
                    // Verifica se o post já não está na lista
                    if (currentDocs.none { it.id == newPost.id }) {
                        currentDocs.add(0, newPost) // Adiciona ao topo
                        
                        val updatedResponse = currentResponse.copy(
                            data = currentResponse.data.copy(docs = currentDocs)
                        )
                        _posts.value = updatedResponse
                        Log.d(TAG, "Novo post adicionado ao topo do mural via FCM.")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao buscar post individual após notificação FCM", e)
            }
        }
    }

    fun getPosts(schoolId: String, loadMore: Boolean = false) {
        if (loadMore && (!hasNextPage || isPaginationLoading)) return

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
                    Log.d(TAG, "Novos posts adicionados. Qtd recebida: ${newDocs.size}. Total agora: ${updatedDocs.size}")
                } else {
                    // Refresh ou carga inicial: substitui tudo
                    _posts.value = response
                    _muralState.value = MuralState.Success(response)
                    Log.d(TAG, "Carga inicial/Refresh concluída. Total: ${response.data.docs.size}")
                }

                hasNextPage = response.data.hasNextPage
                Log.d(TAG, "Status da paginação: Próxima página disponível: $hasNextPage")

            } catch (e: Exception) {
                if (!loadMore) {
                    _muralState.value = MuralState.Error(e.localizedMessage ?: "Erro ao carregar")
                }
                Log.e(TAG, "Erro ao carregar posts", e)
            } finally {
                isPaginationLoading = false
                if (!loadMore && _muralState.value is MuralState.Loading) {
                    // Fallback para garantir que saia do estado de loading caso não tenha entrado em Success/Error
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
}
