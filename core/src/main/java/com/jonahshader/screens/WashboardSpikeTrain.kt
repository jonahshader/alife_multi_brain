package com.jonahshader.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.jonahshader.MultiBrain
import com.jonahshader.systems.math.Metric.MICRO
import com.jonahshader.systems.neuralnet.neurons.WashboardNeuron
import com.jonahshader.systems.ui.Plot
import com.jonahshader.systems.ui.ScreenWindow
import com.jonahshader.systems.ui.Slider
import ktx.app.KtxScreen
import ktx.app.clearScreen
import kotlin.math.PI

class WashboardSpikeTrain : KtxScreen {
    private val window = ScreenWindow(Vector2(1280f, 720f))
    private val voltagePlot = Plot("Time (unit)", "Output Voltage", "Washboard Voltage", Vector2(), Vector2(1280f, PLOT_HEIGHT))
    private val phasePlot = Plot("Time (unit)", "Phase Degrees", "Washboard Phase", Vector2(0f, PLOT_HEIGHT), Vector2(1280f, PLOT_HEIGHT))
    private val currentPlot = Plot("Time (unit)", "Current", "Washboard Current", Vector2(0f, PLOT_HEIGHT * 2), Vector2(1280f, PLOT_HEIGHT))
    private val neurons = mutableListOf<WashboardNeuron>()

//    private var bias = 198 * 10e-6f
    private var bias = 198e-6f
    private var a = 0.01f


    companion object {
        private const val SIM_STEPS = 400
        private const val NEURON_COUNT = 8
        private const val PLOT_HEIGHT = 200f
    }

    init {
        window += voltagePlot
        window += phasePlot
        window += currentPlot

        currentPlot.addTrend(Plot.Trend("Input Current", Color.BLUE, true, Plot.Mode.LINE))


        (0 until NEURON_COUNT).forEachIndexed{ index, _ ->
            voltagePlot.addTrend(Plot.Trend("Voltage $index", Color(1f, 1f, 1f, 1f).fromHsv(index.toFloat() * 360f / NEURON_COUNT, 1f, 1f), true, Plot.Mode.LINE))
            phasePlot.addTrend(Plot.Trend("Phase $index", Color(1f, 1f, 1f, 1f).fromHsv(index.toFloat() * 360f / NEURON_COUNT, 1f, 1f), true, Plot.Mode.LINE))
        }

        window += Slider("Bias", 0f, 900e-6f, bias, Vector2(0f, 680f), size = Vector2(1280f, 40f)) { bias = it; generate() }
        window += Slider("Dampening (a)", 0.0f, 0.1f, a, Vector2(0f, 640f), size = Vector2(1280f, 40f)) { a = it; generate() }



        generate()
    }

    private fun generate() {
        neurons.clear()
        voltagePlot.clearData()
        phasePlot.clearData()
        currentPlot.clearData()

        for (i in 0 until NEURON_COUNT) {
            neurons += WashboardNeuron(a)
//            neurons.last().bias = (198 * 10e-6).toFloat()
            neurons.last().bias = bias
//            neurons.last().bias = 0f
//            neurons.last().angleD = Math.PI / 4
            neurons.last().angleD = .55
//            neurons.last().angleD = 0.4453125
        }

        for (i in 0 until SIM_STEPS) {
            val p = (i - 50).toFloat() / (50)
            val inputCurrent = ((p * (1-p)) * 4).coerceAtLeast(0f) * 30 * MICRO.toFloat() * 100
//            val inputCurrent = 0f
//            var inputCurrent = 0.0065f// 0.0065f
//            if (i > 12) inputCurrent = 0f

            currentPlot.addDatum("Input Current", Vector2(i.toFloat(), inputCurrent))
            neurons[0].addWeightedOutput(inputCurrent)
            neurons[0].update(1/1000f)
            neurons[0].updateOutput()
            for (j in 1 until neurons.size) {
                neurons[j].addWeightedOutput(neurons[j-1].out * 64f) // .25f
                neurons[j].update(1/1000f)
                neurons[j].updateOutput()
            }

            neurons.forEachIndexed { index, it ->
                voltagePlot.addDatum("Voltage $index", Vector2(i.toFloat(), it.out))
                phasePlot.addDatum("Phase $index", Vector2(i.toFloat(), (it.angleD * 360 / (2 * PI)).toFloat()))
            }
        }
    }

    override fun render(delta: Float) {
        clearScreen(0f, 0f, 0f)

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) { generate() }

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