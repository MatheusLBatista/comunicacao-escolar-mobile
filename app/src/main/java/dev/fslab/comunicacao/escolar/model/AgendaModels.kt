package dev.fslab.comunicacao.escolar.model

import com.google.gson.annotations.SerializedName

data class ApiEvent(
    @SerializedName("_id") val id: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("description") val description: String? = null,
    @SerializedName("start_datetime") val startDatetime: String = "",
    @SerializedName("end_datetime") val endDatetime: String? = null,
    @SerializedName("class_ids") val classIds: List<String> = emptyList(),
    @SerializedName("class_names") val classNames: List<String> = emptyList(),
    @SerializedName("school_id") val schoolId: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class Evento(
    val id: String,
    val titulo: String,
    val descricao: String?,
    val dataInicio: String,
    val dataFim: String?,
    val nomeTurmas: List<String>
)

fun ApiEvent.toEvento() = Evento(
    id = id,
    titulo = title,
    descricao = description,
    dataInicio = startDatetime,
    dataFim = endDatetime,
    nomeTurmas = classNames
)
