package com.example.marcadorpadel

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable

class WearActivity : AppCompatActivity(), MessageClient.OnMessageReceivedListener {

    private lateinit var tvScoreA: TextView
    private lateinit var tvScoreB: TextView
    private lateinit var tvSetsA: TextView
    private lateinit var tvSetsB: TextView
    private lateinit var syncManager: WearSyncManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wear)

        tvScoreA = findViewById(R.id.tvScoreA)
        tvScoreB = findViewById(R.id.tvScoreB)
        tvSetsA = findViewById(R.id.tvSetsA)
        tvSetsB = findViewById(R.id.tvSetsB)

        val layoutTeamA: View = findViewById(R.id.layoutTeamA)
        val layoutTeamB: View = findViewById(R.id.layoutTeamB)
        val btnReset: View = findViewById(R.id.btnReset)

        syncManager = WearSyncManager(this)

        layoutTeamA.setOnClickListener {
            syncManager.sendCustomMessage("/add_point", "1")
        }

        layoutTeamB.setOnClickListener {
            syncManager.sendCustomMessage("/add_point", "2")
        }

        btnReset.setOnClickListener {
            syncManager.sendCustomMessage("/reset_match", "")
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
        if (messageEvent.path == "/update_padel_score") {
            val data = String(messageEvent.data, Charsets.UTF_8).split(":")
            if (data.size == 6) {
                runOnUiThread {
                    tvScoreA.text = data[0]
                    tvScoreB.text = data[1]
                    tvSetsA.text = "S:${data[4]} J:${data[2]}"
                    tvSetsB.text = "S:${data[5]} J:${data[3]}"
                }
            }
        }
    }
}
