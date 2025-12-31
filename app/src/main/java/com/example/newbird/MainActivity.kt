package com.example.newbird

import android.media.MediaPlayer
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            // Force full black screen
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black
            ) {
                NewBirdApp()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    @Composable
    fun NewBirdApp() {
        var currentTimeString by remember { mutableStateOf("") }

        // Audio target: 4:55 (295 seconds) matches Midnight
        val targetOffsetSeconds = 4 * 60 + 55

        LaunchedEffect(Unit) {
            while (true) {
                val now = Calendar.getInstance()
                // Update visible clock
                currentTimeString = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(now.time)
                
                // --- SILENT LOGIC START ---
                // Calculate target Midnight
                val targetMidnight = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // Target Start Time = Midnight - 4m 55s
                val startPlayTime = (targetMidnight.timeInMillis) - (targetOffsetSeconds * 1000)
                val diff = startPlayTime - now.timeInMillis

                if (diff <= 0) {
                    // We passed the start time
                    val seekPosition = (now.timeInMillis - startPlayTime).toInt()
                    
                    // Allow playing if we are within 10 mins after start
                    if (seekPosition < targetOffsetSeconds * 1000 + (10 * 60 * 1000)) { 
                         if (!isPlaying) {
                             startPlayer(seekPosition)
                         }
                    }
                }
                // --- SILENT LOGIC END ---

                delay(200) // Update 5 times a second for responsive clock
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currentTimeString,
                color = Color.White,
                fontSize = 60.sp, // Large font
                fontWeight = FontWeight.Bold
            )
        }
    }

    private fun startPlayer(seekTo: Int) {
        if (isPlaying) return
        
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(this, R.raw.free_bird)
                mediaPlayer?.setOnCompletionListener { 
                    isPlaying = false
                }
            }
            
            if (!mediaPlayer!!.isPlaying) {
                if (seekTo > 0) {
                    mediaPlayer?.seekTo(seekTo)
                }
                mediaPlayer?.start()
                isPlaying = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}