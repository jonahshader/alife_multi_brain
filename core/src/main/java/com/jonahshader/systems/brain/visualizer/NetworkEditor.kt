package com.jonahshader.systems.brain.visualizer

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.ScalingViewport
import com.badlogic.gdx.utils.viewport.ScreenViewport
import ktx.app.KtxInputAdapter

class NetworkEditor : KtxInputAdapter {
    private val visCam = OrthographicCamera()
    private val hudCam = OrthographicCamera()
    private val visViewport = FitViewport(1920f, 1080f, visCam)
    private val hudViewport = ScreenViewport(hudCam)

    private var visualizer: NetworkVisualizer? = null

    init {
        show()
    }


    // starts and ends a batch, don't call within batch.use
    fun render() {

    }

    fun update(dt: Float) {

    }

    fun show() {
        resize(Gdx.graphics.width, Gdx.graphics.height)
    }

    fun resize(width: Int, height: Int) {
        visViewport.update(width, height)
        hudViewport.update(width, height)
    }
}