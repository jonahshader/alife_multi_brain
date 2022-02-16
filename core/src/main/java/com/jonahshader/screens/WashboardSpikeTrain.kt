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
        private const val SIM_STEPS = 20000
    }

    init {
        val plot = Plot("Time (unit)", "Output Voltage", "Washboard Voltage", Vector2(), Vector2(300f, 200f))
        val trend = Plot.Trend("asdf", Color.WHITE, true, Plot.Mode.LINE)
        val trend2 = Plot.Trend("phase", Color.GREEN, true, Plot.Mode.LINE)
        val inputCurrentTrend = Plot.Trend("Input Current", Color.BLUE, true, Plot.Mode.LINE)
        window.addChildWindow(plot)
        plot.addTrend(trend)
        plot.addTrend(trend2)
        plot.addTrend(inputCurrentTrend)
        // configure window contents
        val washboardNeuron = WashboardNeuron()

        for (i in 0 until SIM_STEPS) {
            var inputCurrent = .002f
            if (i > 333) inputCurrent = 0f
            washboardNeuron.addWeightedOutput(inputCurrent)
            washboardNeuron.update(0f)
            washboardNeuron.updateOutput()
            plot.addDatum("asdf", Vector2(i.toFloat(), washboardNeuron.out))
            plot.addDatum("phase", Vector2(i.toFloat(), washboardNeuron.angleD.toFloat() / (2 * Math.PI.toFloat())))
            plot.addDatum("Input Current", Vector2(i.toFloat(), inputCurrent))
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