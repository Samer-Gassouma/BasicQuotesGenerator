package com.example.all_in.api

import retrofit2.http.GET
import retrofit2.http.Path

interface QuotableApi {
    @GET("/random")
    suspend fun getRandomQuote(): QuoteResponse
    @GET("/quotes/{id}")
    suspend fun getQuoteById(@Path("id") id: String): QuoteResponse}
