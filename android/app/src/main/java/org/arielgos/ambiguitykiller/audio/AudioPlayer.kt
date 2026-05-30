package org.arielgos.ambiguitykiller.audio

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import java.io.File
import java.io.IOException

class AudioPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    fun playFile(file: File) {
        stopPlaying()
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(file.absolutePath)
                prepare()
                start()
                Log.d("AudioPlayer", "Playback started")
                setOnCompletionListener {
                    stopPlaying()
                }
            } catch (e: IOException) {
                Log.e("AudioPlayer", "playFile() failed", e)
            }
        }
    }

    fun stopPlaying() {
        mediaPlayer?.release()
        mediaPlayer = null
        Log.d("AudioPlayer", "Playback stopped")
    }
}
