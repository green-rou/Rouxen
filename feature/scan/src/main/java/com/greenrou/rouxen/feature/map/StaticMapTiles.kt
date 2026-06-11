package com.greenrou.rouxen.feature.map

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.tan

private const val ZOOM = 13
private const val TILE_SIZE = 256
private const val GRID = 3
private const val CROP_SIZE = 480

/**
 * Fetches a 3x3 grid of OSM raster tiles around (lat, lon), stitches them into a mosaic,
 * and crops a square centered on the exact projected pixel of the target point.
 */
internal fun fetchStaticMapBitmap(client: OkHttpClient, lat: Double, lon: Double): Bitmap {
    val n = 1 shl ZOOM
    val xFrac = (lon + 180.0) / 360.0 * n
    val latRad = Math.toRadians(lat)
    val yFrac = (1.0 - ln(tan(latRad) + 1.0 / cos(latRad)) / PI) / 2.0 * n
    val cx = floor(xFrac).toInt()
    val cy = floor(yFrac).toInt()

    val mosaic = Bitmap.createBitmap(GRID * TILE_SIZE, GRID * TILE_SIZE, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(mosaic)
    for (dx in -1..1) {
        for (dy in -1..1) {
            val tile = fetchTile(client, ZOOM, cx + dx, cy + dy) ?: continue
            canvas.drawBitmap(tile, ((dx + 1) * TILE_SIZE).toFloat(), ((dy + 1) * TILE_SIZE).toFloat(), null)
        }
    }

    val px = ((1 + (xFrac - cx)) * TILE_SIZE).toInt()
    val py = ((1 + (yFrac - cy)) * TILE_SIZE).toInt()
    val half = CROP_SIZE / 2
    val left = (px - half).coerceIn(0, mosaic.width - CROP_SIZE)
    val top = (py - half).coerceIn(0, mosaic.height - CROP_SIZE)
    return Bitmap.createBitmap(mosaic, left, top, CROP_SIZE, CROP_SIZE)
}

private fun fetchTile(client: OkHttpClient, z: Int, x: Int, y: Int): Bitmap? {
    val request = Request.Builder()
        .url("https://tile.openstreetmap.org/$z/$x/$y.png")
        .header("User-Agent", "Rouxen/1.0")
        .build()
    return client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) return null
        BitmapFactory.decodeStream(response.body?.byteStream())
    }
}
