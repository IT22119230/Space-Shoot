package com.example.spaceshoot

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.util.*

class Alien(private val context: Context) {
    private var enemyAlien: Bitmap
    var ex = 0
    var ey = 0
    var enemyVelocity = 0
    private val random: Random

    init {
        val options = BitmapFactory.Options()
        options.inSampleSize = 2 // Adjust this value to resize the image accordingly
        enemyAlien = BitmapFactory.decodeResource(context.resources, R.drawable.alian2, options)
        random = Random()
        resetEnemyAlien()
    }

    fun getEnemyAlienBitmap(): Bitmap {
        return enemyAlien
    }

    fun getEnemyAlienWidth(): Int {
        return enemyAlien.width
    }

    fun getEnemyAlienHeight(): Int {
        return enemyAlien.height
    }

    private fun resetEnemyAlien() {
        ex = 200 + random.nextInt(400)
        ey = 0
        enemyVelocity = 17 + random.nextInt(10)
    }

}