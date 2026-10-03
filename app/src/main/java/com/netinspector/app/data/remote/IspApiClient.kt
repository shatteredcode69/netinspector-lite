package com.netinspector.app.data.remote

import com.netinspector.app.data.model.IspResponseDto
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class IspApiClient {
    private val client = OkHttpClient()

    fun fetch(): IspResponseDto {
        val request = Request.Builder().url("https://ipinfo.io/json").build()
        client.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "ISP lookup failed (${response.code})" }
            val json = JSONObject(response.body?.string().orEmpty())
            return IspResponseDto(json.optString("ip"), json.optString("org"), json.optString("city"), json.optString("region"), json.optString("country"), json.optString("asn"))
        }
    }
}