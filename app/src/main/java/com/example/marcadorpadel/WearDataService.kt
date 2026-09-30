package com.example.marcadorpadel

import android.content.Intent
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class WearDataService : WearableListenerService() {
    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        if (messageEvent.path == "/padel_score_sync" || messageEvent.path == "/request_sync") {
            val intent = Intent("com.example.marcadorpadel.UPDATE_SCORE")
            intent.putExtra("path", messageEvent.path)
            intent.putExtra("data", String(messageEvent.data, Charsets.UTF_8))
            sendBroadcast(intent)
        }
    }
}
