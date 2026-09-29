package com.example.marcadorpadel

import android.content.Context
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable

class WearSyncManager(private val context: Context) {

    private val messageClient: MessageClient = Wearable.getMessageClient(context)

    fun sendScoreUpdate(scoreA: Int, scoreB: Int) {
        val scoreData = "$scoreA:$scoreB".toByteArray(Charsets.UTF_8)
        Wearable.getNodeClient(context).connectedNodes.addOnSuccessListener { nodes ->
            for (node in nodes) {
                messageClient.sendMessage(node.id, "/update_score", scoreData)
            }
        }
    }
}
