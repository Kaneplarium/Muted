package kaneplarium.focustimer.util

import android.content.Context
import android.media.AudioManager

class FocusSoundManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var previousRingerMode: Int? = null

    fun mutePhone() {
        try {
            previousRingerMode = audioManager.ringerMode
            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun unmuteAndSet50PercentVolume() {
        try {
            audioManager.ringerMode = previousRingerMode ?: AudioManager.RINGER_MODE_NORMAL
            
            val streams = listOf(
                AudioManager.STREAM_RING,
                AudioManager.STREAM_MUSIC,
                AudioManager.STREAM_NOTIFICATION
            )
            for (stream in streams) {
                val maxVol = audioManager.getStreamMaxVolume(stream)
                val targetVol = (maxVol * 0.5).toInt()
                audioManager.setStreamVolume(stream, targetVol, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        previousRingerMode = null
    }
}
