package com.jonahshader.screens

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.jonahshader.MultiBrain
import com.jonahshader.systems.neuralnet.neurons.WashboardNeuron
import com.jonahshader.systems.ui.Plot
import com.jonahshader.systems.ui.ScreenWindow
import ktx.app.KtxScreen
import ktx.app.clearScreen

class WashboardSpikeTrain : KtxScreen {
    private val window = ScreenWindow(Vector2(1280f, 720f))

    companion object {
        private const val SIM_STEPS = 1000
    }

    init {
        val plot = Plot("Time (unit)", "Output Voltage", "Washboard Voltage", Vector2(), Vector2(300f, 200f))
        val trend = Plot.Trend("asdf", Color.WHITE, true, Plot.Mode.LINE)
        window.addChildWindow(plot)
        plot.addTrend(trend)
        // configure window contents
        val washboardNeuron = WashboardNeuron()

        for (i in 0 until SIM_STEPS) {
            val inputCurrent = (i / SIM_STEPS.toFloat()) * 0.01f
            washboardNeuron.addWeightedOutput(inputCurrent)
            washboardNeuron.update(0f)
            washboardNeuron.updateOutput()
            plot.addDatum("asdf", Vector2(i.toFloat(), washboardNeuron.out))
        }
    }

    override fun render(delta: Float) {
        clearScreen(0f, 0f, 0f)
        window.update(delta)
        window.render(MultiBrain.batch)
    }

    override fun show() {
        window.show()
    }

    override fun resize(width: Int, height: Int) {
        window.resize(width, height)
    }
}