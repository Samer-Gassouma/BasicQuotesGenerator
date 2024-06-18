package com.example.all_in

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth


class MainActivity : ComponentActivity() {
    private val viewModel: AuthViewModel by viewModels()
    companion object {
        private const val TAG = "KotlinActivity"
    }



    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val quoteViewModel: QuoteViewModel by viewModels() // Get the QuoteViewModel instance
        val viewModel1 = ViewModelProvider(this).get(QuoteViewModel::class.java)
        setContent {
            val auth = FirebaseAuth.getInstance()


            val navController = rememberNavController()
            val startDestination = if (auth.currentUser != null) "home" else "login"

            NavHost(navController = navController, startDestination = startDestination) {
                composable("login") {
                    LoginScreen(viewModel = viewModel, onNavigateToRegister = { navController.navigate("register") })
                }
                composable("register") {
                    RegisterScreen(viewModel = viewModel, onNavigateToLogin = { navController.navigate("login") })
                }
                composable("home") {
                    //HomeScreen(viewModel = viewModel, onLogout = { viewModel.logout() })
                    QuoteScreen(viewModel, viewModel1, onLogout = { viewModel.logout() },{ navController.navigate("favorites") })
                }
                composable("favorites") {
                    FavoritesScreen(quoteViewModel) // Pass quoteIds
                }

            }
        }
    }



}
