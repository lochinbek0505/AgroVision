package uz.falconmobile.agrovision

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import uz.falconmobile.agrovision.databinding.ActivityChatBinding
import java.io.IOException

class ChatActivity : AppCompatActivity() {


    private val client = OkHttpClient()

    // 👇 BU YERGA O'ZINGIZNING GEMINI API KALITINI QO'YING
    private val API_KEY = "AIzaSyA5TZlLHHbmvO0JloXOzzhh2tyKpNb0tJQ"

    private lateinit var binding: ActivityChatBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var kasallik=intent.getStringExtra("kasal")
        val prompt = "$kasallik kasalligini qanday aniqlash va davolash mumkin? Ushbu kasallikka qarshi qanday biologik, kimyoviy yoki agrotekhnologik choralar tavsiya etiladi? Davolashda qanday fungitsidlar, insektitsidlar yoki organik vositalar samarali hisoblanadi? Qanday profilaktika choralarini ko‘rish kerak?\n"
        Log.e("test", kasallik.toString())

        sendPromptToGemini(prompt)

        binding.inputLayout.setOnClickListener {

            finish()

        }
    }

    private fun sendPromptToGemini(prompt: String) {
        val url =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$API_KEY"

        val json = JSONObject().apply {
            put(
                "contents",
                JSONArray().put(
                    JSONObject().put(
                        "parts",
                        JSONArray().put(JSONObject().put("text", prompt))
                    )
                )
            )
        }

        val requestBody =
            RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { binding.tvResponse.text = "Xatolik: ${e.message}" }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                val jsonResponse = JSONObject(responseData ?: "")
                val content = jsonResponse
                    .optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text") ?: "Javob topilmadi"

                runOnUiThread {
                    binding.tvResponse.text = content
                }
            }
        })
    }
}