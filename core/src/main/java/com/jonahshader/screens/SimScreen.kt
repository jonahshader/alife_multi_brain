package com.jonahshader.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.FitViewport
import com.jonahshader.MultiBrain
import com.jonahshader.systems.brain.Network
import com.jonahshader.systems.brain.NetworkParams
import com.jonahshader.systems.brain.visualizer.NetworkVisualizer
import com.jonahshader.systems.brain.visualizer.NeuronGraphic
import com.jonahshader.systems.simulation.simple.SimpleSim
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

    private val simpleSim = SimpleSim()

    override fun render(delta: Float) {
        if (Gdx.input.isKeyPressed(Input.Keys.W)) camera.translate(0f, 1f)
        if (Gdx.input.isKeyPressed(Input.Keys.S)) camera.translate(0f, -1f)
        if (Gdx.input.isKeyPressed(Input.Keys.A)) camera.translate(-1f, 0f)
        if (Gdx.input.isKeyPressed(Input.Keys.D)) camera.translate(1f, 0f)

        camera.update()
        ScreenUtils.clear(.1f, .1f, .1f, 1f)
        viewport.apply()

        testNetwork.update(1/60.0f)
        netVisualizer.update(Vector2.Zero, 0.0f, 1/60.0f)
        simpleSim.update(delta)

        MultiBrain.batch.use(camera) {
            NeuronGraphic.MOUSE_POS = viewport.unproject(Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat()))
            simpleSim.render(MultiBrain.batch)
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