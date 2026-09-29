package com.example.marcadorpadel

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var goldenPointMode = false
    private var idx1 = 0; private var idx2 = 0
    private var games1 = 0; private var games2 = 0
    private var sets1 = 0; private var sets2 = 0
    private var isTieBreak = false

    private val scoreLabels = arrayOf("0", "15", "30", "40", "ADV")

    private lateinit var txtScore1: TextView; private lateinit var txtScore2: TextView
    private lateinit var txtHeader1: TextView; private lateinit var txtHeader2: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtScore1 = findViewById(R.id.txtScore1)
        txtScore2 = findViewById(R.id.txtScore2)
        txtHeader1 = findViewById(R.id.txtHeader1)
        txtHeader2 = findViewById(R.id.txtHeader2)

        setupClickListeners()
        showModeSelectionDialog()
    }

    private fun showModeSelectionDialog() {
        val options = arrayOf("Punto de Oro", "Ventajas (Normal)")
        AlertDialog.Builder(this)
            .setTitle("Selecciona Modo de Juego")
            .setCancelable(false)
            .setItems(options) { _, which ->
                goldenPointMode = (which == 0)
                updateUI()
            }.show()
    }

    private fun setupClickListeners() {
        val btn1 = findViewById<FrameLayout>(R.id.btnTeam1)
        val btn2 = findViewById<FrameLayout>(R.id.btnTeam2)

        btn1.setOnClickListener { addPoint(1) }
        btn1.setOnLongClickListener { removePoint(1); true }

        btn2.setOnClickListener { addPoint(2) }
        btn2.setOnLongClickListener { removePoint(2); true }
    }

    private fun addPoint(team: Int) {
        if (isTieBreak) {
            handleTieBreakPoint(team)
        } else {
            handleStandardPoint(team)
        }
        updateUI()
    }

    private fun handleStandardPoint(team: Int) {
        if (team == 1) {
            if (goldenPointMode && idx1 == 3 && idx2 == 3) { winGame(1); return }
            if (idx1 == 3 && idx2 < 3) winGame(1)
            else if (idx1 == 3 && idx2 == 3) idx1 = 4
            else if (idx1 == 3 && idx2 == 4) idx2 = 3
            else if (idx1 == 4) winGame(1)
            else idx1++
        } else {
            if (goldenPointMode && idx1 == 3 && idx2 == 3) { winGame(2); return }
            if (idx2 == 3 && idx1 < 3) winGame(2)
            else if (idx1 == 3 && idx2 == 3) idx2 = 4
            else if (idx2 == 3 && idx1 == 4) idx1 = 3
            else if (idx2 == 4) winGame(2)
            else idx2++
        }
    }

    private fun handleTieBreakPoint(team: Int) {
        if (team == 1) idx1++ else idx2++
        if (idx1 >= 7 && (idx1 - idx2) >= 2) winGame(1)
        else if (idx2 >= 7 && (idx2 - idx1) >= 2) winGame(2)
    }

    private fun winGame(team: Int) {
        idx1 = 0; idx2 = 0
        if (team == 1) games1++ else games2++

        if (games1 == 6 && games2 == 6) {
            isTieBreak = true
        } else if ((games1 >= 6 && games1 - games2 >= 2) || games1 == 7) {
            sets1++; games1 = 0; games2 = 0; isTieBreak = false
        } else if ((games2 >= 6 && games2 - games1 >= 2) || games2 == 7) {
            sets2++; games1 = 0; games2 = 0; isTieBreak = false
        }
    }

    private fun removePoint(team: Int) {
        if (isTieBreak) {
            if (team == 1 && idx1 > 0) idx1--
            if (team == 2 && idx2 > 0) idx2--
        } else {
            if (team == 1 && idx1 > 0) idx1--
            if (team == 2 && idx2 > 0) idx2--
        }
        updateUI()
    }

    private fun updateUI() {
        txtHeader1.text = "SETS: $sets1  |  Juegos: $games1"
        txtHeader2.text = "SETS: $sets2  |  Juegos: $games2"

        txtScore1.text = if (isTieBreak) idx1.toString() else scoreLabels[idx1]
        txtScore2.text = if (isTieBreak) idx2.toString() else scoreLabels[idx2]
    }
}
