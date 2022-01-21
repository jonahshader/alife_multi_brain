package com.jonahshader.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.FitViewport
import com.jonahshader.MultiBrain
import com.jonahshader.systems.brain.NetworkParams
import com.jonahshader.systems.brain.visualizer.NeuronGraphic
import com.jonahshader.systems.creatureparts.softbody.BrainSoftBody
import com.jonahshader.systems.creatureparts.softbody.SoftBodyParams
import com.jonahshader.systems.screen.ScreenManager
import com.jonahshader.systems.simulation.softbodytravel.SoftBodyTravelSim
import com.jonahshader.systems.ui.TextRenderer
import ktx.app.KtxScreen
import ktx.graphics.use
import java.util.*
import kotlin.concurrent.thread

class SBCreatureTestScreen : KtxScreen {
    companion object {
        private const val SIM_DELTA_TIME = 1/20f
    }
    private val camera = OrthographicCamera()
    private val viewport = FitViewport(1920.0f, 1080.0f, camera)

    private val rand = Random()
    private var creature: BrainSoftBody? = null

    private val maxSteps = 500
    private var stepsRemaining = maxSteps
    private val sim = SoftBodyTravelSim(500, maxSteps, SIM_DELTA_TIME)

    init {
        sim.netParams.hiddenNeuronCountInit = 100
        sim.bodyParams.gripperCountInit = 4
        sim.bodyParams.connectivityInit = 1f
        sim.setup()
        thread {
            while (true) {
                sim.runGeneration()
            }

        }
    }

    override fun render(delta: Float) {
        val speed = camera.zoom * 500 * delta
        if (Gdx.input.isKeyPressed(Input.Keys.W)) camera.translate(0f, speed)
        if (Gdx.input.isKeyPressed(Input.Keys.S)) camera.translate(0f, -speed)
        if (Gdx.input.isKeyPressed(Input.Keys.A)) camera.translate(-speed, 0f)
        if (Gdx.input.isKeyPressed(Input.Keys.D)) camera.translate(speed, 0f)
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) ScreenManager.switchTo(SBCreatureTestScreen())
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) camera.zoom /= 1.5f
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) camera.zoom *= 1.5f

        camera.update()
        ScreenUtils.clear(.1f, .1f, .1f, 1f)
        viewport.apply()

        if (creature == null) {
            val genes = sim.getCopyOfBest()
            if (genes != null) {
                creature = BrainSoftBody(rand, genes)
            }
        } else {
            stepsRemaining--
            if (stepsRemaining <= 0) {
                stepsRemaining = maxSteps
                creature = BrainSoftBody(rand, sim.getCopyOfBest()!!)
            }
        }

        creature?.update(Vector2.Zero, 0f, SIM_DELTA_TIME)

        MultiBrain.batch.use(camera) {
            NeuronGraphic.MOUSE_POS = viewport.unproject(Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat()))
            creature?.render(MultiBrain.batch)

//            TextRenderer.begin(MultiBrain.batch, viewport, TextRenderer.Font.NORMAL, 32f, 0f)
//            TextRenderer.drawText(0f, 0f, "FPS: " + (1/delta))
//            TextRenderer.end()
        }
    }

    override fun show() {
        viewport.update(Gdx.graphics.width, Gdx.graphics.height)
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }
}