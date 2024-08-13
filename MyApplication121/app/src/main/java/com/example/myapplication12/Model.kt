package com.example.myapplication12

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    @Json(name = "token") val token: String,
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "email") val email: String,
    @Json(name = "enabled") val enabled: Boolean,
    @Json(name = "admin") val admin: Boolean
)

@JsonClass(generateAdapter = true)
data class User1(
    @Json(name = "token") val token: String,
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "email") val email: String,
    @Json(name = "enabled") val enabledRaw: Int,
    @Json(name = "admin") val adminRaw: Int
) {
    val enabled: Boolean
        get() = enabledRaw == 1
    val admin: Boolean
        get() = adminRaw == 1
}

@JsonClass(generateAdapter = true)
data class UserLoginRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class UserRegistrationRequest(
    @Json(name = "name") val username: String,
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class UserAuthResponse(
    val user: User,
    val message: String? = null
)

@JsonClass(generateAdapter = true)
data class TodoUpdateRequest(
    val id: Int,
    val description: String,
    @Json(name = "completed") val isCompleted: Boolean,
    val meta: Map<String, Any> = emptyMap()
)

@JsonClass(generateAdapter = true)
data class TodoCreateRequest(
    val description: String,
    val completed: Boolean,
    val meta: Map<String, Any> = emptyMap()
)
@JsonClass(generateAdapter = true)
data class ResponseTodo(
    @Json(name = "id") val id: Int,
    @Json(name = "description") val description: String,
    @Json(name = "completed") val completedRaw: Boolean,
    @Json(name = "meta") val meta: Map<String, Any>?
)
@JsonClass(generateAdapter = true)
data class ResponseTodoupdate(
    @Json(name = "id") val id: Int,
    @Json(name = "description") val description: String,
    @Json(name = "completed") val completedRaw: Boolean,
)
@JsonClass(generateAdapter = true)
data class TodoItem(
    @Json(name = "id") val id: Int,
    @Json(name = "user_id") val userId: Int,
    @Json(name = "description") val description: String,
    @Json(name = "completed") val completedRaw: Int,
    @Json(name = "author") val author: String,
    @Json(name = "meta") val meta: Map<String, Any>?
)
