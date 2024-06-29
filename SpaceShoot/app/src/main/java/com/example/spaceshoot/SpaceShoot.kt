package com.example.spaceshoot


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.media.MediaPlayer
import android.os.Handler
import android.view.Display
import android.view.MotionEvent
import android.view.View


class SpaceShoot(context: Context) : View(context) {

    private var background: Bitmap
    private var heartImage: Bitmap
    private val handler: Handler = Handler()
    private val UPDATE_MILLIS: Long = 30
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    private var points = 0
    private var life = 3
    private var highScore = 0
    private val scorePaint: Paint = Paint()
    private val TEXT_SIZE = 80f
    private val ourPlayer: Player
    private val enemyAlien: Alien
    private val PlayerShoots: java.util.ArrayList<Boom> = java.util.ArrayList()
    private val AlienShoots: java.util.ArrayList<Boom> = java.util.ArrayList()
    private val explosions: java.util.ArrayList<Explosion> = java.util.ArrayList()
    private var enemyShootAction = false
    private var playerInvincible = false
    private var lastUpdateTime = 0L
    private var gameEnded = false
    private lateinit var mediaPlayer: MediaPlayer

    init {
        val display: Display = (context as Activity).windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        screenWidth = size.x
        screenHeight = size.y
        ourPlayer = Player(context, screenWidth, screenHeight)
        enemyAlien = Alien(context)
        background = BitmapFactory.decodeResource(context.resources, R.drawable.gamebg)
        background = Bitmap.createScaledBitmap(background, screenWidth, screenHeight, true)
        heartImage = BitmapFactory.decodeResource(context.resources, R.drawable.life)
        val heartSize = screenWidth / 20
        heartImage = Bitmap.createScaledBitmap(heartImage, heartSize, heartSize, true)
        scorePaint.color = Color.RED
        scorePaint.textSize = TEXT_SIZE
        scorePaint.textAlign = Paint.Align.LEFT
        mediaPlayer = MediaPlayer.create(context as Activity, R.raw.gameplay)
        mediaPlayer.isLooping = true
        mediaPlayer.start()

        loadHighScore()
        startGame()
    }

    private fun startGame() {
        resetGame()
        handler.postDelayed(object : Runnable {
            override fun run() {
                updateGame()
                invalidate()
                handler.postDelayed(this, UPDATE_MILLIS)
            }
        }, UPDATE_MILLIS)
    }

    private fun resetGame() {
        points = 0
        life = 3
        PlayerShoots.clear()
        AlienShoots.clear()
        explosions.clear()
        gameEnded = false
    }

    private fun updateGame() {
        if (!gameEnded) {
            val currentTime = System.currentTimeMillis()
            val elapsedTime = currentTime - lastUpdateTime

            if (elapsedTime >= UPDATE_MILLIS) {
                lastUpdateTime = currentTime

                if (!playerInvincible) {
                    // Update enemy movement
                    enemyAlien.ex += enemyAlien.enemyVelocity
                    if (enemyAlien.ex + enemyAlien.getEnemyAlienWidth() >= screenWidth || enemyAlien.ex <= 0) {
                        enemyAlien.enemyVelocity *= -1
                    }

                    // Trigger enemy shooting
                    if (!enemyShootAction) {
                        val shootDelay = 1000 // Delay between each witch shoot in milliseconds
                        handler.postDelayed({
                            val alienShoot = Boom(context, enemyAlien.ex + enemyAlien.getEnemyAlienWidth() / 2, enemyAlien.ey)
                            AlienShoots.add(alienShoot)
                            enemyShootAction = false
                        }, shootDelay.toLong())
                        enemyShootAction = true
                    }

                    // Update alien shoots and check for collisions with player
                    val alienToRemove: java.util.ArrayList<Boom> = java.util.ArrayList()
                    for (alienShoot in AlienShoots) {
                        alienShoot.shy += 35 // Adjust the alien shoot speed as needed
                        if (alienShoot.shy >= screenHeight) {
                            alienToRemove.add(alienShoot)
                        } else if (alienShoot.shx + alienShoot.getBoomWidth()>= ourPlayer.ox &&
                            alienShoot.shx <= ourPlayer.ox + ourPlayer.getOurPlayerWidth() &&
                            alienShoot.shy + alienShoot.getBoomHeight() >= ourPlayer.oy &&
                            alienShoot.shy <= ourPlayer.oy + ourPlayer.getOurPlayerHeight()) {
                            alienToRemove.add(alienShoot)
                            handlePlayerHit()
                        }
                    }
                    AlienShoots.removeAll(alienToRemove)
                }

                // Update player shoots and check for collisions with enemy alien
                updatePlayerShoots()

                // Check game end conditions
                checkGameEndConditions()
            }
        }
    }

    private fun handlePlayerHit() {
        if (!playerInvincible) {
            life--
            if (life <= 0) {
                endGame()
            } else {
                playerInvincible = true
                val explosion = Explosion(context, ourPlayer.ox, ourPlayer.oy)
                explosions.add(explosion)
                playerInvincible = false
            }
        }
    }

    private fun updatePlayerShoots() {
        val shootsToRemove: java.util.ArrayList<Boom> = java.util.ArrayList()
        for (playershoot in PlayerShoots) {
            playershoot.shy -= 25 // Adjust the shoot speed as needed
            if (playershoot.shy <= 0) {
                shootsToRemove.add(playershoot)
                continue
            }
            // Check for collision with enemy witch
            if (playershoot.shx + playershoot.getBoomWidth() >= enemyAlien.ex &&
                playershoot.shx <= enemyAlien.ex + enemyAlien.getEnemyAlienWidth() &&
                playershoot.shy + playershoot.getBoomHeight() >= enemyAlien.ey &&
                playershoot.shy <= enemyAlien.ey + enemyAlien.getEnemyAlienHeight() / 2) {
                shootsToRemove.add(playershoot)
                val explosion = Explosion(context, enemyAlien.ex, enemyAlien.ey)
                explosions.add(explosion)
                points++
                break
            }
        }
        PlayerShoots.removeAll(shootsToRemove)


        if (PlayerShoots.isEmpty()) {
            val ourShot = Boom(context, ourPlayer.ox + ourPlayer.getOurPlayerWidth()/ 2, ourPlayer.oy)
            PlayerShoots.add(ourShot)
        }
    }

    private fun checkGameEndConditions() {
        if (life <= 0 && !gameEnded) {
            endGame()
            gameEnded = true  // Set gameEnded flag to true here to avoid multiple calls to endGame()
        }
    }

    override fun onDraw(canvas: Canvas) {
        // Draw background
        canvas.drawBitmap(background, 0f, 0f, null)

        // Draw explosions
        val explosionsToRemove: java.util.ArrayList<Explosion> = java.util.ArrayList()
        for (explosion in explosions) {
            val explosionBitmap = explosion.getExplosion(explosion.explosionFrame) ?: continue
            canvas.drawBitmap(explosionBitmap, explosion.eX.toFloat(), explosion.eY.toFloat(), null)
            explosion.explosionFrame++
            if (explosion.explosionFrame > 8) {
                explosionsToRemove.add(explosion)
            }
        }
        explosions.removeAll(explosionsToRemove)

        // Draw score
        canvas.drawText("Score: $points", 0f, TEXT_SIZE, scorePaint)

        // Draw hearts
        for (i in life downTo 1) {
            canvas.drawBitmap(heartImage, (screenWidth - heartImage.width * i).toFloat(), 0f, null)
        }

        // Draw alien
        canvas.drawBitmap(enemyAlien.getEnemyAlienBitmap(), enemyAlien.ex.toFloat(), enemyAlien.ey.toFloat(), null)

        // Draw our player
        canvas.drawBitmap(ourPlayer.getOurPlayer(), ourPlayer.ox.toFloat(), ourPlayer.oy.toFloat(), null)

        // Draw player shoots
        for (shoot in PlayerShoots) {
            canvas.drawBitmap(shoot.boomBitmap, shoot.shx.toFloat(), shoot.shy.toFloat(), null)
        }

        // Draw alien shoots
        for (shoot in AlienShoots) {
            canvas.drawBitmap(shoot.boomBitmap, shoot.shx.toFloat(), shoot.shy.toFloat(), null)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x.toInt()

        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            ourPlayer.ox = touchX
        }

        if (event.action == MotionEvent.ACTION_UP) {
            val ourShot = Boom(context, ourPlayer.ox + ourPlayer.getOurPlayerWidth()/ 2, ourPlayer.oy)
            PlayerShoots.add(ourShot)
            val witchShot = Boom(context, enemyAlien.ex + enemyAlien.getEnemyAlienWidth() / 2, enemyAlien.ey)
            AlienShoots.add(witchShot)
        }

        return true
    }

    private fun endGame() {
        mediaPlayer.release()
        if (points > highScore) {
            highScore = points
            saveHighScore()
        }
        handler.removeCallbacksAndMessages(null) // Remove all callbacks and messages
        val intent = Intent(context, GameOver::class.java)
        intent.putExtra("points", points)
        intent.putExtra("highScore", highScore)
        context.startActivity(intent)
        (context as Activity).finish()
    }
    private fun saveHighScore() {
        val prefs: SharedPreferences = context.getSharedPreferences("HighScorePrefs", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = prefs.edit()
        editor.putInt("highScore", highScore)
        editor.apply()
    }

    private fun loadHighScore() {
        val prefs: SharedPreferences = context.getSharedPreferences("HighScorePrefs", Context.MODE_PRIVATE)
        highScore = prefs.getInt("highScore", 0)
    }
}
