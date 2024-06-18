package com.example.all_in.api


sealed class NetworkRepsonse<out T> {
    data class Success<out T>(val value: T): NetworkRepsonse<T>()
    data class Error(val message: String): NetworkRepsonse<Nothing>()
    object Loading: NetworkRepsonse<Nothing>()
}
