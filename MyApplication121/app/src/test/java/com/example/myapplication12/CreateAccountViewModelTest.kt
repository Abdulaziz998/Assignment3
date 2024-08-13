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
import kotlinx.coroutines.Dispatchers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*

@ExperimentalCoroutinesApi
class CreateAccountViewModelTest {
    private lateinit var viewModel: CreateAccountViewModel
    private val apiService = mockk<ApiService>(relaxed = true)
    private val navController = mockk<NavController>(relaxed = true)
    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CreateAccountViewModel(apiService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `createAccount should succeed with valid data`() = runTest {
        val userRequest = UserRegistrationRequest("Jane Doe", "jane@example.com", "password123")
        coEvery { apiService.registerUser(any(), userRequest) } returns Response.success(User("token123", 1, "Jane Doe", "jane@example.com", true, false))

        viewModel.createAccount("Jane Doe", "jane@example.com", "password123",navController)

        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
        verify { navController.navigate("LoginScreen") }
    }

    @Test
    fun `createAccount should fail with invalid data`() = runTest {
        val userRequest = UserRegistrationRequest("Jane Doe", "jane@example.com", "")
        val errorResponse = "".toResponseBody("application/json".toMediaType())
        coEvery { apiService.registerUser(any(), userRequest) } returns Response.error(400, errorResponse)

        viewModel.createAccount("Jane Doe", "jane@example.com", "password",navController)

        assertFalse(viewModel.isLoading.value)
        assertNotNull(viewModel.error.value)
        assertEquals("Account creation failed:  ", viewModel.error.value)
        verify(exactly = 0) { navController.navigate("LoginScreen") }
    }
}