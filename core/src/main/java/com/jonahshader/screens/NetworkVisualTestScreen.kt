package com.jonahshader.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.FitViewport
import com.jonahshader.MultiBrain
import com.jonahshader.systems.neuralnet.cyclic.CyclicNetwork
import com.jonahshader.systems.neuralnet.cyclic.CyclicNetworkParams
import com.jonahshader.systems.neuralnet.neurons.NeuronName
import com.jonahshader.systems.neuralnet.visualizer.NetworkVisualizer
import com.jonahshader.systems.neuralnet.visualizer.NeuronGraphic
import com.jonahshader.systems.screen.ScreenManager
import ktx.app.KtxScreen
import ktx.graphics.use
import java.util.*

class NetworkVisualTestScreen : KtxScreen {
    private val camera = OrthographicCamera()
    private val viewport = FitViewport(1920.0f, 1080.0f, camera)

    private val rand = Random()
    private val netParams = CyclicNetworkParams()
    private val testNetwork: CyclicNetwork

    private val netVisualizer: NetworkVisualizer

//    private val simpleSim = SimpleSim()

    init {
        netParams.hiddenNeuronTypes = listOf(NeuronName.Washboard)
        netParams.connectivityInit = 0.5f
        testNetwork = CyclicNetwork(6, 6, netParams, rand)
        netVisualizer = NetworkVisualizer(testNetwork)
    }

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
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT_BRACKET)) netVisualizer.incrementSpringLength(-25f)
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT_BRACKET)) netVisualizer.incrementSpringLength(25f)

        camera.update()
        ScreenUtils.clear(.1f, .1f, .1f, 1f)
        viewport.apply()

        testNetwork.update(1/1000.0f)
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