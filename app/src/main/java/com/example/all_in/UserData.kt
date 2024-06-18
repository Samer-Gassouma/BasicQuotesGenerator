package com.example.all_in

data class UserData(
    val userId: String = "",
    val email: String = "",

    val favoriteQuotes: List<String> = emptyList(),
)
