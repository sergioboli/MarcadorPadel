package com.example.marcadorpadel

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable

class MainActivity : AppCompatActivity(), MessageClient.OnMessageReceivedListener {

    private var scoreA = 0
    private var scoreB = 0
    private var gamesA = 0
    private var gamesB = 0
    private var setsA = 0
    private var setsB = 0
    private var isGoldenPoint = false

    private val pointsSequence = arrayOf("0", "15", "30", "40")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Forzar orientación horizontal por código
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        
        setContentView(R.layout.activity_main)
        updateUI()
    }

    override fun onResume() {
        super.onResume()
        Wearable.getMessageClient(this).addListener(this)
    }

    override fun onPause() {
        super.onPause()
        Wearable.getMessageClient(this).removeListener(this)
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == "/padel_score_sync") {
            val data = String(messageEvent.data, Charsets.UTF_8)
            val parts = data.split(",")
            if (parts.size >= 7) {
                runOnUiThread {
                    scoreA = parts[0].toIntOrNull() ?: 0
                    scoreB = parts[1].toIntOrNull() ?: 0
                    gamesA = parts[2].toIntOrNull() ?: 0
                    gamesB = parts[3].toIntOrNull() ?: 0
                    setsA = parts[4].toIntOrNull() ?: 0
                    setsB = parts[5].toIntOrNull() ?: 0
                    isGoldenPoint = parts[6].toBoolean()
                    updateUI()
                }
            }
        }
    }

    private fun updateUI() {
        val tvScoreA = findViewById<TextView>(R.id.tvScoreA)
        val tvScoreB = findViewById<TextView>(R.id.tvScoreB)
        val tvSetsA = findViewById<TextView>(R.id.tvSetsA)
        val tvSetsB = findViewById<TextView>(R.id.tvSetsB)

        tvScoreA?.text = formatScore(scoreA, scoreB)
        tvScoreB?.text = formatScore(scoreB, scoreA)

        tvSetsA?.text = "S:$setsA J:$gamesA"
        tvSetsB?.text = "S:$setsB J:$gamesB"
    }

    private fun formatScore(myScore: Int, opponentScore: Int): String {
        return when {
            myScore < 4 && opponentScore < 4 -> pointsSequence.getOrElse(myScore) { "0" }
            myScore == opponentScore -> "40"
            myScore > opponentScore -> if (isGoldenPoint) "40" else "AD"
            else -> "40"
        }
    }
}
