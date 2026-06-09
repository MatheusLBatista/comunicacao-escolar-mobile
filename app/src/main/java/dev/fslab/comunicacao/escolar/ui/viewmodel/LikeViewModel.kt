package dev.fslab.comunicacao.escolar.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import retrofit2.HttpException
import dev.fslab.comunicacao.escolar.model.LikeResponse
import dev.fslab.comunicacao.escolar.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LikeState {

    object Idle: LikeState()
    object Loading: LikeState()
    data class Success(val like: LikeResponse): LikeState()
    data class Error(val message:String): LikeState()
}

class LikeViewModel: ViewModel() {

    companion object {
        private const val TAG = "LikeViewModel"
    }

    private val _likeState = MutableStateFlow<LikeState>(LikeState.Idle)
    val likeState: StateFlow<LikeState> = _likeState.asStateFlow()
    private val _like = MutableStateFlow<LikeResponse?>(null)
    val like: StateFlow<LikeResponse?> = _like.asStateFlow()

    fun postLike(postId: String) {
        viewModelScope.launch {
            _likeState.value = LikeState.Loading

            try{
                val response = RetrofitClient.likeApi.postLike(postId)
                _like.value = response
                _likeState.value = LikeState.Success(response)
                Log.d(TAG, "Sucesso ao dar like!")
            } catch (e: HttpException) {
                val errorMessage = when (e.code()) {
                    404 -> "Post não encontrado."
                    401 -> "Não autorizado. Faça login novamente."
                    403 -> "Acesso negado ao like."
                    500 -> "Erro no servidor. Tente novamente mais tarde."
                    498 -> "Sua sessão de login expirou. Faça login novamente."
                    else -> "Erro ao registrar like (${e.code()})"
                }
                _likeState.value = LikeState.Error(errorMessage)
                Log.e(TAG, "Erro http ao registrar like: ${e.code()}")
            } catch (e: java.net.UnknownHostException) {
                _likeState.value = LikeState.Error("Sem conexão com a internet")
                Log.e(TAG, "Erro de conexão", e)
            } catch (e: java.net.SocketTimeoutException) {
              _likeState.value = LikeState.Error("Tempo de conexão esgotado")
              Log.e(TAG, "Timeout ao registrar like", e)
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Erro ao registrar like. Tente novamente."
                _likeState.value = LikeState.Error(errorMsg)
                Log.e(TAG, "Erro ao registrar like", e)
            }
        }
    }
}