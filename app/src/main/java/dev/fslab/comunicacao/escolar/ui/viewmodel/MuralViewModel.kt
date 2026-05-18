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

    fun getPosts(schoolId: String) {
        Log.d(TAG, "Estamos começando uma requisição")

        viewModelScope.launch {
            _muralState.value = MuralState.Loading
            try {
                val response = RetrofitClient.muralApi.getPosts(schoolId)
                _posts.value = response
                _muralState.value = MuralState.Success(response)
                Log.d(TAG, "Posts carregados com sucesso: ${response.data.docs.size} posts")
            } catch (e: HttpException) {
                val errorMessage = when (e.code()) {
                    404 -> "Post não encontrado."
                    401 -> "Não autorizado. Faça login novamente."
                    403 -> "Acesso negado ao mural."
                    500 -> "Erro no servidor. Tente novamente mais tarde."
                    498 -> "Sua sessão de login expirou. Faça login novamente."
                    else -> "Erro ao carregar posts (${e.code()})"
                }
                _muralState.value = MuralState.Error(errorMessage)
                Log.e(TAG, "Erro HTTP ao carregar posts: ${e.code()}", e)
            } catch (e: java.net.UnknownHostException) {
                _muralState.value = MuralState.Error("Sem conexão com a internet")
                Log.e(TAG,"Erro de conexão", e)
            } catch (e: java.net.SocketTimeoutException) {
                _muralState.value = MuralState.Error("Tempo de conexão esgotado")
                Log.e(TAG, "Timeout ao carregar posts", e)
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Erro ao carregar posts. Tente novamnete."
                _muralState.value = MuralState.Error(errorMsg)
                Log.e(TAG, "Erro ao carregar posts", e)
            }
        }
    }

    fun clearError() {
        if(_muralState.value is MuralState.Error) {
            _muralState.value = MuralState.Idle
        }
    }
}
