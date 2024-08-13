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
class LoginViewModelTest {
    private lateinit var viewModel: LoginViewModel
    private val apiService = mockk<ApiService>(relaxed = true)
    private val navController = mockk<NavController>(relaxed = true)
    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(apiService, mockk(relaxed = true))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `login should succeed when credentials are correct`() = testDispatcher.runBlockingTest {
        val email = "user@example.com"
        val password = "password123"
        val userResponse = User1("token123", 1, "John Doe", email, 1, 1)

        coEvery { apiService.loginUser(any(), UserLoginRequest(email, password)) } returns Response.success(userResponse)

        viewModel.login(email, password, navController)

        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
        verify { navController.navigate("TodoList") }
    }

    @Test
    fun `login should fail when API returns an error`() = testDispatcher.runBlockingTest {
        val email = "user@example.com"
        val password = "wrongpassword"
        val responseBody = "{\"message\":\"Invalid credentials\"}".toResponseBody("application/json".toMediaType())

        coEvery { apiService.loginUser(any(), UserLoginRequest(email, password)) } returns Response.error(401, responseBody)

        viewModel.login(email, password, navController)

        assertFalse(viewModel.isLoading.value)
        assertNotNull(viewModel.error.value)
        assertEquals("Login failed: Response.error() {\"message\":\"Invalid credentials\"}", viewModel.error.value)
    }
}
