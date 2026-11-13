package com.example.voiceai

import android.content.Context
import android.os.Environment
import java.io.File

object AppConfig {
    private const val PREFS_NAME = "voice_ai_config"
    private const val KEY_API_KEY = "api_key"
    private const val KEY_SERVER_URL = "server_url"

    // Mặc định server AI (ví dụ: Google Gemini API Audio hoặc OpenAI Whisper hoặc custom Server)
    const val DEFAULT_SERVER_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"

    // Thư mục lưu file cấu hình config.txt ở bộ nhớ ngoài của App để người dùng dễ sửa file trực tiếp
    fun getConfigFile(context: Context): File {
        val dir = context.getExternalFilesDir(null) ?: context.filesDir
        return File(dir, "api_config.txt")
    }

    /**
     * Tự động đọc API key:
     * 1. Ưu tiên đọc từ file api_config.txt ngoài bộ nhớ máy
     * 2. Nếu không có file thì đọc từ SharedPreferences đã lưu trên UI
     */
    fun getApiKey(context: Context): String {
        try {
            val file = getConfigFile(context)
            if (file.exists()) {
                val lines = file.readLines()
                for (line in lines) {
                    val trimmed = line.trim()
                    if (trimmed.startsWith("API_KEY=")) {
                        val key = trimmed.substringAfter("API_KEY=").trim()
                        if (key.isNotEmpty()) return key
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_API_KEY, "") ?: ""
    }

    fun getServerUrl(context: Context): String {
        try {
            val file = getConfigFile(context)
            if (file.exists()) {
                val lines = file.readLines()
                for (line in lines) {
                    val trimmed = line.trim()
                    if (trimmed.startsWith("SERVER_URL=")) {
                        val url = trimmed.substringAfter("SERVER_URL=").trim()
                        if (url.isNotEmpty()) return url
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL
    }

    fun saveConfig(context: Context, apiKey: String, serverUrl: String) {
        // Lưu SharedPreferences
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_API_KEY, apiKey)
            .putString(KEY_SERVER_URL, serverUrl)
            .apply()

        // Đồng bộ ra file txt ngoài để người dùng có thể mở bằng File Manager chỉnh sửa bất kỳ lúc nào
        try {
            val file = getConfigFile(context)
            val content = "# File cau hinh Voice AI - Ban co the thay the API_KEY tai day khi het token\n" +
                    "API_KEY=$apiKey\n" +
                    "SERVER_URL=$serverUrl\n"
            file.writeText(content)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
