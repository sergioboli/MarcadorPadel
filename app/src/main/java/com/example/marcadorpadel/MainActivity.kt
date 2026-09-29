package com.example.marcadorpadel

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable

class MainActivity : AppCompatActivity(), MessageClient.OnMessageReceivedListener {

    private var ptsA = 0
    private var ptsB = 0
    private var gamesA = 0
    private var gamesB = 0
    private var setsA = 0
    private var setsB = 0

    private var isGoldPoint = false

    private lateinit var layoutModeSelection: View
    private lateinit var tvScoreA: TextView
    private lateinit var tvScoreB: TextView
    private lateinit var tvSetsA: TextView
    private lateinit var tvSetsB: TextView
    private lateinit var syncManager: WearSyncManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        layoutModeSelection = findViewById(R.id.layoutModeSelection)
        tvScoreA = findViewById(R.id.tvScoreA)
        tvScoreB = findViewById(R.id.tvScoreB)
        tvSetsA = findViewById(R.id.tvSetsA)
        tvSetsB = findViewById(R.id.tvSetsB)

        val btnModeNormal: Button = findViewById(R.id.btnModeNormal)
        val btnModeGold: Button = findViewById(R.id.btnModeGold)
        val layoutTeamA: View = findViewById(R.id.layoutTeamA)
        val layoutTeamB: View = findViewById(R.id.layoutTeamB)
        val btnUndo: Button = findViewById(R.id.btnUndo)
        val btnReset: Button = findViewById(R.id.btnReset)

        syncManager = WearSyncManager(this)

        btnModeNormal.setOnClickListener {
            isGoldPoint = false
            layoutModeSelection.visibility = View.GONE
        }

        btnModeGold.setOnClickListener {
            isGoldPoint = true
            layoutModeSelection.visibility = View.GONE
        }

        layoutTeamA.setOnClickListener {
            addPoint(1)
        }

        layoutTeamB.setOnClickListener {
            addPoint(2)
        }

        btnUndo.setOnClickListener {
            undoPoint()
        }

        btnReset.setOnClickListener {
            resetMatch()
        }
    }

    private fun addPoint(team: Int) {
        if (team == 1) ptsA++ else ptsB++

        // Lógica Punto de Oro / Deuce
        if (ptsA >= 3 && ptsB >= 3) {
            if (isGoldPoint) {
                if (team == 1 && ptsA == 4) winGame(1)
                else if (team == 2 && ptsB == 4) winGame(2)
            } else {
                if (ptsA - ptsB >= 2) winGame(1)
                else if (ptsB - ptsA >= 2) winGame(2)
            }
        } else {
            if (ptsA == 4) winGame(1)
            else if (ptsB == 4) winGame(2)
        }

        updateUI()
        sendDataToWear()
    }

    private fun winGame(team: Int) {
        ptsA = 0
        ptsB = 0
        if (team == 1) gamesA++ else gamesB++

        if (gamesA >= 6 && gamesA - gamesB >= 2) {
            setsA++
            gamesA = 0
            gamesB = 0
        } else if (gamesB >= 6 && gamesB - gamesA >= 2) {
            setsB++
            gamesA = 0
            gamesB = 0
        }
    }

    private fun undoPoint() {
        if (ptsA > 0) ptsA--
        else if (ptsB > 0) ptsB--
        updateUI()
        sendDataToWear()
    }

    private fun resetMatch() {
        ptsA = 0; ptsB = 0; gamesA = 0; gamesB = 0; setsA = 0; setsB = 0
        layoutModeSelection.visibility = View.VISIBLE
        updateUI()
        sendDataToWear()
    }

    private fun getScoreString(pA: Int, pB: Int): Pair<String, String> {
        if (pA >= 3 && pB >= 3) {
            if (isGoldPoint) return Pair("40", "40")
            if (pA == pB) return Pair("40", "40")
            if (pA > pB) return Pair("VENT", "40")
            return Pair("40", "VENT")
        }
        val scores = arrayOf("0", "15", "30", "40")
        return Pair(scores.getOrElse(pA) { "40" }, scores.getOrElse(pB) { "40" })
    }

    private fun updateUI() {
        runOnUiThread {
            val (sA, sB) = getScoreString(ptsA, ptsB)
            tvScoreA.text = sA
            tvScoreB.text = sB
            tvSetsA.text = "Sets: $setsA | Juegos: $gamesA"
            tvSetsB.text = "Sets: $setsB | Juegos: $gamesB"
        }
    }

    private fun sendDataToWear() {
        val (sA, sB) = getScoreString(ptsA, ptsB)
        val dataStr = "$sA:$sB:$gamesA:$gamesB:$setsA:$setsB"
        syncManager.sendCustomMessage("/update_padel_score", dataStr)
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
        if (messageEvent.path == "/add_point") {
            val team = String(messageEvent.data, Charsets.UTF_8).toIntOrNull() ?: 1
            runOnUiThread { addPoint(team) }
        } else if (messageEvent.path == "/reset_match") {
            runOnUiThread { resetMatch() }
        }
    }
}
