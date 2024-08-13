package com.example.myapplication12

import retrofit2.Response
import retrofit2.http.*
import androidx.compose.runtime.compositionLocalOf

val LocalApiService = compositionLocalOf<ApiService> {
    error("ApiService not provided")
}

interface ApiService {

    @POST("/api/users/register")
    suspend fun registerUser(
        @Query("apikey") apiKey: String,
        @Body user: UserRegistrationRequest
    ): Response<User>

    @POST("/api/users/login")
    suspend fun loginUser(
        @Query("apikey") apiKey: String,
        @Body user: UserLoginRequest
    ): Response<User1>

    @GET("/api/users/{user_id}/todos")
    suspend fun getUserTodos(
        @Path("user_id") userId: Int,
        @Query("apikey") apiKey: String,
        @Header("Authorization") authHeader: String
    ): Response<List<TodoItem>>

    @POST("/api/users/{user_id}/todos")
    suspend fun createTodo(
        @Path("user_id") userId: Int,
        @Query("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Body todo: TodoCreateRequest
    ): Response<ResponseTodo>

    @PUT("/api/users/{user_id}/todos/{id}")
    suspend fun updateTodo(
        @Path("user_id") userId: Int,
        @Path("id") todoId: Int,
        @Query("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Body todo: TodoUpdateRequest
    ): Response<ResponseTodoupdate>
}
