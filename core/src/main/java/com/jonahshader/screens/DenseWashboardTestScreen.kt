package com.jonahshader.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.jonahshader.MultiBrain
import com.jonahshader.systems.neuralnet.layers.LayeredNetwork
import com.jonahshader.systems.neuralnet.layers.StandardLayer
import com.jonahshader.systems.neuralnet.layers.WashboardLayer
import com.jonahshader.systems.ui.Plot
import com.jonahshader.systems.ui.ScreenWindow
import com.jonahshader.systems.ui.Slider
import ktx.app.KtxScreen
import ktx.app.clearScreen
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.get

class DenseWashboardTestScreen : KtxScreen {
    private val window = ScreenWindow(Vector2(1280f, 720f))
    private val voltagePlot = Plot("Time (unit)", "Output Voltage", "Washboard Voltage", Vector2(), Vector2(1280f, PLOT_HEIGHT))
    private val phasePlot = Plot("Time (unit)", "Phase Degrees", "Washboard Phase", Vector2(0f, PLOT_HEIGHT), Vector2(1280f, PLOT_HEIGHT))
    private val currentPlot = Plot("Time (unit)", "Current", "Washboard Current", Vector2(0f, PLOT_HEIGHT * 2), Vector2(1280f, PLOT_HEIGHT))
    private val network: LayeredNetwork

    //    private var bias = 198 * 10e-6f
    private var bias = 0.0024722111f // 198e-6f
    private var a = 0.002f

    private var currentPeak = 0.01f
    private var currentPeakDuration = 25f
    private var weight = 8f
    private var dt = 3.979e-13f

    private val washboardLayers = mutableListOf<WashboardLayer>()


    companion object {
        private const val SIM_STEPS = 200
        private const val OUTPUT_COUNT = 8
        private const val PLOT_HEIGHT = 200f
    }

    init {
        network = LayeredNetwork()
//        network += StandardLayer(1, OUTPUT_COUNT)
        washboardLayers += WashboardLayer(1, OUTPUT_COUNT)
        washboardLayers += WashboardLayer(OUTPUT_COUNT, OUTPUT_COUNT)
        washboardLayers += WashboardLayer(OUTPUT_COUNT, OUTPUT_COUNT)
        washboardLayers += WashboardLayer(OUTPUT_COUNT, OUTPUT_COUNT)
        washboardLayers += WashboardLayer(OUTPUT_COUNT, OUTPUT_COUNT)
        washboardLayers += WashboardLayer(OUTPUT_COUNT, OUTPUT_COUNT)
        washboardLayers += WashboardLayer(OUTPUT_COUNT, OUTPUT_COUNT)
        washboardLayers += WashboardLayer(OUTPUT_COUNT, OUTPUT_COUNT)
        washboardLayers += WashboardLayer(OUTPUT_COUNT, OUTPUT_COUNT)
        washboardLayers.forEach {
            network += it
        }
//        network = DenseWashboardCyclic(1, 50, OUTPUT_COUNT)
        window += voltagePlot
        window += phasePlot
        window += currentPlot

        currentPlot.addTrend(Plot.Trend("Input Current", Color.BLUE, true, Plot.Mode.LINE))


        (0 until OUTPUT_COUNT).forEachIndexed{ index, _ ->
            voltagePlot.addTrend(Plot.Trend("Voltage $index", Color(1f, 1f, 1f, 1f).fromHsv(index.toFloat() * 360f / OUTPUT_COUNT, 1f, 1f), true, Plot.Mode.LINE))
//            phasePlot.addTrend(Plot.Trend("Phase $index", Color(1f, 1f, 1f, 1f).fromHsv(index.toFloat() * 360f / OUTPUT_COUNT, 1f, 1f), true, Plot.Mode.LINE))
        }

        window += Slider("Weight", 0f, weight * 3, weight, Vector2(0f, 680f), size = Vector2(640f, 40f)) { weight = it; generate() }
        window += Slider("Bias", 0f, 250e-5f, bias, Vector2(640f, 680f), size = Vector2(640f, 40f)) { bias = it; generate() }
        window += Slider("Dampening (a)", 0.0f, 0.1f, a, Vector2(0f, 640f), size = Vector2(640f, 40f)) { a = it; generate() }
        window += Slider("dt", dt * .1f, dt * 10f, dt, Vector2(640f, 640f), size = Vector2(640f, 40f)) { dt = it; generate() }
        window += Slider("Current Peak (a)", 0.0f, currentPeak * 3, currentPeak, Vector2(0f, 600f), size = Vector2(640f, 40f)) { currentPeak = it; generate() }
        window += Slider("Current Peak Duration (time unit)", currentPeakDuration/2f, currentPeakDuration*2f, currentPeakDuration, Vector2(640f, 600f), size = Vector2(640f, 40f)) { currentPeakDuration = it; generate() }



        generate()
    }

    private fun generate() {
        network.reset()
//        network.setGlobalBias(bias)
        voltagePlot.clearData()
        phasePlot.clearData()
        currentPlot.clearData()

        washboardLayers.forEach {
            it.weightScale = weight
            it.bias = bias
        }


        for (i in 0 until SIM_STEPS) {
            val scl = (dt * 2.51319419E12f)
            val invscl = 1/scl
            val p = (i - 10 * invscl) / (currentPeakDuration * invscl)
            val inputCurrent = ((p * (1-p)) * 4).coerceAtLeast(0f) * currentPeak
//            val inputCurrent = 0f
//            var inputCurrent = 0.0065f// 0.0065f
//            if (i > 12) inputCurrent = 0f

            currentPlot.addDatum("Input Current", Vector2(i.toFloat(), inputCurrent))
            val output = network.update(mk.ndarray(mk[inputCurrent]), dt)

            for (j in 0 until OUTPUT_COUNT) {
                voltagePlot.addDatum("Voltage $j", Vector2(i.toFloat(), output[j]))
            }
//            neurons.forEachIndexed { index, it ->
//                voltagePlot.addDatum("Voltage $index", Vector2(i.toFloat(), it.out))
//                phasePlot.addDatum("Phase $index", Vector2(i.toFloat(), (it.angleD * 360 / (2 * PI)).toFloat()))
//            }
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