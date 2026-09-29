package com.example.marcadorpadel

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable

class WearActivity : AppCompatActivity(), MessageClient.OnMessageReceivedListener {

    private var scoreA = 0
    private var scoreB = 0

    private lateinit var tvScoreA: TextView
    private lateinit var tvScoreB: TextView
    private lateinit var syncManager: WearSyncManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wear)

        tvScoreA = findViewById(R.id.tvScoreA)
        tvScoreB = findViewById(R.id.tvScoreB)
        val layoutTeamA: View = findViewById(R.id.layoutTeamA)
        val layoutTeamB: View = findViewById(R.id.layoutTeamB)
        val btnReset: View = findViewById(R.id.btnReset)

        syncManager = WearSyncManager(this)

        // Pulsar sobre la mitad/puntuación de Equipo A
        layoutTeamA.setOnClickListener {
            scoreA++
            updateUI()
            syncManager.sendScoreUpdate(scoreA, scoreB)
        }

        // Pulsar sobre la mitad/puntuación de Equipo B
        layoutTeamB.setOnClickListener {
            scoreB++
            updateUI()
            syncManager.sendScoreUpdate(scoreA, scoreB)
        }

        // Botón de reinicio rápido
        btnReset.setOnClickListener {
            scoreA = 0
            scoreB = 0
            updateUI()
            syncManager.sendScoreUpdate(scoreA, scoreB)
        }
    }

    private fun updateUI() {
        runOnUiThread {
            tvScoreA.text = scoreA.toString()
            tvScoreB.text = scoreB.toString()
        }
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
        if (messageEvent.path == "/update_score") {
            val scoreStr = String(messageEvent.data, Charsets.UTF_8)
            val parts = scoreStr.split(":")
            if (parts.size == 2) {
                scoreA = parts[0].toIntOrNull() ?: scoreA
                scoreB = parts[1].toIntOrNull() ?: scoreB
                updateUI()
            }
        }
    }
}
