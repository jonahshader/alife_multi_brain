package com.jonahshader.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.FillViewport
import com.jonahshader.MultiBrain
import com.jonahshader.systems.math.CublasSystem
import com.jonahshader.systems.screen.ScreenManager
import com.jonahshader.systems.simulation.fluidsim.FluidSimParticles
import com.jonahshader.systems.ui.ScreenWindow
import ktx.app.KtxScreen
import ktx.graphics.use

class FluidSimScreen : KtxScreen {
    private val simCam = OrthographicCamera()
    private val simViewport = FillViewport(1920.0f, 1080.0f, simCam)

    private val visCam = OrthographicCamera()
    private val visViewport = FillViewport(1920.0f, 1080.0f, visCam)

    private val window = ScreenWindow(Vector2(1280f, 720f))

    private var visEnabled = false
    private var following = false

    private val fluidSim = FluidSimParticles(48)

    init {
        simCam.zoom = .08f
        simCam.position.x = fluidSim.worldSize/2f
        simCam.position.y = fluidSim.worldSize/2f
        fluidSim.addWall(Rectangle(fluidSim.worldSize/2f, 0f, 1f, fluidSim.worldSize.toFloat()))
        fluidSim.addWall(Rectangle(fluidSim.worldSize/2f - 3f, fluidSim.worldSize/2f - 1f, 3f, 1f))
        fluidSim.addWall(Rectangle(fluidSim.worldSize/2f - 3f, fluidSim.worldSize/2f + 1f, 3f, 1f))
        fluidSim.fill(15, 1f, Rectangle(fluidSim.worldSize/2f, 0f, fluidSim.worldSize/2f, fluidSim.worldSize.toFloat()))
        fluidSim.removeWall(fluidSim.worldSize/2, fluidSim.worldSize/2)
    }

    override fun render(delta: Float) {
        val speed = simCam.zoom * 500 * delta
        if (Gdx.input.isKeyPressed(Input.Keys.W)) simCam.translate(0f, speed)
        if (Gdx.input.isKeyPressed(Input.Keys.S)) simCam.translate(0f, -speed)
        if (Gdx.input.isKeyPressed(Input.Keys.A)) simCam.translate(-speed, 0f)
        if (Gdx.input.isKeyPressed(Input.Keys.D)) simCam.translate(speed, 0f)
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            CublasSystem.cublasShutdown()
            ScreenManager.switchTo(FluidSimScreen())
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) simCam.zoom /= 1.5f
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) simCam.zoom *= 1.5f
        if (Gdx.input.isKeyJustPressed(Input.Keys.V)) visEnabled = !visEnabled
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            CublasSystem.cublasShutdown()
            ScreenManager.pop()
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) following = !following

        simCam.update()
        visCam.update()
        ScreenUtils.clear(.1f, .1f, .1f, 1f)

        fluidSim.update(1/45f)
        println(1/delta)
        window.update(delta)
//        if (following)
//            simViewer.follow(simCam)

        simViewport.apply()
        MultiBrain.batch.use(simCam) {
//            MultiBrain.shapeDrawer.update()
//            fluidSim.render()
        }

        MultiBrain.shapeRenderer.projectionMatrix = simCam.combined
//        fluidSim.renderFields()
        fluidSim.renderParticles()

        window.render(MultiBrain.batch)
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