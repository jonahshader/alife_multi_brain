package com.jonahshader.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.FitViewport
import com.jonahshader.MultiBrain
import com.jonahshader.systems.ui.Plot
import com.jonahshader.systems.ui.Window
import com.jonahshader.systems.utils.Rand
import ktx.app.KtxScreen
import ktx.graphics.use
import ktx.math.div
import ktx.math.minus
import ktx.math.times
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

class UIDemoScreen : KtxScreen {
    private val camera = OrthographicCamera()
    private val viewport = FitViewport(1280f, 720f, camera)
    private val window = Window(Vector2(-viewport.worldWidth/2f, -viewport.worldHeight/2f), Vector2(viewport.worldWidth, viewport.worldHeight), Vector2(20f, 20f))

    private var pMouseDown = false

    init {
        window.bodyColor.a = 0f
        window.edgeColor.a = 0f
        window.draggingEnabled = false
        window.resizingEnabled = false
//        makeChildren(2, 1, window)
        val plot = Plot("X values", "Y values", "Title", pos = Vector2())
//        plot.addTrend(Plot.Trend("Test Data", Color.WHITE, true))
//        for (i in 0 until 100) {
//            plot.addDatum("Test Data", Vector2(Rand.randx.nextFloat(), Rand.randx.nextGaussian().toFloat()))
//        }

        plot.addTrend(Plot.Trend("Scatter", Color.BLUE, false, mode = Plot.Mode.POINT))
        for (i in 0 until 1000) {
            plot.addDatum("Scatter", Vector2(Rand.randx.nextGaussian().toFloat(), Rand.randx.nextGaussian().toFloat()))
        }

        plot.addTrend(Plot.Trend("Scatter2", Color.RED, false, mode = Plot.Mode.POINT))
        for (i in 0 until 1000) {
            plot.addDatum("Scatter2", Vector2(cos(Rand.randx.nextGaussian()).toFloat(), 0f).rotateRad(sin(Rand.randx.nextGaussian()).toFloat()))
        }

        plot.addTrend(Plot.Trend("Spiral", Color.GREEN, false, mode = Plot.Mode.LINE))
        for (i in 0 until 1000) {
            val t = (i * 2 * Math.PI / 500).toFloat()
            val x = cos(t * 3) / 2f.pow(t)
            val y = sin(t * 3) / 2f.pow(t)
            plot.addDatum("Spiral", Vector2(x, y))
        }

        window.addChildWindow(plot)
    }

    private fun makeChildren(branches: Int, depth: Int, parent: Window) {
        if (depth > 0) {
            for (b in 0 until branches) {
                val size = (parent.size) * Rand.randx.nextFloat()
                val pos = (parent.size - size) * Vector2(Rand.randx.nextFloat(), Rand.randx.nextFloat())
                val newWin = Window(pos, size, size/2)
                parent.addChildWindow(newWin)
                makeChildren(branches, depth - 1, newWin)
            }
        }

    }

    override fun show() {
        viewport.update(Gdx.graphics.width, Gdx.graphics.height)
    }

    override fun render(delta: Float) {
        ScreenUtils.clear(.25f, .25f, .25f, 1f)

        val stateChanged = pMouseDown != Gdx.input.isTouched
        pMouseDown = Gdx.input.isTouched

        val mouseWorldPos = viewport.unproject(Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
        window.handleMouseInput(stateChanged, Gdx.input.isTouched, Vector2(mouseWorldPos.x, mouseWorldPos.y))
        window.update(delta)

        viewport.apply()
        MultiBrain.batch.use(camera) {
            window.render(camera, viewport)
        }
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }
}