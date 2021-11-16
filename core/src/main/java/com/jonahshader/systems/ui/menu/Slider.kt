package com.jonahshader.systems.ui.menu

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.ScalingViewport
import com.jonahshader.systems.assets.Assets
import com.jonahshader.systems.sound.SoundSystem
import com.jonahshader.systems.ui.CustomShapes
import com.jonahshader.systems.ui.TextRenderer
import space.earlygrey.shapedrawer.ShapeDrawer
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class Slider : MenuItem {

    lateinit var sliderAction : (i: Float) -> Unit
    var position = 1F

    constructor(action: (i: Float) -> Unit, x: Float, y: Float, width: Float, height: Float, label: String, font: TextRenderer.Font, camera: Camera, initialPos: Float)
            : super({}, x, y, width, height, label, font, camera){
        sliderAction = action
        position = initialPos
    }

    constructor(action: (i: Float) -> Unit, label: String, previousMenuItem: MenuItem, font: TextRenderer.Font, camera: Camera, initialPos: Float)
            : super({}, label, previousMenuItem, font, camera) {
        sliderAction = action
        position = initialPos
    }

    override fun run(offset: Vector2, dt: Float, viewport: ScalingViewport) {
        val xo = offset.x + x
        val yo = offset.y + y
        val m = mouseWorld(viewport)
        if (m.x >= xo && m.y >= yo && m.x <= xo + width && m.y <= yo + height) {
            if (Gdx.input.justTouched()) {
                SoundSystem.playSoundStandalone(Assets.manager.get(Assets.MENU_OPEN_SOUND, Sound::class.java), .8f, 0f)
                position = (m.x-xo)/width
                position = min(position, 1f)
                position = max(position, 0f)
                sliderAction(position)
            }
            if (!mouseOver) {
                mouseOver = true
                // play mouseOver sound
                SoundSystem.playSoundStandalone(Assets.manager.get(Assets.MENU_MOUSE_OVER_SOUND, Sound::class.java), .8f, 0f)
            }

            progress += dt * progressPerSecond
        } else {
            mouseOver = false
            progress -= dt * progressPerSecond
        }
        progress = progress.coerceIn(0f, 1f)
    }

    override fun draw(batch: SpriteBatch, shapeDrawer: ShapeDrawer, viewport: ScalingViewport, offset: Vector2) {
        val xo = offset.x + x
        val yo = offset.y + y

        shapeDrawer.setColor(0.0f, 0.0f, 0.0f, .5f)
        CustomShapes.filledRoundedRect(shapeDrawer, xo, yo + height/2, width, 10f, 6f)

        val progressMapped = progress.pow(1/2f)

        shapeDrawer.setColor(0.8f, 0.8f, 0.8f, 1f)
        CustomShapes.filledRoundedRect(shapeDrawer, xo + MOUSE_OVER_INDENT * progressMapped, yo + MOUSE_OVER_INDENT * progressMapped + height/2, width, 10f, 6f)
        shapeDrawer.setColor(0.4f, 0.4f, 0.4f, 1f)
        CustomShapes.filledRoundedRect(shapeDrawer, xo + MOUSE_OVER_INDENT * progressMapped+position*width-7.5f, yo + MOUSE_OVER_INDENT * progressMapped + height/2-2, 15f, 15f, 6f)
        TextRenderer.begin(batch, viewport, font, height * .25f, 0.05f)
        TextRenderer.color = Color.WHITE
        TextRenderer.drawTextCentered(xo + (width/2f) + MOUSE_OVER_INDENT * progressMapped, yo + MOUSE_OVER_INDENT * progressMapped + height/2 - 25, label, height * (0.04f + progressMapped/32f), .75f)
        TextRenderer.end()


    }
}