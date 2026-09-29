package com.example.marcadorpadel

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WearActivity : AppCompatActivity() {

    private var isGoldenPoint = false
    private var scoreA = 0
    private var scoreB = 0
    private var gamesA = 0
    private var gamesB = 0
    private var setsA = 0
    private var setsB = 0

    private val pointsSequence = arrayOf("0", "15", "30", "40")

    private lateinit var syncManager: WearSyncManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wear)

        syncManager = WearSyncManager(this)

        val layoutMode = findViewById<View>(R.id.layoutWearModeSelection)
        val btnNormal = findViewById<Button>(R.id.btnWearModeNormal)
        val btnGold = findViewById<Button>(R.id.btnWearModeGold)

        val layoutTeamA = findViewById<View>(R.id.layoutTeamA)
        val layoutTeamB = findViewById<View>(R.id.layoutTeamB)

        btnNormal?.setOnClickListener {
            isGoldenPoint = false
            layoutMode?.visibility = View.GONE
            syncState()
        }

        btnGold?.setOnClickListener {
            isGoldenPoint = true
            layoutMode?.visibility = View.GONE
            syncState()
        }

        layoutTeamA?.setOnClickListener { addPoint(true) }
        layoutTeamB?.setOnClickListener { addPoint(false) }

        updateUI()
    }

    private fun addPoint(isTeamA: Boolean) {
        if (isTeamA) {
            scoreA++
        } else {
            scoreB++
        }
        checkGameWinner()
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

    private fun syncState() {
        val data = "$scoreA,$scoreB,$gamesA,$gamesB,$setsA,$setsB,$isGoldenPoint"
        syncManager.sendCustomMessage("/padel_score_sync", data)
    }
}package com.example.marcadorpadel

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WearActivity : AppCompatActivity() {

    private var isGoldenPoint = false
    private var scoreA = 0
    private var scoreB = 0
    private var gamesA = 0
    private var gamesB = 0
    private var setsA = 0
    private var setsB = 0

    private val pointsSequence = arrayOf("0", "15", "30", "40")

    private lateinit var syncManager: WearSyncManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wear)

        syncManager = WearSyncManager(this)

        val layoutMode = findViewById<View>(R.id.layoutWearModeSelection)
        val btnNormal = findViewById<Button>(R.id.btnWearModeNormal)
        val btnGold = findViewById<Button>(R.id.btnWearModeGold)

        val layoutTeamA = findViewById<View>(R.id.layoutTeamA)
        val layoutTeamB = findViewById<View>(R.id.layoutTeamB)

        btnNormal?.setOnClickListener {
            isGoldenPoint = false
            layoutMode?.visibility = View.GONE
        }

        btnGold?.setOnClickListener {
            isGoldenPoint = true
            layoutMode?.visibility = View.GONE
        }

        layoutTeamA?.setOnClickListener { addPoint(true) }
        layoutTeamB?.setOnClickListener { addPoint(false) }

        updateUI()
    }

    private fun addPoint(isTeamA: Boolean) {
        if (isTeamA) {
            scoreA++
        } else {
            scoreB++
        }
        checkGameWinner()
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

    private fun syncState() {
        val data = "$scoreA,$scoreB,$gamesA,$gamesB,$setsA,$setsB,$isGoldenPoint"
        syncManager.sendCustomMessage("/padel_score_sync", data)
    }
}
