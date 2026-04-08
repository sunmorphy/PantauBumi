package com.andikas.pantaubumi.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.DrawableCompat
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.IconFactory

fun bitmapDescriptorFromVector(
    context: Context,
    @DrawableRes vectorResId: Int,
    pinColor: Int
): Icon {
    val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
    val wrapDrawable = DrawableCompat.wrap(vectorDrawable!!).mutate()
    DrawableCompat.setTint(wrapDrawable, Color.WHITE)
    val width = 64
    val height = 64
    val bitmap = createBitmap(width, height)
    val canvas = Canvas(bitmap)

    val paint = Paint().apply {
        color = pinColor
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    val radius = width / 2f
    canvas.drawCircle(radius, radius, radius, paint)

    paint.color = Color.WHITE
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 4f
    canvas.drawCircle(radius, radius, radius - 2f, paint)

    wrapDrawable.setBounds(12, 12, width - 12, height - 12)
    wrapDrawable.draw(canvas)

    return IconFactory.getInstance(context).fromBitmap(bitmap)
}
