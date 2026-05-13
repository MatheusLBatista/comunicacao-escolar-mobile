package dev.fslab.comunicacao.escolar.model

import com.google.gson.annotations.SerializedName

data class DailyLogsResponse(
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("code") val code: Int = 0,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: DailyLogsData? = null,
    @SerializedName("errors") val errors: List<String> = emptyList()
) {
    fun getErrorMessage(): String = errors.firstOrNull() ?: message
}

data class DailyLogsData(
    @SerializedName("docs") val docs: List<DailyLogDoc> = emptyList(),
    @SerializedName("totalDocs") val totalDocs: Int = 0,
    @SerializedName("limit") val limit: Int = 0,
    @SerializedName("totalPages") val totalPages: Int = 0,
    @SerializedName("page") val page: Int = 0
)

data class DailyLogDoc(
    @SerializedName("_id") val id: String = "",
    @SerializedName("school_id") val schoolId: String = "",
    @SerializedName("student_id") val studentId: String = "",
    @SerializedName("teacher_id") val teacherId: String = "",
    @SerializedName("dailylogtemplate_id") val dailyLogTemplateId: String = "",
    @SerializedName("is_present") val isPresent: Boolean = true,
    @SerializedName("entries") val entries: List<DailyLogEntry> = emptyList(),
    @SerializedName("attachments") val attachments: List<Any> = emptyList(),
    @SerializedName("read_at") val readAt: String? = null,
    @SerializedName("observation") val observation: String = "",
    @SerializedName("date") val date: String = "",
    @SerializedName("ativo") val ativo: Boolean = true
)

data class DailyLogEntry(
    @SerializedName("_id") val id: String = "",
    @SerializedName("field_key") val fieldKey: String = "",
    @SerializedName("value") val value: String = ""
)

