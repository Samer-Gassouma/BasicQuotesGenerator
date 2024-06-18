package com.example.all_in

sealed class AuthState {
    object LoggedOut : AuthState()
    object LoggedIn : AuthState()
    data class LoggedOutWithError(val error: String?) : AuthState()
    object Loading : AuthState()
}
