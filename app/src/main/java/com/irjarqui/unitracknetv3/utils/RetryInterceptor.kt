package com.irjarqui.unitracknetv3.utils

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class RetryInterceptor(
    private val maxRetries: Int = 2,
    private val delayMs: Long = 1_000,
    private val retryHttpCodes: Set<Int> = setOf(500, 502, 503, 504)
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 0
        var lastException: IOException? = null

        while (attempt <= maxRetries) {
            try {
                val response = chain.proceed(chain.request())
                if (response.code !in retryHttpCodes) {
                    return response
                } else {
                    response.close()
                }
            } catch (e: IOException) {
                lastException = e
            }

            attempt++
            if (attempt > maxRetries) break
            Thread.sleep(delayMs * attempt)
        }

        throw lastException ?: IOException("RetryInterceptor: solicitud fallida tras $maxRetries reintentos")
    }
}