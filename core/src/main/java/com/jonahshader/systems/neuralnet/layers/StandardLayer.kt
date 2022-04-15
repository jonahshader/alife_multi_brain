package com.jonahshader.systems.neuralnet.layers

import com.jonahshader.systems.utils.Rand
import org.jetbrains.kotlinx.multik.api.d1array
import org.jetbrains.kotlinx.multik.api.d2array
import org.jetbrains.kotlinx.multik.api.linalg.dot
import org.jetbrains.kotlinx.multik.api.math.exp
import org.jetbrains.kotlinx.multik.api.math.sin
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.ndarray.data.*
import org.jetbrains.kotlinx.multik.ndarray.operations.*

class StandardLayer : Layer {
    private val inputSize: Int
    private val outputSize: Int
    private var weights: NDArray<Float, D2>
    private var biases: NDArray<Float, D1>

    constructor(inputSize: Int, outputSize: Int) {
        this.inputSize = inputSize
        this.outputSize = outputSize
        weights = mk.d2array(outputSize, inputSize) { Rand.randx.nextGaussian().toFloat() }
        biases = mk.d1array(outputSize) { Rand.randx.nextGaussian().toFloat() }
    }

    constructor(c: StandardLayer) {
        inputSize = c.inputSize
        outputSize = c.outputSize
        weights = c.weights.deepCopy()
        biases = c.biases.deepCopy()
    }

    override fun update(input: NDArray<Float, D1>, dt: Float): NDArray<Float, D1> {
        val output = ((weights dot input) + biases)
        return (2.0f / (1f + (output * -2.0f).exp())) - 1.0f // tanh activation
    }

    override fun mutateParameters(amount: Float) {
        weights.plusAssign(mk.d2array(outputSize, inputSize) { Rand.randx.nextGaussian().toFloat() * amount })
        biases.plusAssign(mk.d1array(outputSize) { Rand.randx.nextGaussian().toFloat() * amount })
    }

    override fun getParameters(): NDArray<Float, D1> {
        return weights.flatten().append(biases.flatten())
    }

    override fun getParamCount(): Int {
        return (outputSize * inputSize) + outputSize
    }

    override fun setParameters(params: NDArray<Float, D1>) {
        weights = params[0..(outputSize * inputSize)].reshape(outputSize, inputSize).asDNArray().asD2Array()
        biases = params[(outputSize * inputSize)..((outputSize * inputSize) + outputSize)].asDNArray().asD1Array()
    }

    override fun clone() = StandardLayer(this)

    override fun reset() {}
}