package com.sandip.skinglance

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface SkinCanApi {
    @Multipart
    @POST("/predict")
    fun uploadImage(@Part image: MultipartBody.Part): Call<List<String>>
}
