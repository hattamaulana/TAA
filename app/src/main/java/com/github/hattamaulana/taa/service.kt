package com.github.hattamaulana.taa

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

const val BASE_URL = "http://192.168.43.136:8000"

interface Service {
    @POST("/api")
    @Multipart
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