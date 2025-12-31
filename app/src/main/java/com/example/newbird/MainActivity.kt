package com.example.newbird

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NewBirdApp()
                }
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
        var currentTime by remember { mutableStateOf(Calendar.getInstance()) }
        var timeToStart by remember { mutableStateOf("") }
        var statusMessage by remember { mutableStateOf("Calcolo...") }

        // Audio target: 4:55 (295 seconds) matches Midnight
        val targetOffsetSeconds = 4 * 60 + 55

        LaunchedEffect(Unit) {
            while (true) {
                currentTime = Calendar.getInstance()
                
                // Calculate target Midnight
                val now = Calendar.getInstance()
                val targetMidnight = Calendar.getInstance().apply {
                    // Set to next midnight (Jan 1st usually, or just next midnight for safety/testing)
                    // If today is Dec 31, target is tomorrow. 
                    // Logic: Find the closest future midnight that is a New Year (Jan 1 00:00).
                    // For general robustness, let's target the *next* midnight.
                    // If user runs this on Dec 31, next midnight is Jan 1.
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // If currently Dec 31st, ensure we are targeting Jan 1st.
                // Simple logic for the specific request: 
                // Target Start Time = Midnight - 4m 55s
                val startPlayTime = (targetMidnight.timeInMillis) - (targetOffsetSeconds * 1000)
                val diff = startPlayTime - now.timeInMillis

                if (diff > 0) {
                    statusMessage = "In attesa del momento giusto..."
                    val seconds = diff / 1000
                    val minutes = seconds / 60
                    val remSeconds = seconds % 60
                    timeToStart = String.format("Mancano: %02d:%02d", minutes, remSeconds)
                } else {
                    // We passed the start time
                    val seekPosition = (now.timeInMillis - startPlayTime).toInt()
                    
                    // If we are before midnight (meaning seekPosition < targetOffsetSeconds * 1000), play!
                    // If we are AFTER midnight, well, it's done.
                    
                    if (seekPosition < targetOffsetSeconds * 1000 + (10 * 60 * 1000)) { // Allow playing if we are within 10 mins after start
                         if (!isPlaying) {
                             startPlayer(seekPosition)
                             statusMessage = "RIPRODUZIONE IN CORSO!"
                             timeToStart = "ROCK N ROLL!"
                         } else {
                             statusMessage = "RIPRODUZIONE IN CORSO!"
                             timeToStart = "Goditi l'assolo a mezzanotte!"
                         }
                    } else {
                        statusMessage = "Evento passato."
                        timeToStart = "--:--"
                    }
                }

                delay(500) // Update every half second
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "New Bird Countdown", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(32.dp))
            Text(text = statusMessage, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = timeToStart, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(32.dp))
            Text(text = "Ora attuale: ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(currentTime.time)}")
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Target start: 23:55:05")
        }
    }

    private fun startPlayer(seekTo: Int) {
        if (isPlaying) return
        
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.free_bird)
            mediaPlayer?.setOnCompletionListener { 
                isPlaying = false
            }
            if (seekTo > 0) {
                mediaPlayer?.seekTo(seekTo)
            }
            mediaPlayer?.start()
            isPlaying = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
