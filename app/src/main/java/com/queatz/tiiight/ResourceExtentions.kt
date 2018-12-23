package com.queatz.tiiight

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable




fun Drawable.toBitmap(color: Int? = null): Bitmap {
    if (this is BitmapDrawable) {
        return this.bitmap
    }

    val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)

    color?.let {
        colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
    }

    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)

    return bitmap
}