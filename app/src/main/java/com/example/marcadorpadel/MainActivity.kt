package com.example.marcadorpadel

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
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

    private val history = mutableListOf<State>()
    private val pointsSequence = arrayOf("0", "15", "30", "40")
    private lateinit var syncManager: WearSyncManager

    data class State(
        val scoreA: Int, val scoreB: Int,
        val gamesA: Int, val gamesB: Int,
        val setsA: Int, val setsB: Int
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        setContentView(R.layout.activity_main)

        syncManager = WearSyncManager(this)

        val layoutTeamA = findViewById<View>(R.id.layoutTeamA)
        val layoutTeamB = findViewById<View>(R.id.layoutTeamB)

        // Clic corto: Sumar punto
        layoutTeamA?.setOnClickListener { addPoint(true) }
        layoutTeamB?.setOnClickListener { addPoint(false) }

        // Clic largo: Deshacer
        layoutTeamA?.setOnLongClickListener {
            undoPoint()
            true
        }
        layoutTeamB?.setOnLongClickListener {
            undoPoint()
            true
        }

        updateUI()
    }

    private fun saveState() {
        history.add(State(scoreA, scoreB, gamesA, gamesB, setsA, setsB))
    }

    private fun addPoint(isTeamA: Boolean) {
        saveState()
        if (isTeamA) scoreA++ else scoreB++
        checkGameWinner()
        updateUI()
        syncState()
    }

    private fun undoPoint() {
        if (history.isNotEmpty()) {
            val lastState = history.removeAt(history.size - 1)
            scoreA = lastState.scoreA
            scoreB = lastState.scoreB
            gamesA = lastState.gamesA
            gamesB = lastState.gamesB
            setsA = lastState.setsA
            setsB = lastState.setsB
            updateUI()
            syncState()
        }
    }

    private fun checkGameWinner() {
        if (scoreA >= 4 || scoreB >= 4) {
            val diff = scoreA - scoreB
            if (isGoldenPoint) {
                if (scoreA >= 4) winGame(true) else if (scoreB >= 4) winGame(false)
            } else {
                if (diff >= 2) winGame(true)
                else if (diff <= -2) winGame(false)
            }
        }
    }

    private fun winGame(isTeamA: Boolean) {
        scoreA = 0
        scoreB = 0
        if (isTeamA) gamesA++ else gamesB++
        if (gamesA >= 6 || gamesB >= 6) {
            if (Math.abs(gamesA - gamesB) >= 2) {
                if (gamesA > gamesB) setsA++ else setsB++
                gamesA = 0
                gamesB = 0
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            Wearable.getMessageClient(this).addListener(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            Wearable.getMessageClient(this).removeListener(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

        tvSetsA?.text = "SET:$setsA JUEGO:$gamesA"
        tvSetsB?.text = "SET:$setsB JUEGO:$gamesB"
    }

    private fun formatScore(myScore: Int, opponentScore: Int): String {
        return when {
            myScore < 4 && opponentScore < 4 -> pointsSequence.getOrElse(myScore) { "0" }
            myScore == opponentScore -> "40"
            myScore > opponentScore -> if (isGoldenPoint) "40" else "AD"
            else -> "40"
        }
    }

    private fun syncState() {
        try {
            val data = "$scoreA,$scoreB,$gamesA,$gamesB,$setsA,$setsB,$isGoldenPoint"
            syncManager.sendCustomMessage("/padel_score_sync", data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
