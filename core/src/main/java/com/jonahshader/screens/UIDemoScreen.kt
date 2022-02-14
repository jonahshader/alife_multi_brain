package com.jonahshader.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.FitViewport
import com.jonahshader.MultiBrain
import com.jonahshader.systems.ui.Window
import ktx.app.KtxScreen
import ktx.graphics.use

class UIDemoScreen : KtxScreen {
    private val camera = OrthographicCamera()
    private val viewport = FitViewport(1280f, 720f, camera)
    private val window = Window(Vector2(), Vector2(100f, 100f))

    private var pMouseDown = false

    init {

    }

    override fun show() {
        viewport.update(Gdx.graphics.width, Gdx.graphics.height)
    }

    override fun render(delta: Float) {
        ScreenUtils.clear(.25f, .25f, .25f, 1f)

        val stateChanged = pMouseDown != Gdx.input.isTouched
        pMouseDown = Gdx.input.isTouched

        val mouseWorldPos = viewport.project(Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
        window.handleMouseInput(stateChanged, Gdx.input.isTouched, Vector2(mouseWorldPos.x, mouseWorldPos.y))
        window.update(delta)

        viewport.apply()
        MultiBrain.batch.use(camera) {
            window.render(camera)
        }
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }
}