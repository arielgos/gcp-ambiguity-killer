package org.arielgos.ambiguitykiller.audio

import android.content.Context
import android.os.ParcelFileDescriptor
import android.util.Log
import com.google.mlkit.genai.common.audio.AudioSource
import com.google.mlkit.genai.speechrecognition.SpeechRecognition
import com.google.mlkit.genai.speechrecognition.SpeechRecognizerOptions
import com.google.mlkit.genai.speechrecognition.SpeechRecognizerRequest
import com.google.mlkit.genai.speechrecognition.SpeechRecognizerResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

class MLKitSpeechManager(private val context: Context) {

    private val speechRecognizer = SpeechRecognition.getClient(
        SpeechRecognizerOptions.builder().apply {
            locale = Locale.US
            preferredMode = SpeechRecognizerOptions.Mode.MODE_ADVANCED
        }.build()
    )

    private var recognitionJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    fun transcribeFile(file: File, onResult: (String) -> Unit, onError: (Throwable) -> Unit) {
        recognitionJob?.cancel()

        val pfd = try {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        } catch (e: Exception) {
            onError(e)
            return
        }

        val audioSource = AudioSource.fromPfd(pfd)
        val request = SpeechRecognizerRequest.builder().apply {
            this.audioSource = audioSource
        }.build()

        recognitionJob = scope.launch {
            try {
                speechRecognizer.startRecognition(request).collect { response ->
                    when (response) {
                        is SpeechRecognizerResponse.PartialTextResponse -> {
                            Log.d("MLKitSpeechManager", "Partial: ${response.text}")
                            onResult(response.text)
                        }

                        is SpeechRecognizerResponse.FinalTextResponse -> {
                            Log.d("MLKitSpeechManager", "Final: ${response.text}")
                            onResult(response.text)
                        }

                        is SpeechRecognizerResponse.ErrorResponse -> {
                            Log.e("MLKitSpeechManager", "Error: ${response.e}")
                            onError(response.e)
                        }

                        else -> {
                            Log.d("MLKitSpeechManager", "Other response: $response")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MLKitSpeechManager", "Exception during recognition", e)
                onError(e)
            } finally {
                try {
                    pfd.close()
                } catch (e: Exception) {
                    Log.e("MLKitSpeechManager", "Error closing PFD", e)
                }
            }
        }
    }

    fun stopRecognition() {
        recognitionJob?.cancel()
        scope.launch {
            try {
                speechRecognizer.stopRecognition()
            } catch (e: Exception) {
                Log.e("MLKitSpeechManager", "Error stopping recognition", e)
            }
        }
    }

    fun close() {
        stopRecognition()
        speechRecognizer.close()
        scope.cancel()
    }
}
