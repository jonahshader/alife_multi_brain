package com.jonahshader.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.FitViewport
import com.jonahshader.MultiBrain
import com.jonahshader.systems.brain.densecyclic.DenseCyclicNetwork
import com.jonahshader.systems.brain.makeDenseNetworkBuilder
import com.jonahshader.systems.screen.ScreenManager
import com.jonahshader.systems.simulation.foodgrid.FoodCreature
import com.jonahshader.systems.simulation.foodgrid.FoodGrid
import com.jonahshader.systems.simulation.foodgrid.FoodSim
import com.jonahshader.systems.simulation.foodgrid.SimViewer
import ktx.app.KtxScreen
import ktx.graphics.use

class FoodCreatureTestScreen : KtxScreen {
    private val simCam = OrthographicCamera()
    private val simViewport = FitViewport(1920.0f, 1080.0f, simCam)

    private val visCam = OrthographicCamera()
    private val visViewport = FitViewport(1920.0f, 1080.0f, visCam)

    private val sim = FoodSim(makeDenseNetworkBuilder(10), 100, 10, 500, 1/20f)
    private val simViewer = SimViewer(sim)

    private var visEnabled = false

    init {
        sim.start()
    }

    override fun render(delta: Float) {
        val speed = simCam.zoom * 500 * delta
        if (Gdx.input.isKeyPressed(Input.Keys.W)) simCam.translate(0f, speed)
        if (Gdx.input.isKeyPressed(Input.Keys.S)) simCam.translate(0f, -speed)
        if (Gdx.input.isKeyPressed(Input.Keys.A)) simCam.translate(-speed, 0f)
        if (Gdx.input.isKeyPressed(Input.Keys.D)) simCam.translate(speed, 0f)
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            sim.stop()
            ScreenManager.switchTo(FoodCreatureTestScreen())
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) simCam.zoom /= 1.5f
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) simCam.zoom *= 1.5f
        if (Gdx.input.isKeyJustPressed(Input.Keys.V)) visEnabled = !visEnabled
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            sim.stop()
            ScreenManager.pop()
        }

        simCam.update()
        visCam.update()
        ScreenUtils.clear(.1f, .1f, .1f, 1f)

        simViewer.update()

        simViewport.apply()
        MultiBrain.shapeDrawer.update()
        MultiBrain.batch.use(simCam) {
            simViewer.render()
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