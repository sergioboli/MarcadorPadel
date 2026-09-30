override fun onMessageReceived(messageEvent: MessageEvent) {
    if (messageEvent.path == "/request_sync") {
        // El reloj solicita sincronización: le enviamos el estado actual
        syncState()
    } else if (messageEvent.path == "/padel_score_sync") {
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
