package com.jonahshader.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.FitViewport
import com.jonahshader.MultiBrain
import com.jonahshader.systems.brain.visualizer.NetworkVisualizer
import com.jonahshader.systems.brain.visualizer.NeuronGraphic
import com.jonahshader.systems.creatureparts.softbody.BrainSoftBody
import com.jonahshader.systems.screen.ScreenManager
import com.jonahshader.systems.simulation.softbodytravel.SoftBodyTravelSim
import ktx.app.KtxScreen
import ktx.graphics.use
import java.util.*
import kotlin.concurrent.thread

class SBCreatureTestScreen : KtxScreen {
    companion object {
        private const val SIM_DELTA_TIME = 1/20f
    }
    private val simCam = OrthographicCamera()
    private val simViewport = FitViewport(1920.0f, 1080.0f, simCam)

    private val visCam = OrthographicCamera()
    private val visViewport = FitViewport(1920.0f, 1080.0f, visCam)

    private val rand = Random()
    private var creature: BrainSoftBody? = null
    private var visualizer: NetworkVisualizer? = null

    private val maxSteps = 500
    private var stepsRemaining = maxSteps
    private val sim = SoftBodyTravelSim(800, maxSteps, SIM_DELTA_TIME)

    private var visEnabled = false
    private var running = true

    init {
        visCam.translate(640f, 0f)
        visCam.zoom = 1.2f

        sim.netParams.hiddenNeuronCountInit = 250
        sim.netParams.connectivityInit = .005f
        sim.bodyParams.gripperCountInit = 15
        sim.bodyParams.connectivityInit = .4f
        sim.setup()
        thread {
            while (running) {
                sim.runGeneration()
            }
            println("training thread stopped")
        }
    }

    override fun render(delta: Float) {
        val speed = simCam.zoom * 500 * delta
        if (Gdx.input.isKeyPressed(Input.Keys.W)) simCam.translate(0f, speed)
        if (Gdx.input.isKeyPressed(Input.Keys.S)) simCam.translate(0f, -speed)
        if (Gdx.input.isKeyPressed(Input.Keys.A)) simCam.translate(-speed, 0f)
        if (Gdx.input.isKeyPressed(Input.Keys.D)) simCam.translate(speed, 0f)
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            running = false
            ScreenManager.switchTo(SBCreatureTestScreen())
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) simCam.zoom /= 1.5f
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) simCam.zoom *= 1.5f
        if (Gdx.input.isKeyJustPressed(Input.Keys.V)) visEnabled = !visEnabled
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            running = false
            ScreenManager.pop()
        }

        simCam.update()
        visCam.update()
        ScreenUtils.clear(.1f, .1f, .1f, 1f)


        if (creature == null) {
            val genes = sim.getCopyOfBest()
            if (genes != null) {
                creature = BrainSoftBody(rand, genes)
                visualizer = NetworkVisualizer(creature!!.network)
            }
        } else {
            stepsRemaining--
            if (stepsRemaining <= 0) {
                stepsRemaining = maxSteps
                creature = BrainSoftBody(rand, sim.getCopyOfBest()!!)
                visualizer = NetworkVisualizer(creature!!.network)
            }
        }

        creature?.update(Vector2.Zero, 0f, SIM_DELTA_TIME)
        visualizer?.update(Vector2.Zero, 0f, 1/60f)

        if (visEnabled) {
            visViewport.apply()
            MultiBrain.batch.use(visCam) {

                // draw visualizer
                visualizer?.render(it, visCam)
            }
        }


        simViewport.apply()
        MultiBrain.batch.use(simCam) {
            // draw origin
            MultiBrain.shapeDrawer.setColor(1f, 1f, 1f, 1f)
            MultiBrain.shapeDrawer.filledCircle(0f, 0f, 8f)

            // set mouse pos for some reason
            NeuronGraphic.MOUSE_POS = simViewport.unproject(Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat()))
            // draw creature
            creature?.render(it, simCam)

//            TextRenderer.begin(MultiBrain.batch, viewport, TextRenderer.Font.NORMAL, 32f, 0f, camera.zoom)
//            TextRenderer.drawText(0f, 0f, "FPS: " + (1/delta))
//            TextRenderer.end()
        }
    }

    override fun show() {
        simViewport.update(Gdx.graphics.width, Gdx.graphics.height)
        visViewport.update(Gdx.graphics.width, Gdx.graphics.height)
    }

    override fun resize(width: Int, height: Int) {
        simViewport.update(width, height)
        visViewport.update(width, height)
    }
}