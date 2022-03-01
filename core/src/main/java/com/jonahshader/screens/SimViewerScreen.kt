package com.jonahshader.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.FillViewport
import com.jonahshader.MultiBrain
import com.jonahshader.systems.math.CublasSystem
import com.jonahshader.systems.screen.ScreenManager
import com.jonahshader.systems.simulation.foodgrid.SimViewer
import com.jonahshader.systems.training.EvolutionStrategies
import com.jonahshader.systems.ui.Plot
import com.jonahshader.systems.ui.ScreenWindow
import ktx.app.KtxScreen
import ktx.graphics.use

class SimViewerScreen(private val sim: EvolutionStrategies, private val framesPerStep: Int) : KtxScreen {
    private val simCam = OrthographicCamera()
    private val simViewport = FillViewport(1920.0f, 1080.0f, simCam)

    private val visCam = OrthographicCamera()
    private val visViewport = FillViewport(1920.0f, 1080.0f, visCam)

    private val window = ScreenWindow(Vector2(1280f, 720f))

    private val simViewer = SimViewer(sim)

    private var visEnabled = false
    private var following = false
    private var currFrame = 0

    init {
        //TODO: compare dense to cuda. should be 1 to 1 identical (after tanh is solved or removed temporarily)
        val plot = Plot("Iteration", "Fitness", "Food Creature Fitness", Vector2())
        window.addChildWindow(plot)
        plot.addTrend(Plot.Trend("todo: autogen from foodsim params", Color.BLUE, false, mode = Plot.Mode.LINE))
        sim.addFitnessCallback {
            plot.addDatum("todo: autogen from foodsim params", it)
        }
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
            sim.dispose()
            CublasSystem.cublasShutdown()
            ScreenManager.switchTo(FoodCreatureTestScreen())
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) simCam.zoom /= 1.5f
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) simCam.zoom *= 1.5f
        if (Gdx.input.isKeyJustPressed(Input.Keys.V)) visEnabled = !visEnabled
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            sim.stop()
            sim.dispose()
            CublasSystem.cublasShutdown()
            ScreenManager.pop()
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) following = !following

        simCam.update()
        visCam.update()
        ScreenUtils.clear(.1f, .1f, .1f, 1f)

        if (currFrame == 0) {
            currFrame += framesPerStep
            simViewer.update()
        }
        window.update(delta)
        if (following)
            simViewer.follow(simCam)

        simViewport.apply()
        MultiBrain.batch.use(simCam) {
            MultiBrain.shapeDrawer.update()
            simViewer.render()
        }

        window.render(MultiBrain.batch)
        currFrame--
    }

    override fun show() {
        simViewport.update(Gdx.graphics.width, Gdx.graphics.height)
        visViewport.update(Gdx.graphics.width, Gdx.graphics.height)
        window.show()
    }

    override fun resize(width: Int, height: Int) {
        simViewport.update(width, height)
        visViewport.update(width, height)
        window.resize(width, height)
    }
}