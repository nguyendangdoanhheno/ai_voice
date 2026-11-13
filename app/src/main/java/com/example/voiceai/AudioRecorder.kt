package com.example.voiceai

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class AudioRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    var currentOutputFile: File? = null
        private set
    var isRecording: Boolean = false
        private set

    fun startRecording(): File {
        val outputDir = context.cacheDir
        val file = File(outputDir, "voice_record_${System.currentTimeMillis()}.mp4")
        currentOutputFile = file

        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128000)
            setAudioSamplingRate(44100)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        isRecording = true
        return file
    }

    fun stopRecording(): File? {
        if (!isRecording) return null
        return try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            isRecording = false
            currentOutputFile
        } catch (e: Exception) {
            e.printStackTrace()
            recorder = null
            isRecording = false
            null
        }
    }
}
