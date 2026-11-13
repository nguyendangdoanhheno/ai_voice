package com.example.voiceai

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

class NetworkManager {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun sendVoiceToAi(
        audioFile: File,
        serverUrl: String,
        apiKey: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("Chưa thiết lập API Key! Vui lòng nhập API Key hoặc sửa file config."))
            }

            // Phân loại kiểu server dựa trên URL:
            // 1. Google Gemini API
            if (serverUrl.contains("googleapis.com")) {
                return@withContext callGeminiApi(audioFile, serverUrl, apiKey)
            }
            // 2. OpenAI Whisper hoặc server tương thích multipart
            else {
                return@withContext callMultipartServer(audioFile, serverUrl, apiKey)
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }

    private fun callGeminiApi(audioFile: File, serverUrl: String, apiKey: String): Result<String> {
        val audioBytes = audioFile.readBytes()
        val base64Audio = Base64.encodeToString(audioBytes, Base64.NO_WRAP)

        val fullUrl = if (serverUrl.contains("?key=")) serverUrl else "$serverUrl?key=$apiKey"

        val jsonBody = JSONObject().apply {
            val contents = JSONArray().apply {
                val partContainer = JSONObject().apply {
                    val parts = JSONArray().apply {
                        // Prompt hướng dẫn AI
                        put(JSONObject().apply {
                            put("text", "Hãy nghe đoạn ghi âm giọng nói này, phiên âm chuẩn xác sang tiếng Việt và tóm tắt hoặc trả lời trực tiếp nội dung người nói yêu cầu:")
                        })
                        // Dữ liệu audio nhúng base64
                        put(JSONObject().apply {
                            put("inlineData", JSONObject().apply {
                                put("mimeType", "audio/mp4")
                                put("data", base64Audio)
                            })
                        })
                    }
                    put("parts", parts)
                }
                put(partContainer)
            }
            put("contents", contents)
        }

        val request = Request.Builder()
            .url(fullUrl)
            .post(jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val resText = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return Result.failure(Exception("Lỗi Gemini (${response.code}): $resText"))
            }

            try {
                val jsonRes = JSONObject(resText)
                val text = jsonRes.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                return Result.success(text)
            } catch (e: Exception) {
                return Result.success("Kết quả từ AI:\n$resText")
            }
        }
    }

    private fun callMultipartServer(audioFile: File, serverUrl: String, apiKey: String): Result<String> {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                audioFile.name,
                audioFile.asRequestBody("audio/mp4".toMediaType())
            )
            .addFormDataPart("model", "whisper-1")
            .build()

        val request = Request.Builder()
            .url(serverUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            val resText = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return Result.failure(Exception("Lỗi Server (${response.code}): $resText"))
            }
            return Result.success(resText)
        }
    }
}
