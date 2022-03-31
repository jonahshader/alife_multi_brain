package com.jonahshader.systems.simulation.mnist

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.jonahshader.MultiBrain
import com.jonahshader.systems.creatureparts.TaskBuilder
import com.jonahshader.systems.creatureparts.ReinforcementTask
import com.jonahshader.systems.neuralnet.Network
import com.jonahshader.systems.simulation.mnist.MnistData.IMAGE_WIDTH_HEIGHT
import kotlin.math.roundToInt

class SpikingClassifier(private val dt: Float, private val maxTime: Float, networkBuilder: (Int, Int) -> Network) : ReinforcementTask {
    override var currentIteration = 0
    override var maxIterations = (maxTime / dt).roundToInt()
    override val network: Network
    private val color = Color()

    companion object {
        private const val PIXEL_SIZE = 8f
        val defaultBuilder: TaskBuilder = { SpikingClassifier(1f, 1f, it) }
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
        val newClassifier = SpikingClassifier(dt, maxTime) { _, _ -> network.clone() }
        newClassifier.network.reset()
        return newClassifier
    }

    override fun restartAndRandomize() {
        setup()
        currentIteration = 0
        network.reset()
    }

    override fun render() {
        for (y in 0 until IMAGE_WIDTH_HEIGHT) {
            for (x in 0 until IMAGE_WIDTH_HEIGHT) {
                val brightness = datum.first[x + y * IMAGE_WIDTH_HEIGHT] / 255f
                color.set(brightness, brightness, brightness, 1f)
                MultiBrain.shapeDrawer.filledRectangle(x * PIXEL_SIZE, -y * PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE, color)
            }
        }
    }

    override fun update(dt: Float) {
        currentIteration++
    }

    override fun getFitness(): Float {
        return Math.random().toFloat() // TODO
    }

    override fun spectate(cam: Camera) {
    }
}