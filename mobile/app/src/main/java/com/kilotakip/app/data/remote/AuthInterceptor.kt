package com.kilotakip.app.data.remote

import com.kilotakip.app.data.datastore.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Her isteğe kayıtlı token'ı ekler. Sunucu 401 dönerse (admin oturumu sonlandırmış
 * veya token geçersizse) yerel oturumu temizler; bir sonraki ekran açılışında
 * kullanıcı login ekranına yönlendirilir.
 */
class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { sessionManager.getToken() }

        val request = chain.request().newBuilder().apply {
            if (!token.isNullOrBlank()) {
                addHeader("Authorization", "Bearer $token")
            }
            addHeader("Accept", "application/json")
        }.build()

        val response = chain.proceed(request)

        if (response.code == 401) {
            runBlocking { sessionManager.clearSession() }
        }

        return response
    }
}
