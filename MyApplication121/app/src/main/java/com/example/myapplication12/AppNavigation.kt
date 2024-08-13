package com.example.myapplication12

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun AppNavigation(navController: NavHostController) {

    NavHost(navController = navController, startDestination = "LoginScreen") {
        composable("LoginScreen") {
            val loginViewModel = LoginViewModel(LocalApiService.current,LocalContext.current)
            LoginScreen(navController, loginViewModel)
        }
        composable("CreateAccountScreen") {
            val createAccountViewModel = CreateAccountViewModel(LocalApiService.current)
            CreateAccountScreen(navController, createAccountViewModel)
        }
        composable("TodoList") {
            val todoListViewModel = TodoListViewModel(LocalApiService.current,LocalContext.current)
            TodoListScreen(navController, todoListViewModel)
        }
        composable("AddTodo") {
            val todoListViewModel = TodoListViewModel(LocalApiService.current,LocalContext.current)
            AddTodoScreen(navController, todoListViewModel)
        }
    }
}