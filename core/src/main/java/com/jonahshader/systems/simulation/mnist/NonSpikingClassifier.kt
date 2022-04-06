package com.jonahshader.systems.simulation.mnist

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.viewport.ScalingViewport
import com.jonahshader.MultiBrain
import com.jonahshader.systems.creatureparts.TaskBuilder
import com.jonahshader.systems.creatureparts.ReinforcementTask
import com.jonahshader.systems.neuralnet.Network
import com.jonahshader.systems.simulation.mnist.MnistData.IMAGE_WIDTH_HEIGHT
import com.jonahshader.systems.ui.TextRenderer
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.tanh

class NonSpikingClassifier(override var maxIterations: Int, networkBuilder: (Int, Int) -> Network) : ReinforcementTask {
    override var currentIteration = 0
    override val network: Network
    private val color = Color()

    private var fitness = 999f

    companion object {
        private const val PIXEL_SIZE = 8f
        fun makeBuilder(maxIterations: Int) : TaskBuilder = { NonSpikingClassifier(maxIterations, it) }
        val defaultBuilder: TaskBuilder = { NonSpikingClassifier(1, it) }
    }

    private lateinit var datum: Pair<IntArray, Int>

    init {
        MnistData.load() // ensure mnist data is loaded
        network = networkBuilder(IMAGE_WIDTH_HEIGHT * IMAGE_WIDTH_HEIGHT, 10)
        setup()
    }

    private fun setup() {
        datum = MnistData.getRandomTrainingData()
    }

    override fun cloneAndReset(): ReinforcementTask {
        val newClassifier = NonSpikingClassifier(maxIterations) { _, _ -> network.clone() }
        newClassifier.network.reset()
        return newClassifier
    }

    override fun restartAndRandomize() {
        setup()
        currentIteration = 0
        network.reset()
    }

    override fun render(viewport: ScalingViewport) {
        for (y in 0 until IMAGE_WIDTH_HEIGHT) {
            for (x in 0 until IMAGE_WIDTH_HEIGHT) {
                val brightness = datum.first[x + y * IMAGE_WIDTH_HEIGHT] / 255f
                color.set(brightness, brightness, brightness, 1f)
                MultiBrain.shapeDrawer.filledRectangle(x * PIXEL_SIZE, -y * PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE, color)
            }
        }

        TextRenderer.color.set(1f, 1f, 1f, 1f)
        TextRenderer.begin(MultiBrain.batch, viewport, TextRenderer.Font.LIGHT, PIXEL_SIZE * 3, 0f)
        var max = -1f
        var maxIndex = -1
        for (i in 0 until 10) {
            val prediction = tanh(network.getOutput(i)) * .5f + .5f
            if (prediction > max) {
                max = prediction
                maxIndex = i
            }
            TextRenderer.drawText(PIXEL_SIZE * (IMAGE_WIDTH_HEIGHT + 1), i * PIXEL_SIZE * 3, "$i prediction: $prediction")
        }
        TextRenderer.drawText(PIXEL_SIZE * (IMAGE_WIDTH_HEIGHT + 1), -PIXEL_SIZE * 3, "Target: ${datum.second}")
        if (done()) {
            if (maxIndex == datum.second) {
                TextRenderer.color.set(0f, 1f, 0f, 1f)
            } else {
                TextRenderer.color.set(1f, 0f, 0f, 1f)
            }
            TextRenderer.drawText(PIXEL_SIZE * (IMAGE_WIDTH_HEIGHT + 1), -PIXEL_SIZE * 6, "Prediction: $maxIndex")
        }

        TextRenderer.end()
    }

    override fun update(dt: Float) {
        for (i in 0 until IMAGE_WIDTH_HEIGHT * IMAGE_WIDTH_HEIGHT)
            network.setInput(i, datum.first[i]/255f)

        network.update(dt) // dt should be unused


//        var max = -1f
//        var maxIndex = -1
//        for (i in 0 until 10) {
//            val prediction = tanh(network.getOutput(i)) * .5f + .5f
//            if (prediction > max) {
//                max = prediction
//                maxIndex = i
//            }
//        }

        fitness = 1-crossEntropyLoss((0 until 10).map { if (it == datum.second) 1f else 0f }, (0 until 10).map {network.getOutput(it)})

//        fitness = if (maxIndex != datum.second) {
//            0f
//        } else {
//            1-(1-(tanh(network.getOutput(datum.second)) * .5f + .5f)).pow(2) // 1 - squared error (since we are maximizing fitness)
//        }

        currentIteration++
    }

    override fun getFitness(): Float {
        return fitness
    }

    override fun spectate(cam: Camera) {
    }

    private fun softmax(values: List<Float>) : List<Float> {
        val exp = values.map { Math.E.toFloat().pow(it) }
        val sumExp = exp.sum()
        return exp.map { it / sumExp }
    }

    private fun crossEntropyLoss(expected: List<Float>, actualNetOutput: List<Float>) : Float {
        return expected.zip(softmax(actualNetOutput)).map {
            it.first * ln(it.second)
        }.sum() / -expected.size
    }
}