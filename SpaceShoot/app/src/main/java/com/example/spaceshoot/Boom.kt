package com.example.spaceshoot

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect

class Boom(context: Context, var shx: Int, var shy: Int) {

    var boomBitmap: Bitmap

    init {
        val options = BitmapFactory.Options()
        options.inSampleSize = 4
        boomBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.boom,options)
    }

    fun getBoom(): Bitmap {
        return boomBitmap
    }

    fun getBoomWidth(): Int {
        return boomBitmap.width
    }

    fun getBoomHeight(): Int {
        return boomBitmap.height
    }

    fun getHitbox(): Rect {
        return Rect(shx, shy, shx + boomBitmap.width, shy + boomBitmap.height)
    }
}
