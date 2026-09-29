package com.example.marcadorpadel

import android.content.Context
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable

class WearSyncManager(private val context: Context) {

    private val messageClient: MessageClient = Wearable.getMessageClient(context)

    fun sendCustomMessage(path: String, data: String) {
        val bytes = data.toByteArray(Charsets.UTF_8)
        Wearable.getNodeClient(context).connectedNodes.addOnSuccessListener { nodes ->
            for (node in nodes) {
                messageClient.sendMessage(node.id, path, bytes)
            }
        }
    }
}
