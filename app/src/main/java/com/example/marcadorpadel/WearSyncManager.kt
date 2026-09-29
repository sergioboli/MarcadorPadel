package com.example.marcadorpadel

import android.content.Context
import com.google.android.gms.wearable.Wearable

class WearSyncManager(private val context: Context) {

    fun sendCustomMessage(path: String, data: String) {
        Wearable.getNodeClient(context).connectedNodes.addOnSuccessListener { nodes ->
            for (node in nodes) {
                Wearable.getMessageClient(context).sendMessage(
                    node.id,
                    path,
                    data.toByteArray(Charsets.UTF_8)
                )
            }
        }
    }
}
