package com.example.voiceai

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.voiceai.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var recorder: AudioRecorder
    private val networkManager = NetworkManager()

    private val requestAudioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            toggleRecording()
        } else {
            Toast.makeText(this, "Cần cấp quyền Microphone để ghi âm", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recorder = AudioRecorder(this)

        loadInitialConfig()

        binding.btnSaveConfig.setOnClickListener {
            val key = binding.etApiKey.text.toString().trim()
            val url = binding.etServerUrl.text.toString().trim()
            AppConfig.saveConfig(this, key, url)
            Toast.makeText(this, "Đã lưu cấu hình thành công!", Toast.LENGTH_SHORT).show()
            updateConfigPathDisplay()
        }

        binding.btnRecord.setOnClickListener {
            checkPermissionAndRecord()
        }
    }

    private fun loadInitialConfig() {
        val apiKey = AppConfig.getApiKey(this)
        val serverUrl = AppConfig.getServerUrl(this)

        binding.etApiKey.setText(apiKey)
        binding.etServerUrl.setText(serverUrl)
        updateConfigPathDisplay()
    }

    private fun updateConfigPathDisplay() {
        val file = AppConfig.getConfigFile(this)
        binding.tvConfigPath.text = "File cấu hình ngoài: ${file.absolutePath}"
    }

    private fun checkPermissionAndRecord() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            toggleRecording()
        } else {
            requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun toggleRecording() {
        if (!recorder.isRecording) {
            // Bắt đầu ghi âm
            try {
                recorder.startRecording()
                binding.tvStatus.text = "🔴 Đang ghi âm giọng nói..."
                binding.tvStatus.setTextColor(Color.parseColor("#FF5252"))
                binding.btnRecord.text = "⏹️"
                binding.btnRecord.setBackgroundColor(Color.parseColor("#D50000"))
            } catch (e: Exception) {
                Toast.makeText(this, "Không thể bắt đầu ghi âm: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Dừng ghi âm và gửi lên server
            val audioFile = recorder.stopRecording()
            binding.tvStatus.text = "Đã dừng. Đang tải âm thanh lên Server AI..."
            binding.tvStatus.setTextColor(Color.parseColor("#80D8FF"))
            binding.btnRecord.text = "🎙️"
            binding.btnRecord.setBackgroundColor(Color.parseColor("#E91E63"))

            if (audioFile != null && audioFile.exists()) {
                sendAudioToServer(audioFile)
            } else {
                Toast.makeText(this, "Không tìm thấy file ghi âm", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendAudioToServer(file: File) {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvAiResult.text = "Đang gửi dữ liệu âm thanh đến Server AI..."

        val apiKey = AppConfig.getApiKey(this)
        val serverUrl = AppConfig.getServerUrl(this)

        lifecycleScope.launch {
            val result = networkManager.sendVoiceToAi(file, serverUrl, apiKey)
            binding.progressBar.visibility = View.GONE

            result.onSuccess { responseText ->
                binding.tvStatus.text = "✅ AI đã xử lý xong!"
                binding.tvStatus.setTextColor(Color.parseColor("#00E676"))
                binding.tvAiResult.text = responseText
            }.onFailure { error ->
                binding.tvStatus.text = "❌ Thất bại!"
                binding.tvStatus.setTextColor(Color.parseColor("#FF5252"))
                binding.tvAiResult.text = "Lỗi: ${error.message}"
            }
        }
    }
}
