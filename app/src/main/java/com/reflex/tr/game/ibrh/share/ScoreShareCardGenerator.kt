package com.reflex.tr.game.ibrh.share

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface

/**
 * Draws the shareable score card onto a [Canvas]. Rendering a Compose tree would need a live
 * window and a measured layout; this poster is fixed-size, so Canvas keeps it lifecycle-free.
 */
object ScoreShareCardGenerator {

    const val WIDTH = 1080
    const val HEIGHT = 1920

    private const val MARGIN = 84f

    // Sampled from ReflexGamePalette so the card matches the app.
    private const val BG_TOP = 0xFF0B1330.toInt()
    private const val BG_BOTTOM = 0xFF3A2280.toInt()
    private const val NEON_BLUE = 0xFF4A6CF7.toInt()
    private const val NEON_PURPLE = 0xFF7A3DFF.toInt()
    private const val GOLD = 0xFFFFC857.toInt()
    private const val CORAL = 0xFFFF2D35.toInt()
    private const val TEXT_PRIMARY = 0xFFF4F7FF.toInt()
    private const val TEXT_SECONDARY = 0xFFB7C2E8.toInt()
    private const val CARD_FILL = 0x2EFFFFFF
    private const val CARD_STROKE = 0x5CFFFFFF

    /**
     * Returns null instead of throwing when the bitmap cannot be allocated, so a low-memory device
     * degrades to "sharing failed" rather than a crash.
     */
    fun generate(data: ScoreShareData): Bitmap? = runCatching {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).drawCard(data)
        bitmap
    }.getOrNull()

    private fun Canvas.drawCard(data: ScoreShareData) {
        drawBackground()
        val afterHeader = drawHeader(data, startY = 200f)
        val afterScore = drawScoreBlock(data, afterHeader)
        drawStats(data, afterScore)
        drawFooter(data)
    }

    private fun Canvas.drawBackground() {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.shader = LinearGradient(
            0f, 0f, 0f, HEIGHT.toFloat(),
            BG_TOP, BG_BOTTOM, Shader.TileMode.CLAMP
        )
        drawRect(0f, 0f, WIDTH.toFloat(), HEIGHT.toFloat(), paint)

        // Two soft glows so the flat gradient reads as the game's neon arena.
        paint.shader = RadialGradient(
            WIDTH * 0.82f, HEIGHT * 0.16f, 620f,
            intArrayOf(0x59FF2D35, 0x00FF2D35), null, Shader.TileMode.CLAMP
        )
        drawRect(0f, 0f, WIDTH.toFloat(), HEIGHT.toFloat(), paint)
        paint.shader = RadialGradient(
            WIDTH * 0.12f, HEIGHT * 0.78f, 700f,
            intArrayOf(0x4D4A6CF7, 0x004A6CF7), null, Shader.TileMode.CLAMP
        )
        drawRect(0f, 0f, WIDTH.toFloat(), HEIGHT.toFloat(), paint)
    }

    private fun Canvas.drawHeader(data: ScoreShareData, startY: Float): Float {
        var y = startY
        drawCenteredText(data.labels.title.uppercase(), y, 82f, GOLD, Typeface.DEFAULT_BOLD, letterSpacing = 0.16f)
        y += 84f
        if (data.labels.slogan.isNotBlank()) {
            drawCenteredText(data.labels.slogan, y, 44f, TEXT_SECONDARY, Typeface.DEFAULT)
            y += 70f
        }
        return y + 40f
    }

    private fun Canvas.drawScoreBlock(data: ScoreShareData, startY: Float): Float {
        var y = startY
        val cardHeight = if (data.isNewBestScore) 520f else 452f
        drawPanel(MARGIN, y, WIDTH - MARGIN, y + cardHeight)

        var inner = y + 96f
        if (data.isNewBestScore && data.labels.newRecord.isNotBlank()) {
            drawCenteredText(data.labels.newRecord.uppercase(), inner, 40f, GOLD, Typeface.DEFAULT_BOLD, letterSpacing = 0.1f)
            inner += 74f
        }
        drawCenteredText(data.labels.score.uppercase(), inner, 40f, TEXT_SECONDARY, Typeface.DEFAULT, letterSpacing = 0.2f)
        inner += 168f
        drawCenteredText(
            text = data.score.toString(),
            baselineY = inner,
            size = 216f,
            color = if (data.isNewBestScore) GOLD else TEXT_PRIMARY,
            typeface = Typeface.DEFAULT_BOLD,
            glow = if (data.isNewBestScore) GOLD else NEON_BLUE
        )
        inner += 92f
        drawCenteredText(data.modeName, inner, 52f, CORAL, Typeface.DEFAULT_BOLD)

        return y + cardHeight + 56f
    }

    private fun Canvas.drawStats(data: ScoreShareData, startY: Float) {
        val rows = buildList {
            add(data.labels.bestScore to data.bestScore.toString())
            add(data.labels.accuracy to data.accuracyText)
            add(data.labels.combo to "x${data.maxCombo}")
            if (data.earnedCoins > 0) add(data.labels.coins to "+${data.earnedCoins}")
        }.filter { it.first.isNotBlank() }

        val rowHeight = 116f
        val height = rowHeight * rows.size + 56f
        drawPanel(MARGIN, startY, WIDTH - MARGIN, startY + height)

        val left = MARGIN + 56f
        val right = WIDTH - MARGIN - 56f
        var y = startY + 96f
        rows.forEach { (label, value) ->
            // The value is laid out first and the label gets whatever is left, so a long localised
            // label ("Coins earned") shortens instead of running into the number.
            val valueWidth = measureText(value, 54f, Typeface.DEFAULT_BOLD)
            val labelWidth = right - left - valueWidth - 32f
            drawText(
                text = fitText(label, labelWidth, 46f, Typeface.DEFAULT),
                x = left,
                baselineY = y,
                size = 46f,
                color = TEXT_SECONDARY,
                typeface = Typeface.DEFAULT,
                align = Paint.Align.LEFT
            )
            drawText(value, right, y, 54f, TEXT_PRIMARY, Typeface.DEFAULT_BOLD, align = Paint.Align.RIGHT)
            y += rowHeight
        }

        if (data.labels.theme.isNotBlank()) {
            drawCenteredText(data.labels.theme, startY + height + 68f, 38f, TEXT_SECONDARY, Typeface.DEFAULT)
        }
    }

    private fun Canvas.drawFooter(data: ScoreShareData) {
        if (data.labels.challenge.isNotBlank()) {
            drawCenteredMultiline(data.labels.challenge, HEIGHT - 300f, 54f, GOLD, Typeface.DEFAULT_BOLD)
        }
        if (data.labels.storeHint.isNotBlank()) {
            drawCenteredMultiline(data.labels.storeHint, HEIGHT - 130f, 38f, TEXT_SECONDARY, Typeface.DEFAULT)
        }
    }

    private fun Canvas.drawPanel(left: Float, top: Float, right: Float, bottom: Float) {
        val rect = RectF(left, top, right, bottom)
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = CARD_FILL }
        drawRoundRect(rect, 56f, 56f, fill)
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            shader = LinearGradient(left, top, right, bottom, NEON_BLUE, NEON_PURPLE, Shader.TileMode.CLAMP)
            alpha = 0xB0
        }
        drawRoundRect(rect, 56f, 56f, stroke)
        stroke.shader = null
        stroke.color = CARD_STROKE
        stroke.strokeWidth = 1.5f
        drawRoundRect(rect, 56f, 56f, stroke)
    }

    private fun Canvas.drawCenteredText(
        text: String,
        baselineY: Float,
        size: Float,
        color: Int,
        typeface: Typeface,
        letterSpacing: Float = 0f,
        glow: Int? = null
    ) {
        drawText(text, WIDTH / 2f, baselineY, size, color, typeface, Paint.Align.CENTER, letterSpacing, glow)
    }

    private fun Canvas.drawText(
        text: String,
        x: Float,
        baselineY: Float,
        size: Float,
        color: Int,
        typeface: Typeface,
        align: Paint.Align,
        letterSpacing: Float = 0f,
        glow: Int? = null
    ) {
        if (text.isBlank()) return
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = size
            this.typeface = typeface
            textAlign = align
            this.letterSpacing = letterSpacing
        }
        if (glow != null) {
            // setShadowLayer needs a software layer, which a plain Bitmap canvas already is.
            paint.setShadowLayer(size * 0.28f, 0f, 0f, glow)
        }
        drawText(text, x, baselineY, paint)
    }

    private fun measureText(text: String, size: Float, typeface: Typeface): Float {
        return Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            this.typeface = typeface
        }.measureText(text)
    }

    /** Trims and ellipsises [text] until it fits [maxWidth]. */
    private fun fitText(text: String, maxWidth: Float, size: Float, typeface: Typeface): String {
        if (maxWidth <= 0f) return ""
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            this.typeface = typeface
        }
        if (paint.measureText(text) <= maxWidth) return text
        var end = text.length
        while (end > 0 && paint.measureText(text.take(end) + "…") > maxWidth) {
            end--
        }
        return if (end <= 0) "" else text.take(end).trimEnd() + "…"
    }

    /** Wraps on spaces so a long localised sentence never runs past the card edge. */
    private fun Canvas.drawCenteredMultiline(
        text: String,
        baselineY: Float,
        size: Float,
        color: Int,
        typeface: Typeface
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            this.typeface = typeface
        }
        val maxWidth = WIDTH - MARGIN * 2 - 40f
        val lines = mutableListOf<String>()
        var current = StringBuilder()
        text.split(' ').forEach { word ->
            val candidate = if (current.isEmpty()) word else "$current $word"
            if (paint.measureText(candidate) <= maxWidth || current.isEmpty()) {
                current = StringBuilder(candidate)
            } else {
                lines.add(current.toString())
                current = StringBuilder(word)
            }
        }
        if (current.isNotEmpty()) lines.add(current.toString())

        val lineHeight = size * 1.3f
        var y = baselineY - (lines.size - 1) * lineHeight
        lines.forEach { line ->
            drawCenteredText(line, y, size, color, typeface)
            y += lineHeight
        }
    }
}
