package com.jonahshader.systems.neuralnet.densecyclic

import com.jonahshader.systems.neuralnet.Network
import com.jonahshader.systems.neuralnet.NetworkBuilder
import com.jonahshader.systems.utils.Rand
import org.jetbrains.kotlinx.multik.api.*
import org.jetbrains.kotlinx.multik.api.linalg.dot
import org.jetbrains.kotlinx.multik.ndarray.data.*
import org.jetbrains.kotlinx.multik.ndarray.operations.*
import java.util.*
import kotlin.math.tanh

class DenseCyclicNetwork : Network {
    companion object {
        fun makeBuilder(hiddenSize: Int) : NetworkBuilder = { input, output -> DenseCyclicNetwork(input, hiddenSize, output) }
    }
    private val rand: Random

    private val inputVector: NDArray<Float, D1>
    private var outputVector: NDArray<Float, D1>
    private var hiddenBuffer: NDArray<Float, D1>
    private var hiddenOut: NDArray<Float, D1>
    private val hiddenBias: NDArray<Float, D1>
    private val outputBias: NDArray<Float, D1>

    private val inputToHiddenWeights: NDArray<Float, D2>
    private val hiddenToHiddenWeights: NDArray<Float, D2>
    private val inputToOutputWeights: NDArray<Float, D2>
    private val hiddenToOutputWeights: NDArray<Float, D2>

    constructor(inputSize: Int, hiddenSize: Int, outputSize: Int, rand: Random = Rand.randx) {
        this.rand = rand
        this.inputVector = mk.zeros(inputSize)
        this.outputVector = mk.zeros(outputSize)
        this.hiddenBuffer = mk.zeros(hiddenSize)
        this.hiddenOut = mk.zeros(hiddenSize)
        this.hiddenBias = mk.d1array(hiddenSize) { rand.nextGaussian().toFloat() }
        this.outputBias = mk.d1array(outputSize) { rand.nextGaussian().toFloat() }
        this.inputToHiddenWeights = mk.d2arrayIndices(hiddenSize, inputSize) { _, _ -> rand.nextGaussian().toFloat() }
        this.hiddenToHiddenWeights = mk.d2arrayIndices(hiddenSize, hiddenSize) { _, _ -> rand.nextGaussian().toFloat() }
        this.inputToOutputWeights = mk.d2arrayIndices(outputSize, inputSize) { _, _ -> rand.nextGaussian().toFloat() }
        this.hiddenToOutputWeights = mk.d2arrayIndices(outputSize, hiddenSize) { _, _ -> rand.nextGaussian().toFloat() }
    }

    constructor(toCopy: DenseCyclicNetwork) {
        this.rand = toCopy.rand
        this.inputVector = toCopy.inputVector.deepCopy()
        this.outputVector = toCopy.outputVector.deepCopy()
        this.hiddenBuffer = toCopy.hiddenBuffer.deepCopy()
        this.hiddenOut = toCopy.hiddenOut.deepCopy()
        this.hiddenBias = toCopy.hiddenBias.deepCopy()
        this.outputBias = toCopy.outputBias.deepCopy()
        this.inputToHiddenWeights = toCopy.inputToHiddenWeights.deepCopy()
        this.hiddenToHiddenWeights = toCopy.hiddenToHiddenWeights.deepCopy()
        this.inputToOutputWeights = toCopy.inputToOutputWeights.deepCopy()
        this.hiddenToOutputWeights = toCopy.hiddenToOutputWeights.deepCopy()
    }

    override val multithreadable = true

    override fun setInput(index: Int, value: Float) {
        inputVector[index] = value
    }

    override fun getOutput(index: Int): Float {
        return outputVector[index]
    }

    override fun getInputSize(): Int {
        return inputVector.size
    }

    override fun getOutputSize(): Int {
        return outputVector.size
    }

    override fun mutateParameters(amount: Float) {
        // mutate bias and weights
        for (i in hiddenBias.indices)
            hiddenBias.data[i] += rand.nextGaussian().toFloat() * amount
        for (i in outputBias.indices)
            outputBias.data[i] += rand.nextGaussian().toFloat() * amount
        for (i in inputToHiddenWeights.indices)
            inputToHiddenWeights.data[i] += rand.nextGaussian().toFloat() * amount
        for (i in hiddenToHiddenWeights.indices)
            hiddenToHiddenWeights.data[i] += rand.nextGaussian().toFloat() * amount
        for (i in inputToOutputWeights.indices)
            inputToOutputWeights.data[i] += rand.nextGaussian().toFloat() * amount
        for (i in hiddenToOutputWeights.indices)
            hiddenToOutputWeights.data[i] += rand.nextGaussian().toFloat() * amount
    }

    override fun getParameters(): List<Float> = hiddenBias.toList() + outputBias.toList() +
            inputToHiddenWeights.toList() + hiddenToHiddenWeights.toList() +
            inputToOutputWeights.toList() + hiddenToOutputWeights.toList()

    override fun setParameters(params: List<Float>) {
        var index = 0
        for (i in hiddenBias.indices)
            hiddenBias.data[i] = params[index++]
        for (i in outputBias.indices)
            outputBias.data[i] = params[index++]
        for (i in inputToHiddenWeights.indices)
            inputToHiddenWeights.data[i] = params[index++]
        for (i in hiddenToHiddenWeights.indices)
            hiddenToHiddenWeights.data[i] = params[index++]
        for (i in inputToOutputWeights.indices)
            inputToOutputWeights.data[i] = params[index++]
        for (i in hiddenToOutputWeights.indices)
            hiddenToOutputWeights.data[i] = params[index++]
    }

    override fun update(dt: Float) {
        hiddenBuffer = (hiddenToHiddenWeights dot hiddenOut) + (inputToHiddenWeights dot inputVector) + hiddenBias
        outputVector = (hiddenToOutputWeights dot hiddenOut) + (inputToOutputWeights dot inputVector) + outputBias

        hiddenOut = hiddenBuffer.map { tanh(it) }
//        hiddenOut = hiddenBuffer.map { (if (it < 0) it/32 else it).coerceIn(-1f, 2f) }
    }

    override fun clone() = DenseCyclicNetwork(this)
    override fun reset() {
        for (i in outputVector.indices)
            outputVector[i] = 0f
        for (i in hiddenBuffer.indices)
            hiddenBuffer[i] = 0f
        for (i in hiddenOut.indices)
            hiddenOut[i] = 0f
    }

    override fun dispose() {
        // empty
    }
}