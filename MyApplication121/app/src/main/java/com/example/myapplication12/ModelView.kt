package com.example.myapplication12

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

import retrofit2.HttpException
import java.io.IOException

class LoginViewModel(private val apiService: ApiService, context: Context) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val apiKey = "ec4eac8a-4de2-44f9-8680-3ec3f118dcfe"

    fun login(email: String, password: String, navController: NavController) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = apiService.loginUser(apiKey, UserLoginRequest(email, password))
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    with(sharedPreferences.edit()) {
                        putString("user_token", user.token)
                        putInt("user_id", user.id)
                        putString("user_name", user.name)
                        putString("user_email", user.email)
                        putBoolean("user_enabled", user.enabled)
                        putBoolean("user_admin", user.admin)
                        apply()
                    }
                    navController.navigate("TodoList")
                    _error.value = null
                } else {
                    _error.value = "Login failed: ${response.message()} ${response.errorBody()?.string()}"
                }
            } catch (e: HttpException) {
                _error.value = "HTTP error: ${e.response()?.errorBody()?.string()}"
            } catch (e: IOException) {
                _error.value = "Network error: ${e.localizedMessage}"
            } catch (e: Exception) {
                _error.value = "An error occurred: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

class CreateAccountViewModel(private val apiService: ApiService) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val apiKey = "ec4eac8a-4de2-44f9-8680-3ec3f118dcfe"

    fun createAccount(name: String, email: String, password: String, navController: NavController) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = apiService.registerUser(apiKey, UserRegistrationRequest(name, email, password))
                if (response.isSuccessful && response.body() != null) {
                    _error.value = null
                    navController.navigate("LoginScreen")
                } else {
                    _error.value = "Account creation failed: ${response.message()} ${response.errorBody()?.string()}"
                }
            } catch (e: HttpException) {
                _error.value = "HTTP error: ${e.localizedMessage}"
            } catch (e: IOException) {
                _error.value = "Network error: ${e.localizedMessage}"
            } catch (e: Exception) {
                _error.value = "An error occurred: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}


class TodoListViewModel(private val apiService: ApiService, context: Context) : ViewModel() {
    private val _todos = MutableStateFlow<List<TodoItem>>(emptyList())
    val todos: StateFlow<List<TodoItem>> = _todos
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    private val context1 = context
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private fun getUserId(): Int {
        return sharedPreferences.getInt("user_id", -1)
    }

    private fun getUserToken(): String? {
        return sharedPreferences.getString("user_token", null)
    }

    private val apiKey = "ec4eac8a-4de2-44f9-8680-3ec3f118dcfe"

    fun loadTodos() {
        val userId = getUserId()
        val token = getUserToken()
        if (userId == -1 || token == null) {
            _error.value = "User ID or token not found. Please log in again."
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = apiService.getUserTodos(userId, apiKey, "Bearer $token")
                if (response.isSuccessful) {
                    _todos.value = response.body() ?: emptyList()
                    _error.value = null
                } else {
                    _error.value = "Failed to fetch todos: ${response.message()} ${response.errorBody()?.string()}"
                }
            } catch (e: HttpException) {
                _error.value = "HTTP error: ${e.response()?.errorBody()?.string()}"
            } catch (e: IOException) {
                _error.value = "Network error: ${e.localizedMessage}"
            } catch (e: Exception) {
                _error.value = "An error occurred: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addTodo(description: String, navController: NavController) {
        val userId = getUserId()
        val token = getUserToken()
        if (userId == -1 || token == null) {
            _error.value = "User ID or token not found. Please log in again."
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val request = TodoCreateRequest(description, completed = false, meta = emptyMap())
                Log.d("AddTodoRequest", request.toString())
                val response = apiService.createTodo(userId, apiKey, "Bearer $token", request)
                Log.d("AddTodoResponse", response.toString())
                if (response.isSuccessful) {
                    _error.value = null
                    navController.navigate("TodoList")
                } else {
                    _error.value = "Failed to create todo: ${response.message()} ${response.errorBody()?.string()}"
                }
            } catch (e: HttpException) {
                _error.value = "HTTP error: ${e.response()?.errorBody()?.string()}"
            } catch (e: IOException) {
                _error.value = "Network error: ${e.localizedMessage}"
            } catch (e: Exception) {
                _error.value = "An error occurred: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTodo(id: Int, description: String, isCompleted: Boolean) {
        val userId = getUserId()
        val token = getUserToken()
        if (userId == -1 || token == null) {
            _error.value = "User ID or token not found. Please log in again."
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val request = TodoUpdateRequest(id, description, isCompleted, meta = emptyMap())
                val response = apiService.updateTodo(userId, id, apiKey, "Bearer $token", request)
                if (response.isSuccessful) {
                    _error.value = null
                } else {
                    _error.value = "Failed to update todo: ${response.message()} ${response.errorBody()?.string()}"

                }

                Log.d("error finder",_error.value.toString())
                loadTodos()
            } catch (e: HttpException) {
                _error.value = "HTTP error: ${e.response()?.errorBody()?.string()}"
            } catch (e: IOException) {
                _error.value = "Network error: ${e.localizedMessage}"
            } catch (e: Exception) {
                _error.value = "An error occurred: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}