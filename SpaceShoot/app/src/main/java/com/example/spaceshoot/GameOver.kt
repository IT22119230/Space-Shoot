package com.example.spaceshoot

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class GameOver : AppCompatActivity() {

    private lateinit var tvPoints: TextView
    private lateinit var tvHighScore: TextView
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gameover)

        val points = intent.getIntExtra("points", 0) // Retrieve points from intent extras
        val highScore = intent.getIntExtra("highScore", 0) // Retrieve high score from intent extras

        tvPoints = findViewById(R.id.tvPoints)
        tvPoints.text = getString(R.string.points, points) // Display points

        tvHighScore = findViewById(R.id.tvHighScore) // Initialize TextView for high score
        tvHighScore.text = getString(R.string.high_score, highScore) // Display high score
        mediaPlayer = MediaPlayer.create(this, R.raw.gameover)
        mediaPlayer.isLooping = true
        mediaPlayer.start()
    }

    fun restart(view: View) {
        finish()
        mediaPlayer.release()
        val intent = Intent(this, StartUp::class.java)
        startActivity(intent)

    }

    fun exit(view: View) {
        finishAffinity()
        mediaPlayer.release()
    }
}
