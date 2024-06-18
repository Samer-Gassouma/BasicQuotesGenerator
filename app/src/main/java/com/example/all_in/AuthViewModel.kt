package com.example.all_in

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.all_in.api.QuoteResponse
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@SuppressLint("StaticFieldLeak")
class AuthViewModel(
) : ViewModel() {
    private val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()
    private val firestore :FirebaseFirestore
        get() = FirebaseFirestore.getInstance()


    private val _authState = MutableStateFlow<AuthState>(AuthState.LoggedOut)
    val authState: StateFlow<AuthState> = _authState.asStateFlow() // use asStateFlow to expose as read-only
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "UserInfo")


    fun login(email: String, password: String) = viewModelScope.launch {
        try {
            _authState.value = AuthState.Loading
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        saveUserData(email = email, userId = auth.currentUser?.uid ?: "")
                    } else {
                        Log.e("Login", "Error occurred while logging in", task.exception)
                    }
                }

            _authState.value = AuthState.LoggedIn

        } catch (e: Exception) {
            _authState.value = AuthState.LoggedOutWithError(e.message)
        }
    }



    fun logout() = viewModelScope.launch {
        auth.signOut()
        _authState.value = AuthState.LoggedOut
    }

    fun register(email: String, password: String) = viewModelScope.launch {
        try {
            _authState.value = AuthState.Loading
            auth.createUserWithEmailAndPassword(email, password).await()
            _authState.value = AuthState.LoggedIn
        } catch (e: Exception) {
            _authState.value = AuthState.LoggedOutWithError(e.message)
        }
    }

    fun forgotPassword(email: String) = viewModelScope.launch {
        try {
            _authState.value = AuthState.Loading
            auth.sendPasswordResetEmail(email).await()
            _authState.value = AuthState.LoggedOut
        } catch (e: Exception) {
            _authState.value = AuthState.LoggedOutWithError(e.message)
        }
    }

    fun checkLoggedIn() {
        if (auth.currentUser != null) {
            _authState.value = AuthState.LoggedIn
        }
    }


    private fun saveUserData(userId: String, email: String) {
        val userData = UserData(userId, email)
        val userRef = firestore.collection("users").document(userId)

        userRef.set(userData)
            .addOnSuccessListener {
                // User data saved successfully
                Log.i("UserData", "User data saved successfully")
            }
            .addOnFailureListener {
                // Error occurred while saving user data
                Log.e("UserData", "Error occurred while saving user data", it)
            }
    }


    fun addToFavorites(id : String) = viewModelScope.launch {
        val userId = auth.currentUser?.uid ?: return@launch
        val userRef = firestore.collection("users").document(userId)
        val userSnapshot = userRef.get().await()
        val favoriteQuotes = userSnapshot.getString("favoriteQuotes")?.split(",")?.toMutableList() ?: mutableListOf()
        favoriteQuotes.add(id)
        userRef.update("favoriteQuotes", favoriteQuotes.joinToString(","))
    }

    fun removeFromFavorites(id : String) = viewModelScope.launch {
        val userId = auth.currentUser?.uid ?: return@launch
        val userRef = firestore.collection("users").document(userId)
        val userSnapshot = userRef.get().await()
        val favoriteQuotes = userSnapshot.getString("favoriteQuotes")?.split(",")?.toMutableList() ?: mutableListOf()
        favoriteQuotes.remove(id)
        userRef.update("favoriteQuotes", favoriteQuotes.joinToString(","))
    }
}
