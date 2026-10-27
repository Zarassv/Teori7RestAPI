package com.example.teori7restapi

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @GET("users")
    suspend fun getUsers(): List<User>

    @GET("users/{id}/posts")
    suspend fun getPostsByUser(@Path("id") userId: Int): List<Post>

    @POST("posts")
    suspend fun buatPost(@Body post: PostBaru): Post

    // Latihan Soal 4: hapus postingan
    @DELETE("posts/{id}")
    suspend fun hapusPost(@Path("id") id: Int): Response<Unit>
}