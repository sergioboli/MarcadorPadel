package com.example.marcadorpadel

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
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
    private var isModeSelected = false

    private val pointsSequence = arrayOf("0", "15", "30", "40")
    private lateinit var syncManager: WearSyncManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Si se ejecuta en un reloj Wear OS, redirigir a la interfaz del reloj
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH)) {
            setContentView(R.layout.activity_wear)
            setupWearEvents()
        } else {
            setContentView(R.layout.activity_main)
            setupPhoneEvents()
        }

        syncManager = WearSyncManager(this)
    }

    private fun setupPhoneEvents() {
        val layoutModeSelection = findViewById<View>(R.id.layoutModeSelection)
        val btnPuntoOro = findViewById<Button>(R.id.btnPuntoOro)
        val btnVentaja = findViewById<Button>(R.id.btnVentaja)

        btnPuntoOro?.setOnClickListener {
            isGoldenPoint = true
            isModeSelected = true
            layoutModeSelection?.visibility = View.GONE
            syncState()
        }

        btnVentaja?.setOnClickListener {
            isGoldenPoint = false
            isModeSelected = true
            layoutModeSelection?.visibility = View.GONE
            syncState()
        }

        setupClickListeners()
    }

    private fun setupWearEvents() {
        setupClickListeners()
    }

    private fun setupClickListeners() {
        val layoutTeamA = findViewById<View>(R.id.layoutTeamA)
        val layoutTeamB = findViewById<View>(R.id.layoutTeamB)

        layoutTeamA?.setOnClickListener { addPoint(true) }
        layoutTeamB?.setOnClickListener { addPoint(false) }

        layoutTeamA?.setOnLongClickListener {
            subtractPoint(true)
            true
        }
        layoutTeamB?.setOnLongClickListener {
            subtractPoint(false)
            true
        }
    }

    private fun addPoint(isTeamA: Boolean) {
        if (!isModeSelected && !packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH)) return
        if (isTeamA) scoreA++ else scoreB++
        checkGameWinner()
        updateUI()
        syncState()
    }

    private fun subtractPoint(isTeamA: Boolean) {
        if (isTeamA) {
            if (scoreA > 0) scoreA--
        } else {
            if (scoreB > 0) scoreB--
        }
        updateUI()
        syncState()
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
        Wearable.getMessageClient(this).addListener(this)
        syncManager.sendCustomMessage("/request_sync", "get")
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
                    isModeSelected = true

                    val layoutModeSelection = findViewById<View>(R.id.layoutModeSelection)
                    layoutModeSelection?.visibility = View.GONE

                    val tvWearWaiting = findViewById<View>(R.id.tvWearWaiting)
                    val layoutWearMainContent = findViewById<View>(R.id.layoutWearMainContent)
                    tvWearWaiting?.visibility = View.GONE
                    layoutWearMainContent?.visibility = View.VISIBLE

                    updateUI()
                }
            }
        } else if (messageEvent.path == "/request_sync") {
            syncState()
        }
    }

    private fun updateUI() {
        val tvScoreA = findViewById<TextView>(R.id.tvScoreA)
        val tvScoreB = findViewById<TextView>(R.id.tvScoreB)
        val tvSetsA = findViewById<TextView>(R.id.tvSetsA)
        val tvSetsB = findViewById<TextView>(R.id.tvSetsB)

        tvScoreA?.text = formatScore(scoreA, scoreB)
        tvScoreB?.text = formatScore(scoreB, scoreA)

        if (packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH)) {
            tvSetsA?.text = "SETS: $setsA\nJUEGOS: $gamesA"
            tvSetsB?.text = "SETS: $setsB\nJUEGOS: $gamesB"
        } else {
            tvSetsA?.text = "SETS: $setsA   JUEGOS: $gamesA"
            tvSetsB?.text = "SETS: $setsB   JUEGOS: $gamesB"
        }
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
        val data = "$scoreA,$scoreB,$gamesA,$gamesB,$setsA,$setsB,$isGoldenPoint"
        syncManager.sendCustomMessage("/padel_score_sync", data)
    }
}
