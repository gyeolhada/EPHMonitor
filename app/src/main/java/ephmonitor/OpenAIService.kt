package com.example.ephmonitor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

class OpenAIService(private val apiKey: String) {
    // 配置OkHttpClient，添加timeout和重试机制
    private val client = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS) // 延长读取超时
        .build()
    
    private val baseUrl = "https://api.deepseek.com/chat/completions"  // DeepSeek API 地址

    fun getFitnessAdvice(
        heartRate: List<Float>,
        breathRate: List<Float>,
        caloriesBurned: Float,
        exercise_type: String,
        callback: (String) -> Unit
    ) {
        // 构造请求体
        val messages = org.json.JSONArray().apply {
            put(org.json.JSONObject().apply {
                put("role", "system")
                put("content", "You are a fitness expert. Provide personalized advice based on the user's exercise data and exercise type. Please speak in Chinese")
            })
            put(org.json.JSONObject().apply {
                put("role", "user")
                put("content", "Exercise data this time: heartRate = $heartRate, breathRate = $breathRate, Calories burned = $caloriesBurned, exercise_type = $exercise_type. Give me fitness advice.")
            })
        }
        val requestBody = org.json.JSONObject().apply {
            put("model", "deepseek-chat")
            put("stream", false)
            put("messages", messages)
            put("max_tokens", 500)  // 限制响应长度，减少处理时间
            put("temperature", 0.7) // 控制响应创造性
        }.toString()

        // 构造 HTTP 请求
        val request = Request.Builder()
            .url(baseUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("User-Agent", "EPHMonitor/1.0")  // 添加User-Agent
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        // 异步执行请求
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                val errorMessage = when {
                    e.message?.contains("timeout", ignoreCase = true) == true -> 
                        "请求超时，请检查网络连接"
                    e.message?.contains("connect", ignoreCase = true) == true -> 
                        "连接失败，请检查网络设置"
                    else -> "网络错误: ${e.message}"
                }
                callback("Error: $errorMessage")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                try {
                    if (!response.isSuccessful) {
                        val errorMessage = when (response.code) {
                            401 -> "API密钥无效或已过期"
                            429 -> "请求过于频繁，请稍后再试"
                            500, 502, 503, 504 -> "服务器错误，请稍后再试"
                            else -> "API错误: ${response.code} - ${response.message}"
                        }
                        callback("Error: $errorMessage")
                        return
                    }

//                    val responseBody = response.body?.string()
//                    if (responseBody.isNullOrEmpty()) {
//                        callback("Error: 服务器返回空响应")
//                        return
//                    }
                    val responseBody = response.body?.source()?.use { source ->
                        source.readString(Charset.defaultCharset())
                    }

                    val jsonResponse = JSONObject(responseBody)

                    // 检查是否存在 "choices" 字段
                    if (!jsonResponse.has("choices")) {
                        callback("API响应格式错误: 缺少'choices'字段")
                        return
                    }

                    val choices = jsonResponse.getJSONArray("choices")
                    if (choices.length() == 0) {
                        callback("API响应格式错误: 'choices'数组为空")
                        return
                    }

                    val advice = choices
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                    callback(advice)
                } catch (e: Exception) {
                    callback("JSON解析错误: ${e.message}")
                } finally {
                    response.close()  // 确保响应被关闭
                }
            }
        })
    }
}