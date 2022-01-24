package com.jonahshader.systems.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ScalingViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.jonahshader.systems.assets.Assets

object TextRenderer {
    private var batch: Batch? = null
    private var viewport: Viewport? = null
    private var font: BitmapFont? = null
    var color: Color = Color.WHITE

    enum class Font {
        LIGHT,
        NORMAL,
        HEAVY
    }

    private fun fontToBitmapFont(font: Font) : BitmapFont {
        return when (font) {
            Font.LIGHT -> Assets.lightFont
            Font.NORMAL -> Assets.normalFont
            Font.HEAVY -> Assets.heavyFont
        }
    }

    private fun fontToSpread(font: Font) : Float {
        return when (font) {
            Font.LIGHT -> Assets.LIGHT_FONT_SPREAD
            Font.NORMAL -> Assets.NORMAL_FONT_SPREAD
            Font.HEAVY -> Assets.HEAVY_FONT_SPREAD
        }
    }

    private fun calculateScale(size: Float, font: Font) : Float {
        return when (font) {
            Font.LIGHT -> size / Assets.LIGHT_FONT_SIZE
            Font.NORMAL -> size / Assets.NORMAL_FONT_SIZE
            Font.HEAVY -> size / Assets.HEAVY_FONT_SIZE
        }
    }

    fun begin(batch: Batch, viewport: ScalingViewport, font: Font, size: Float, boldness: Float, cameraZoom: Float = 1f) {
        TextRenderer.batch = batch
        TextRenderer.viewport = viewport
        TextRenderer.font = fontToBitmapFont(font)
        val scale = calculateScale(size, font)
        val screenScale = viewport.scaling.apply(viewport.worldWidth, viewport.worldHeight, Gdx.graphics.width.toFloat(),
            Gdx.graphics.height.toFloat()
        ).x / viewport.worldWidth
        TextRenderer.font!!.setUseIntegerPositions(false)
        TextRenderer.font!!.data.setScale(scale)
        batch.shader = Assets.dffShader
        Assets.dffShader.setUniformf("p_distOffset", boldness)
        Assets.dffShader.setUniformf("p_spread", fontToSpread(font))
        Assets.dffShader.setUniformf("p_renderScale", scale * screenScale / cameraZoom)
    }

    fun end() {
        batch!!.shader = null
    }

    fun drawTextCentered(x: Float, y: Float, text: String, shadowDistance: Float, shadowOpacity: Float) {
        font?.color?.set(0f, 0f, 0f, color.a * shadowOpacity)
        font?.draw(batch!!, text, x, y + font!!.capHeight/2f, 0f, Align.center, false)
        updateColor()
        font?.draw(batch!!, text, x + shadowDistance, y + (font!!.capHeight/2f) + shadowDistance, 0f, Align.center, false)
    }

    fun drawTextCentered(x: Float, y: Float, text: String) {
        updateColor()
        font?.draw(batch!!, text, x, y + font!!.capHeight/2f, 0f, Align.center, false)
    }

    fun drawText(x: Float, y: Float, text: String) {
        updateColor()
        font?.draw(batch!!, text, x, y + font!!.capHeight, 0f, Align.left, false)
    }

    private fun updateColor() {
        font?.color = color
    }
}