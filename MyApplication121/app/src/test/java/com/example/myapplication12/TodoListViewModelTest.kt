package com.example.myapplication12

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.After
import org.junit.Test
import retrofit2.Response
import androidx.navigation.NavController
import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*

@ExperimentalCoroutinesApi
class TodoListViewModelTest {
    private lateinit var viewModel: TodoListViewModel
    private val apiService = mockk<ApiService>(relaxed = true)
    private val navController = mockk<NavController>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)
    private val sharedPreferences = mockk<SharedPreferences>(relaxed = true)
    private val testDispatcher = TestCoroutineDispatcher()
    private var authToken: String? = null

    @Before
    fun setUp() = runTest {
        Dispatchers.setMain(testDispatcher)
        coEvery { context.getSharedPreferences(any(), any()) } returns sharedPreferences
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
        val loginResponse = User1("token123", 1, "user11", "user11@mail.com", 1, 0)
        coEvery { apiService.loginUser(any(), UserLoginRequest("user11@mail.com", "123456789")) } returns Response.success(loginResponse)
        coEvery { sharedPreferences.edit().putString("user_token", loginResponse.token).apply() } returns Unit
        authToken = loginResponse.token

        viewModel = TodoListViewModel(apiService, context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }
    @Test
    fun `loadTodos should succeed with valid data`() = runTest {
        val todos = listOf(TodoItem(1, 1, "Task 1", 0, "John Doe", null))
        coEvery { sharedPreferences.getString("user_token", null) } returns authToken
        coEvery { apiService.getUserTodos(any(), any(), any()) } returns Response.success(todos)

        viewModel.loadTodos()

        assertFalse(viewModel.isLoading.value)
        assertEquals(todos, viewModel.todos.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `loadTodos should fail with error`() = runTest {
        val errorResponse = "Failed to load todos".toResponseBody("application/json".toMediaType())
        coEvery { sharedPreferences.getString("user_token", null) } returns authToken
        coEvery { apiService.getUserTodos(any(), any(), any()) } returns Response.error(404, errorResponse)

        viewModel.loadTodos()

        assertFalse(viewModel.isLoading.value)
        assertTrue(viewModel.todos.value.isEmpty())
        assertNotNull(viewModel.error.value)
        assertEquals("Failed to fetch todos: Response.error() Failed to load todos", viewModel.error.value)
    }
    @Test
    fun `addTodo should succeed with valid data`() = runTest {
        val userId = 1
        val description = "New Task"
        val request = TodoCreateRequest(description, completed = false)
        coEvery { sharedPreferences.getInt("user_id", -1) } returns userId
        coEvery { sharedPreferences.getString("user_token", null) } returns authToken
        coEvery { apiService.createTodo(userId, any(), any(), request) } returns Response.success(ResponseTodo(1, description, false, null))

        viewModel.addTodo(description, navController)

        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
        verify { navController.navigate("TodoList") }
    }

    @Test
    fun `addTodo should fail with error`() = runTest {
        val userId = 1
        val description = "New Task"
        val errorResponse = "Failed to create todo".toResponseBody("application/json".toMediaType())
        coEvery { sharedPreferences.getInt("user_id", -1) } returns userId
        coEvery { sharedPreferences.getString("user_token", null) } returns authToken
        coEvery { apiService.createTodo(userId, any(), any(), any()) } returns Response.error(400, errorResponse)

        viewModel.addTodo(description, navController)

        assertFalse(viewModel.isLoading.value)
        assertNotNull(viewModel.error.value)
        assertEquals("Failed to create todo: Response.error() Failed to create todo", viewModel.error.value)
        verify(exactly = 0) { navController.navigate(any<String>()) }
    }
    @Test
    fun `updateTodo should succeed with valid data`() = runTest {
        val userId = 1
        val todoId = 1
        val description = "Updated Task"
        val isCompleted = true
        val request = TodoUpdateRequest(todoId, description, isCompleted)
        val updatedTodos = listOf(TodoItem(todoId, userId, description, if (isCompleted) 1 else 0, "John Doe", null))

        coEvery { sharedPreferences.getInt("user_id", -1) } returns userId
        coEvery { sharedPreferences.getString("user_token", null) } returns authToken
        coEvery { apiService.updateTodo(userId, todoId, any(), any(), request) } returns Response.success(ResponseTodoupdate(todoId, description, isCompleted))
        coEvery { apiService.getUserTodos(userId, any(), any()) } returns Response.success(updatedTodos)

        viewModel.updateTodo(todoId, description, isCompleted)

        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
        assertEquals(updatedTodos, viewModel.todos.value)
    }

    @Test
    fun `updateTodo should fail with error`() = runTest {
        val userId = 1
        val todoId = 1
        val description = "Updated Task"
        val isCompleted = true
        val errorResponse = "Failed to update todo".toResponseBody("application/json".toMediaType())
        coEvery { sharedPreferences.getInt("user_id", -1) } returns userId
        coEvery { sharedPreferences.getString("user_token", null) } returns authToken
        coEvery { apiService.updateTodo(userId, todoId, any(), any(), any()) } returns Response.error(400, errorResponse)

        viewModel.updateTodo(todoId, description, isCompleted)

        assertFalse(viewModel.isLoading.value)
        assertNotNull(viewModel.error.value)
        assertEquals("Failed to fetch todos:  ", viewModel.error.value)
    }
}