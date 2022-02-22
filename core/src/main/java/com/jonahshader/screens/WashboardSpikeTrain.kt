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
    private val neurons = mutableListOf<WashboardNeuron>()

    companion object {
        private const val SIM_STEPS = 300
    }

    init {
        for (i in 0 until 7) {
            neurons += WashboardNeuron()
//            neurons.last().bias = (198 * 10e-6).toFloat()
            neurons.last().bias = (198 * 10e-6).toFloat()
//            neurons.last().bias = 0f
//            neurons.last().angleD = Math.PI / 4
            neurons.last().angleD = 0.4453125
        }

        val plot = Plot("Time (unit)", "Output Voltage", "Washboard Voltage", Vector2(), Vector2(1280f, 720f))
        val inputCurrentTrend = Plot.Trend("Input Current", Color.BLUE, true, Plot.Mode.LINE)
        window.addChildWindow(plot)
        plot.addTrend(inputCurrentTrend)
        neurons.forEachIndexed { index, it ->
            plot.addTrend(Plot.Trend("Voltage $index", Color(1f, 1f, 1f, 1f).fromHsv(index.toFloat() * 360f / neurons.size, 1f, 1f), true, Plot.Mode.LINE))
        }

        for (i in 0 until SIM_STEPS) {
//            val inputCurrent = 0f
            var inputCurrent = 0.0065f // 0.0065f
            if (i > 6) inputCurrent = 0f

            plot.addDatum("Input Current", Vector2(i.toFloat(), inputCurrent))
            neurons[0].addWeightedOutput(inputCurrent)
            neurons[0].update(1/1000f)
            neurons[0].updateOutput()
            for (j in 1 until neurons.size) {
                neurons[j].addWeightedOutput(neurons[j-1].out * .25f) // .25f
                neurons[j].update(1/1000f)
                neurons[j].updateOutput()
            }

            neurons.forEachIndexed { index, it ->
                plot.addDatum("Voltage $index", Vector2(i.toFloat(), it.out))
            }
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