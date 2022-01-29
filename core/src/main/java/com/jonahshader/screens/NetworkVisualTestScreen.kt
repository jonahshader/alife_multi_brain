package com.jonahshader.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.FitViewport
import com.jonahshader.MultiBrain
import com.jonahshader.systems.brain.CyclicNetwork
import com.jonahshader.systems.brain.NetworkParams
import com.jonahshader.systems.brain.visualizer.NetworkVisualizer
import com.jonahshader.systems.brain.visualizer.NeuronGraphic
import com.jonahshader.systems.screen.ScreenManager
import ktx.app.KtxScreen
import ktx.graphics.use
import java.util.*

class NetworkVisualTestScreen : KtxScreen {
    private val camera = OrthographicCamera()
    private val viewport = FitViewport(1920.0f, 1080.0f, camera)

    private val rand = Random()
    private val netParams = NetworkParams()
    private val testNetwork = CyclicNetwork(6, 6, netParams, rand)

    private val netVisualizer = NetworkVisualizer(testNetwork)

//    private val simpleSim = SimpleSim()

    override fun render(delta: Float) {
        val speed = camera.zoom * 500 * delta
        if (Gdx.input.isKeyPressed(Input.Keys.W)) camera.translate(0f, speed)
        if (Gdx.input.isKeyPressed(Input.Keys.S)) camera.translate(0f, -speed)
        if (Gdx.input.isKeyPressed(Input.Keys.A)) camera.translate(-speed, 0f)
        if (Gdx.input.isKeyPressed(Input.Keys.D)) camera.translate(speed, 0f)
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) ScreenManager.switchTo(NetworkVisualTestScreen())
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) camera.zoom /= 1.5f
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) camera.zoom *= 1.5f
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) ScreenManager.pop()

        camera.update()
        ScreenUtils.clear(.1f, .1f, .1f, 1f)
        viewport.apply()

        testNetwork.update(1/60.0f)
        netVisualizer.update(Vector2.Zero, 0.0f, 1/60.0f)
//        simpleSim.update(delta)

        MultiBrain.shapeDrawer.update()
        MultiBrain.batch.use(camera) {
            NeuronGraphic.MOUSE_POS = viewport.unproject(Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat()))
//            simpleSim.render(MultiBrain.batch)
            netVisualizer.render(MultiBrain.batch, camera)
        }
    }

    override fun show() {
        viewport.update(Gdx.graphics.width, Gdx.graphics.height)
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }
}