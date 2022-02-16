package com.jonahshader.systems.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.ScalingViewport
import com.jonahshader.MultiBrain
import ktx.graphics.use

class ScreenWindow(viewportSize: Vector2) {
    private val cam = OrthographicCamera()
    private val viewport = FitViewport(viewportSize.x, viewportSize.y, cam)
    private val window = Window(viewport)

    private var pMouseDown = false

    /**
     * don't call within batch.begin/end
     */
    fun update(dt: Float) {
        val stateChanged = pMouseDown != Gdx.input.isTouched
        pMouseDown = Gdx.input.isTouched
        val mouseWorldPos = viewport.unproject(Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
        window.handleMouseInput(stateChanged, Gdx.input.isTouched, Vector2(mouseWorldPos.x, mouseWorldPos.y))
        window.update(dt)
    }

    fun render(batch: SpriteBatch) {
        viewport.apply()
        batch.use(cam) {
            MultiBrain.shapeDrawer.update()
            window.render(cam, viewport)
        }
    }

    fun addChildWindow(childWindow: Window) {
        window.addChildWindow(childWindow)
    }

    fun show() {
        resize(Gdx.graphics.width, Gdx.graphics.height)
    }

    fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }
}