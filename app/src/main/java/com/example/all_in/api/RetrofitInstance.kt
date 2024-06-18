package com.example.all_in.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.quotable.io")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val quotableApi:QuotableApi = retrofit.create(QuotableApi::class.java)

}