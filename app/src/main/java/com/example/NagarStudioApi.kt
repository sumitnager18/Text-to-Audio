package com.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

data class TtsResult(val audioBase64: String, val durationSeconds: Double, val sampleRate: Int)

class NagarStudioApi(private val baseUrl: String) {
    private val client = OkHttpClient()

    suspend fun health(): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder().url("$baseUrl/api/health").get().build()
            client.newCall(request).execute().use { it.isSuccessful }
        }.getOrDefault(false)
    }

    suspend fun generate(text: String, voice: String, emotion: String): TtsResult = withContext(Dispatchers.IO) {
        val json = JSONObject().put("text", text).put("voice", voice).put("emotion", emotion)
        val body = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url("$baseUrl/api/tts/generate").post(body).build()
        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw IllegalStateException(raw.ifBlank { "Nagar Studio request failed: " + response.code })
            val data = JSONObject(raw)
            val audio = data.optString("audioBase64")
            if (audio.isBlank()) throw IllegalStateException("Server returned no audio.")
            TtsResult(audio, data.optDouble("durationSeconds", 0.0), data.optInt("sampleRate", 24000))
        }
    }
}
