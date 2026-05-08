package dev.fslab.comunicacao.escolar.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fslab.comunicacao.escolar.model.UpdateUserRequest
import dev.fslab.comunicacao.escolar.model.User
import dev.fslab.comunicacao.escolar.network.RetrofitClient
import dev.fslab.comunicacao.escolar.network.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

sealed class PerfilUiState {
    object Idle : PerfilUiState()
    object Loading : PerfilUiState()
    data class Success(val message: String) : PerfilUiState()
    data class Error(val message: String) : PerfilUiState()
}

class PerfilViewModel : ViewModel() {

    companion object {
        private const val TAG = "PerfilViewModel"
    }

    private val _uiState = MutableStateFlow<PerfilUiState>(PerfilUiState.Idle)
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    private val _nomeEditado = MutableStateFlow("")
    val nomeEditado: StateFlow<String> = _nomeEditado.asStateFlow()

    private val _salvando = MutableStateFlow(false)
    val salvando: StateFlow<Boolean> = _salvando.asStateFlow()

    fun setNome(nome: String) {
        _nomeEditado.value = nome
    }

    fun inicializar(user: User) {
        _nomeEditado.value = user.nome
    }

    fun salvarNome(userId: String, novoNome: String, onSuccess: (User) -> Unit) {
        viewModelScope.launch {
            _salvando.value = true
            try {
                val response = RetrofitClient.userApi.update(
                    id = userId,
                    request = UpdateUserRequest(fullName = novoNome.trim())
                )
                if (response.isSuccess()) {
                    val updatedUser = response.data?.toUser()
                    if (updatedUser != null) {
                        onSuccess(updatedUser)
                        _uiState.value = PerfilUiState.Success("Nome atualizado com sucesso!")
                    }
                } else {
                    _uiState.value = PerfilUiState.Error(response.getErrorMessage())
                }
            } catch (e: retrofit2.HttpException) {
                val msg = when (e.code()) {
                    403 -> "Sem permissão para editar este perfil."
                    404 -> "Usuário não encontrado."
                    else -> "Erro ao salvar (${e.code()})."
                }
                _uiState.value = PerfilUiState.Error(msg)
                Log.e(TAG, "Erro HTTP ao salvar nome", e)
            } catch (e: Exception) {
                _uiState.value = PerfilUiState.Error("Erro: ${e.localizedMessage ?: "Tente novamente"}")
                Log.e(TAG, "Erro ao salvar nome", e)
            } finally {
                _salvando.value = false
            }
        }
    }

    fun clearState() {
        if (_uiState.value is PerfilUiState.Error || _uiState.value is PerfilUiState.Success) {
            _uiState.value = PerfilUiState.Idle
        }
    }

    fun uploadAvatar(context: Context, uri: Uri, onSuccess: (User) -> Unit) {
        viewModelScope.launch {
            _salvando.value = true
            try {
                val contentResolver = context.contentResolver
                val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
                val bytes = contentResolver.openInputStream(uri)?.readBytes()
                    ?: throw IllegalStateException("Não foi possível ler a imagem")

                val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                val extension = when (mimeType) {
                    "image/png"  -> "png"
                    "image/webp" -> "webp"
                    else         -> "jpg"
                }
                val part = MultipartBody.Part.createFormData(
                    name = "avatar",
                    filename = "avatar.$extension",
                    body = requestBody
                )

                val response = RetrofitClient.userApi.uploadAvatar(part)
                if (response.isSuccess()) {
                    val updatedUser = response.data?.toUser()
                    if (updatedUser != null) {
                        onSuccess(updatedUser)
                        _uiState.value = PerfilUiState.Success("Foto atualizada com sucesso!")
                    }
                } else {
                    _uiState.value = PerfilUiState.Error(response.getErrorMessage())
                }
            } catch (e: retrofit2.HttpException) {
                _uiState.value = PerfilUiState.Error("Erro ao enviar imagem (${e.code()}).")
                Log.e(TAG, "Erro HTTP ao fazer upload de avatar", e)
            } catch (e: Exception) {
                _uiState.value = PerfilUiState.Error("Erro: ${e.localizedMessage ?: "Tente novamente"}")
                Log.e(TAG, "Erro ao fazer upload de avatar", e)
            } finally {
                _salvando.value = false
            }
        }
    }
}
