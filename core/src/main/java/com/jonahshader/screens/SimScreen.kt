package com.jonahshader.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.FitViewport
import com.jonahshader.MultiBrain
import com.jonahshader.systems.brain.Network
import com.jonahshader.systems.brain.neurons.NetworkParams
import com.jonahshader.systems.brain.visualizer.NetworkVisualizer
import com.jonahshader.systems.brain.visualizer.NeuronGraphic
import ktx.app.KtxScreen
import ktx.graphics.use
import java.util.*

class SimScreen : KtxScreen {
    private val camera = OrthographicCamera()
    private val viewport = FitViewport(640.0f, 360.0f, camera)

    private val rand = Random()
    private val netParams = NetworkParams()
    private val testNetwork = Network(3, 3, netParams, rand)

    private val netVisualizer = NetworkVisualizer(testNetwork)

    override fun render(delta: Float) {
        camera.update()
        ScreenUtils.clear(.1f, .1f, .1f, 1f)
        viewport.apply()

        testNetwork.update(1/60.0f)
        netVisualizer.update(Vector2.Zero, 0.0f, 1/60.0f)

        MultiBrain.batch.use(camera) {
            NeuronGraphic.MOUSE_POS = viewport.unproject(Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat()))
            netVisualizer.render(MultiBrain.batch)
        }
    }

    override fun show() {
        viewport.update(Gdx.graphics.width, Gdx.graphics.height)
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }
}