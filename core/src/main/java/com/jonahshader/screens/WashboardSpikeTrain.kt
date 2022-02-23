package com.jonahshader.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.jonahshader.MultiBrain
import com.jonahshader.systems.neuralnet.neurons.WashboardNeuron
import com.jonahshader.systems.ui.Plot
import com.jonahshader.systems.ui.ScreenWindow
import com.jonahshader.systems.ui.Slider
import ktx.app.KtxScreen
import ktx.app.clearScreen

class WashboardSpikeTrain : KtxScreen {
    private val window = ScreenWindow(Vector2(1280f, 720f))
    private val plot = Plot("Time (unit)", "Output Voltage", "Washboard Voltage", Vector2(), Vector2(1280f, 640f))
    private val neurons = mutableListOf<WashboardNeuron>()

//    private var bias = 198 * 10e-6f
    private var bias = 198e-6f
    private var a = 0.01f


    companion object {
        private const val SIM_STEPS = 2000
        private const val NEURON_COUNT = 8
    }

    init {
        window.addChildWindow(plot)
        plot.addTrend(Plot.Trend("Input Current", Color.BLUE, true, Plot.Mode.LINE))
        (0 until NEURON_COUNT).forEachIndexed{ index, _ ->
            plot.addTrend(Plot.Trend("Voltage $index", Color(1f, 1f, 1f, 1f).fromHsv(index.toFloat() * 360f / NEURON_COUNT, 1f, 1f), true, Plot.Mode.LINE))
        }

        val biasSlider = Slider(10 * 10e-6f, 900 * 10e-6f, bias, Vector2(0f, 640f)) { bias = it }
        window.addChildWindow(biasSlider)


        generate()
    }

    private fun generate() {
        neurons.clear()
        plot.clearData()
        for (i in 0 until 15) {
            neurons += WashboardNeuron(a)
//            neurons.last().bias = (198 * 10e-6).toFloat()
            neurons.last().bias = bias
//            neurons.last().bias = 0f
//            neurons.last().angleD = Math.PI / 4
            neurons.last().angleD = 0.4453125
            neurons.last().angle = 0.4453125f
        }

        for (i in 0 until SIM_STEPS) {
            val p = i.toFloat() / (170)
            val inputCurrent = (p * (1-p)).coerceAtLeast(0f) * .08f
//            val inputCurrent = 0f
//            var inputCurrent = 0.0065f// 0.0065f
//            if (i > 12) inputCurrent = 0f

            plot.addDatum("Input Current", Vector2(i.toFloat(), inputCurrent))
            neurons[0].addWeightedOutput(inputCurrent)
            neurons[0].update(1/1000f)
            neurons[0].updateOutput()
            for (j in 1 until neurons.size) {
                neurons[j].addWeightedOutput(neurons[j-1].out * 4f) // .25f
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