package com.jonahshader.systems.ui

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.ScalingViewport
import com.jonahshader.MultiBrain

class Slider(private val label: String, private val min: Float, private val max: Float, default: Float = (min + max)/2, pos: Vector2, size: Vector2 = Vector2(80f, 20f), private val callback: (Float) -> Unit, ) : Window(pos, size, size) {
    private var current = default
    private val p: Float
        get() = (current - min) / (max - min)

    init {
        callback(current)
        draggingEnabled = false
    }
    override fun render(cam: OrthographicCamera, viewport: ScalingViewport) {
        drawContainer()
        MultiBrain.shapeDrawer.setColor(1f, 1f, 1f, 1f)
        MultiBrain.shapeDrawer.filledRectangle(globalPosition.x, globalPosition.y, size.x * p, size.y)

        with(TextRenderer) {
            begin(MultiBrain.batch, viewport, TextRenderer.Font.NORMAL, size.y / 3f, 0f, cam.zoom)
            color.set(.5f, .5f, .5f, 1f)
            drawTextCentered(globalPosition.x + size.x/2, globalPosition.y + size.y/2, "$label: $current")
            end()
        }

    }

    override fun customMouseHandle(mouseStateChange: Boolean, mouseDown: Boolean, mousePos: Vector2): Boolean {
        return if (mouseDown &&
            mousePos.x in globalPosition.x..globalPosition.x + size.x &&
            mousePos.y in globalPosition.y..globalPosition.y + size.y) {
            updateValue(mousePos.x)
            true
        } else {
            false
        }
    }

    private fun updateValue(mouseX: Float) {
        val pTemp = (mouseX - globalPosition.x) / size.x
        current = min + pTemp * (max - min)
        callback(current)
    }
}