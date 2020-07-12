package com.github.hattamaulana.taa

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

const val BASE_URL = "https://todo.dhanifudin.com"

interface Service {
    @GET("/{name}")
    fun get(@Path("name") name: String): Call<String>

    @POST("/")
    @FormUrlEncoded
    fun upload(@Part image: MultipartBody.Part): Call<String>
}

object ServiceGenerator {
    var retrofit: Retrofit? = null

    private val builder = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create())

    private val logging = HttpLoggingInterceptor()
        .setLevel(HttpLoggingInterceptor.Level.BODY)

    private val httpClient = OkHttpClient.Builder()

    fun createService(): Service? {
        httpClient.addInterceptor(logging)
        builder.client(httpClient.build())
        retrofit = builder.build()

        return retrofit?.create(Service::class.java)
    }
}