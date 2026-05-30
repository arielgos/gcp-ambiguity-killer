package org.arielgos.ambiguitykiller

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import org.arielgos.ambiguitykiller.audio.MLKitSpeechManager
import org.arielgos.ambiguitykiller.audio.VoiceRecorder
import org.arielgos.ambiguitykiller.network.CloudRunRequest
import org.arielgos.ambiguitykiller.network.NetworkClient

class MainActivity : AppCompatActivity() {

    private val messages: MutableList<Message> = mutableListOf()

    private var list: ListView? = null
    private var txtMessage: EditText? = null
    private var btnRecord: FloatingActionButton? = null
    private var btnSend: Button? = null

    private lateinit var voiceRecorder: VoiceRecorder
    private lateinit var mlKitSpeechManager: MLKitSpeechManager
    private val RECORD_AUDIO_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val adapter = MessageAdapter(this@MainActivity, messages)
        list = findViewById(R.id.list)
        list?.setAdapter(adapter)

        txtMessage = findViewById(R.id.message)
        btnRecord = findViewById(R.id.record)
        btnSend = findViewById(R.id.send)

        voiceRecorder = VoiceRecorder(this)
        mlKitSpeechManager = MLKitSpeechManager(this)


        btnRecord?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.parent.requestDisallowInterceptTouchEvent(true)
                    v.performClick()
                    if (checkPermission()) {
                        txtMessage?.setText("")
                        voiceRecorder.startRecording()
                        Toast.makeText(this, "Recording...", Toast.LENGTH_SHORT).show()
                    } else {
                        requestPermission()
                    }
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val audioFile = voiceRecorder.stopRecording()
                    if (audioFile != null) {
                        Toast.makeText(this, "Transcribing...", Toast.LENGTH_SHORT).show()

                        mlKitSpeechManager.transcribeFile(audioFile, onResult = { text ->
                            txtMessage?.setText(text)
                        }, onError = { error ->
                            Log.e("MainActivity", "Transcription error", error)
                            Toast.makeText(this, "Transcription failed", Toast.LENGTH_SHORT).show()
                        })
                    }
                    true
                }

                MotionEvent.ACTION_MOVE -> true

                else -> false
            }
        }

        btnSend?.setOnClickListener(View.OnClickListener {
            val userMessage = txtMessage?.text.toString()
            if (userMessage.isBlank()) return@OnClickListener

            messages.add(Message(userMessage, MessageType.USER))
            txtMessage?.setText("")
            adapter.notifyDataSetChanged()

            val request = CloudRunRequest(
                user = "arielgos", value = userMessage
            )

            lifecycleScope.launch {
                try {
                    val response = NetworkClient.cloudRunService.executeCloudRun(request) // Handle success
                    Log.d(R.string.app_name.toString(), response.toString())
                    messages.add(Message(response.result, MessageType.SYSTEM))
                    adapter.notifyDataSetChanged()
                } catch (e: Exception) { // Handle error
                    Log.e(R.string.app_name.toString(), e.stackTrace.toString(), e)
                }
            }
        })
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mlKitSpeechManager.close()
    }
}
