package com.example.chatapplication.`interface`

import com.example.chatapplication.Constants.Constants.Companion.CONTENT_TYPE
import com.example.chatapplication.Constants.Constants.Companion.SERVER_KEY
import com.example.chatapplication.model.PushNotification
import retrofit2.Response
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationApi {
    @Headers("Authorization : key = $SERVER_KEY","Content-type : $CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(
        @Body notification: PushNotification
    ):Response<ResponseBody>
}