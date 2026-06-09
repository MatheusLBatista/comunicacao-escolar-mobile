package dev.fslab.comunicacao.escolar.model

import com.google.gson.annotations.SerializedName

data class ApiEvent(
    @SerializedName("_id") val id: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("description") val description: String? = null,
    @SerializedName("start_date") val startDate: String = "",
    @SerializedName("end_date") val endDate: String? = null,
    @SerializedName("all_day") val allDay: Boolean = false,
    @SerializedName("class_ids") val classIds: List<String> = emptyList(),
    @SerializedName("class_names") val classNames: List<String> = emptyList(),
    @SerializedName("created_at") val createdAt: String? = null
)

data class Evento(
    val id: String,
    val titulo: String,
    val descricao: String?,
    val dataInicio: String,
    val dataFim: String?,
    val nomeTurmas: List<String>,
    val classIds: List<String> = emptyList(),
    val allDay: Boolean = false
)

fun ApiEvent.toEvento() = Evento(
    id = id,
    titulo = title,
    descricao = description,
    dataInicio = startDate,
    dataFim = endDate,
    nomeTurmas = classNames,
    classIds = classIds,
    allDay = allDay
)

data class CreateEventTarget(
    @SerializedName("scope") val scope: String,
    @SerializedName("target_ids") val targetIds: List<String>
)

data class UpdateEventRequest(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String?,
    @SerializedName("all_day") val allDay: Boolean,
    @SerializedName("target") val target: CreateEventTarget
)

data class CreateEventRequest(
    @SerializedName("school_id") val schoolId: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("type") val type: String,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String?,
    @SerializedName("all_day") val allDay: Boolean = false,
    @SerializedName("target") val target: CreateEventTarget
)
