package com.example.newbird

import android.media.MediaPlayer
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
        
        // Custom Time State
        var customHour by remember { mutableStateOf("") }
        var customMinute by remember { mutableStateOf("") }
        var useCustomTime by remember { mutableStateOf(false) }
        var targetCalendar by remember { mutableStateOf<Calendar?>(null) }

        // Audio target: 4:55 (295 seconds) matches the Target Time
        val targetOffsetSeconds = 4 * 60 + 55

        // Loop di aggiornamento
        LaunchedEffect(useCustomTime, targetCalendar) {
            while (true) {
                val now = Calendar.getInstance() // Gets phone's timezone automatically
                currentTime = now
                
                // Determine target time
                val target = if (useCustomTime && targetCalendar != null) {
                    // Update the day of the target if it has passed? 
                    // For testing, we usually set a time in the near future.
                    // If the configured target is in the past relative to 'now', add a day
                    val checkTarget = targetCalendar!!.clone() as Calendar
                    checkTarget.set(Calendar.SECOND, 0)
                    checkTarget.set(Calendar.MILLISECOND, 0)
                    
                    // If we just set 10:00 and it is 10:01, assume tomorrow.
                    // But if we are calculating "Start Time", we need to be careful.
                    // Logic: The "Drop" happens at target. Play starts at target - 4m55s.
                    
                    val startPlayTimeCheck = checkTarget.timeInMillis - (targetOffsetSeconds * 1000)
                    if (now.timeInMillis > startPlayTimeCheck + (10 * 60 * 1000)) {
                         // If the whole event is long past (10 mins after drop), move to tomorrow
                         checkTarget.add(Calendar.DAY_OF_YEAR, 1)
                    }
                    checkTarget
                } else {
                    // Default: Next Midnight
                    val nextMidnight = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, 1)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    nextMidnight
                }

                val startPlayTime = (target.timeInMillis) - (targetOffsetSeconds * 1000)
                val diff = startPlayTime - now.timeInMillis

                if (diff > 0) {
                    val seconds = diff / 1000
                    val minutes = seconds / 60
                    val remSeconds = seconds % 60
                    
                    statusMessage = if (useCustomTime) "Target Personalizzato: ${formatTime(target)}" else "Target: Mezzanotte"
                    timeToStart = String.format("Mancano: %02d:%02d", minutes, remSeconds)
                } else {
                    // We passed the start time
                    val seekPosition = (now.timeInMillis - startPlayTime).toInt()
                    
                    // Allow playing if we are within 10 mins after the specific start moment
                    if (seekPosition < targetOffsetSeconds * 1000 + (10 * 60 * 1000)) { 
                         if (!isPlaying) {
                             startPlayer(seekPosition)
                         }
                         statusMessage = "SOLO IN ARRIVO / IN CORSO!"
                         timeToStart = "ROCK N ROLL!"
                    } else {
                        statusMessage = "Evento passato per oggi."
                        timeToStart = "--:--"
                        isPlaying = false
                        mediaPlayer?.pause()
                    }
                }

                delay(500)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "NewBird Countdown", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(text = statusMessage, fontSize = 18.sp)
            Text(text = timeToStart, fontSize = 40.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(text = "Ora attuale: ${formatTime(currentTime)}")
            
            Spacer(modifier = Modifier.height(32.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            Text("Imposta Orario Drop (Test)", fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = customHour,
                    onValueChange = { if (it.length <= 2) customHour = it.filter { c -> c.isDigit() } },
                    label = { Text("HH") },
                    modifier = Modifier.width(70.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(":")
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = customMinute,
                    onValueChange = { if (it.length <= 2) customMinute = it.filter { c -> c.isDigit() } },
                    label = { Text("MM") },
                    modifier = Modifier.width(70.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row {
                Button(onClick = {
                    val h = customHour.toIntOrNull()
                    val m = customMinute.toIntOrNull()
                    if (h != null && m != null && h in 0..23 && m in 0..59) {
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.HOUR_OF_DAY, h)
                        cal.set(Calendar.MINUTE, m)
                        cal.set(Calendar.SECOND, 0)
                        cal.set(Calendar.MILLISECOND, 0)
                        
                        // If user sets 10:00 and it is 10:05, assume they meant tomorrow? 
                        // Or maybe they want to test the "event passed" logic.
                        // Let's assume if it's in the past more than 15 mins, it's tomorrow.
                        if (cal.timeInMillis < System.currentTimeMillis() - (15 * 60 * 1000)) {
                            cal.add(Calendar.DAY_OF_YEAR, 1)
                        }
                        
                        targetCalendar = cal
                        useCustomTime = true
                        
                        // Stop current playback if resetting
                        mediaPlayer?.pause()
                        mediaPlayer?.seekTo(0)
                        isPlaying = false
                    }
                }) {
                    Text("Imposta Target")
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Button(onClick = { 
                    useCustomTime = false 
                    customHour = ""
                    customMinute = ""
                    mediaPlayer?.pause()
                    isPlaying = false
                }) {
                    Text("Reset Mezzanotte")
                }
            }
        }
    }

    private fun formatTime(cal: Calendar): String {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(cal.time)
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
