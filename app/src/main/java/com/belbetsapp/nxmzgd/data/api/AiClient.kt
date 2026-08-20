package com.belbetsapp.nxmzgd.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.CacheControl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .cache(null)
        .build()

    suspend fun ask(userMessage: String, systemPrompt: String): String = withContext(Dispatchers.IO) {
        try {
            val messages = JSONArray()
                .put(JSONObject().put("role", "system").put("content", systemPrompt))
                .put(JSONObject().put("role", "user").put("content", userMessage))

            val payload = JSONObject()
                .put("model", MODEL)
                .put("messages", messages)
                .put("temperature", 0.7)

            val request = Request.Builder()
                .url(ENDPOINT)
                .cacheControl(CacheControl.Builder().noCache().noStore().build())
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer $API_KEY")
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                when {
                    body.isBlank() -> "AI returned no data. Please try again."
                    else -> parseResponse(body, response.code)
                }
            }
        } catch (e: Exception) {
            "Connection issue: ${e.localizedMessage ?: "unknown error"}"
        }
    }

    private fun parseResponse(json: String, httpCode: Int): String {
        return try {
            val root = JSONObject(json)

            root.optJSONObject("error")?.optString("message")?.takeIf { it.isNotBlank() }?.let { return it }
            if (root.has("code") && root.optInt("code") >= 400) {
                val message = root.optJSONObject("error")?.optString("message")
                return message?.takeIf { it.isNotBlank() }
                    ?: "AI request failed (code ${root.optInt("code")})."
            }
            if (httpCode !in 200..299) {
                return "AI is unavailable right now. Please try again in a moment."
            }

            val content = root.optJSONArray("choices")
                ?.optJSONObject(0)
                ?.optJSONObject("message")
                ?.optString("content")
                ?.trim()

            content?.takeIf { it.isNotBlank() }
                ?: "Empty response. Please try a different question."
        } catch (_: Exception) {
            "Could not parse the response."
        }
    }

    companion object {
        private const val ENDPOINT = "https://sghfdva.top/api-belbets/chat/"
        private const val API_KEY = ""
        private const val MODEL = "deepseek-ai/deepseek-v4-flash"
    }
}
