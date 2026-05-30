package org.arielgos.ambiguitykiller.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

class VoiceRecorder(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null

    fun startRecording() {
        audioFile = File(context.cacheDir, "recording.m4a")
        
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(audioFile?.absolutePath)
            
            try {
                prepare()
                start()
                Log.d("VoiceRecorder", "Recording started")
            } catch (e: IOException) {
                Log.e("VoiceRecorder", "prepare() failed", e)
            }
        }
    }

    fun stopRecording(): File? {
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            Log.d("VoiceRecorder", "Recording stopped: ${audioFile?.absolutePath}")
            audioFile
        } catch (e: Exception) {
            Log.e("VoiceRecorder", "stopRecording() failed", e)
            null
        }
    }
}
