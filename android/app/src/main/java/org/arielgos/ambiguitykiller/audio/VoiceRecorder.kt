package org.arielgos.ambiguitykiller.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.util.concurrent.atomic.AtomicBoolean

class VoiceRecorder(private val context: Context) {

    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    private val isRecording = AtomicBoolean(false)
    private var audioFile: File? = null

    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    @SuppressLint("MissingPermission") fun startRecording() {
        if (isRecording.get()) return

        audioFile = File(context.cacheDir, "recording.wav")
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION, sampleRate, channelConfig, audioFormat, minBufferSize
        )

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            Log.e("VoiceRecorder", "AudioRecord initialization failed")
            return
        }

        audioRecord?.startRecording()
        isRecording.set(true)

        recordingThread = Thread({
            writeAudioDataToFile()
        }, "AudioRecordingThread")
        recordingThread?.start()
        Log.d("VoiceRecorder", "Recording started (WAV 16k)")
    }

    private fun writeAudioDataToFile() {
        var os: FileOutputStream? = null
        try {
            os = FileOutputStream(audioFile) // Write placeholder WAV header
            os.write(ByteArray(44))

            val data = ByteArray(minBufferSize)
            while (isRecording.get()) {
                val read = audioRecord?.read(data, 0, minBufferSize) ?: 0
                if (read > 0) {
                    os.write(data, 0, read)
                }
            }

            // Read remaining data after isRecording is set to false
            var readRemaining: Int = 0
            do {
                readRemaining = audioRecord?.read(data, 0, minBufferSize) ?: 0
                if (readRemaining > 0) {
                    os.write(data, 0, readRemaining)
                }
            } while (readRemaining > 0)

            os.flush()
        } catch (e: IOException) {
            Log.e("VoiceRecorder", "Error writing audio data", e)
        } finally {
            try {
                os?.close()
                audioFile?.let { updateWavHeader(it) }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun updateWavHeader(file: File) {
        val totalAudioLen = file.length() - 44
        val totalDataLen = totalAudioLen + 36
        val longSampleRate = sampleRate.toLong()
        val channels = 1
        val byteRate = (16 * sampleRate * channels / 8).toLong()
        val header = ByteArray(44)

        header[0] = 'R'.toByte() // RIFF/WAVE header
        header[1] = 'I'.toByte()
        header[2] = 'F'.toByte()
        header[3] = 'F'.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = (totalDataLen shr 8 and 0xff).toByte()
        header[6] = (totalDataLen shr 16 and 0xff).toByte()
        header[7] = (totalDataLen shr 24 and 0xff).toByte()
        header[8] = 'W'.toByte()
        header[9] = 'A'.toByte()
        header[10] = 'V'.toByte()
        header[11] = 'E'.toByte()
        header[12] = 'f'.toByte() // 'fmt ' chunk
        header[13] = 'm'.toByte()
        header[14] = 't'.toByte()
        header[15] = ' '.toByte()
        header[16] = 16 // 4 bytes: size of 'fmt ' chunk
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // format = 1
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (longSampleRate and 0xff).toByte()
        header[25] = (longSampleRate shr 8 and 0xff).toByte()
        header[26] = (longSampleRate shr 16 and 0xff).toByte()
        header[27] = (longSampleRate shr 24 and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()
        header[32] = (1 * 16 / 8).toByte() // block align
        header[33] = 0
        header[34] = 16 // bits per sample
        header[35] = 0
        header[36] = 'd'.toByte()
        header[37] = 'a'.toByte()
        header[38] = 't'.toByte()
        header[39] = 'a'.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = (totalAudioLen shr 8 and 0xff).toByte()
        header[42] = (totalAudioLen shr 16 and 0xff).toByte()
        header[43] = (totalAudioLen shr 24 and 0xff).toByte()

        var raf: RandomAccessFile? = null
        try {
            raf = RandomAccessFile(file, "rw")
            raf.seek(0)
            raf.write(header)
        } catch (e: IOException) {
            Log.e("VoiceRecorder", "Error updating WAV header", e)
        } finally {
            raf?.close()
        }
    }

    fun stopRecording(): File? {
        if (!isRecording.get()) return null

        isRecording.set(false)

        audioRecord?.apply {
            if (state == AudioRecord.STATE_INITIALIZED) {
                try {
                    stop()
                } catch (e: Exception) {
                    Log.e("VoiceRecorder", "Error stopping AudioRecord", e)
                }
            }
        }

        try {
            recordingThread?.join(1000) // Wait up to 1s for thread to finish
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        recordingThread = null

        audioRecord?.release()
        audioRecord = null

        Log.d("VoiceRecorder", "Recording stopped: ${audioFile?.absolutePath}")
        return audioFile
    }
}
