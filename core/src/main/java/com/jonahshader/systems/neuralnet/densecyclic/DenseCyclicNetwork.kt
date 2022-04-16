package com.jonahshader.systems.neuralnet.densecyclic

import com.jonahshader.systems.neuralnet.Network
import com.jonahshader.systems.neuralnet.NetworkBuilder
import com.jonahshader.systems.utils.Rand
import org.jetbrains.kotlinx.multik.api.*
import org.jetbrains.kotlinx.multik.api.linalg.dot
import org.jetbrains.kotlinx.multik.ndarray.data.*
import org.jetbrains.kotlinx.multik.ndarray.operations.*
import java.util.*
import kotlin.math.sqrt
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
    private var hiddenBias: NDArray<Float, D1>
    private var outputBias: NDArray<Float, D1>

    private var inputToHiddenWeights: NDArray<Float, D2>
    private var hiddenToHiddenWeights: NDArray<Float, D2>
    private var inputToOutputWeights: NDArray<Float, D2>
    private var hiddenToOutputWeights: NDArray<Float, D2>

    constructor(inputSize: Int, hiddenSize: Int, outputSize: Int, rand: Random = Rand.randx) {
        val sqrt6 = sqrt(6f)
        val inputToHiddenRange = sqrt6 / sqrt(1f + inputSize + hiddenSize)
        val inputToOutputRange = sqrt6 / sqrt(1f + inputSize + outputSize)
        val hiddenToHiddenRange = sqrt6 / sqrt(1f + hiddenSize * 2)
        val hiddenToOutputRange = sqrt6 / sqrt(1f + hiddenSize + outputSize)


        this.rand = rand
        this.inputVector = mk.zeros(inputSize)
        this.outputVector = mk.zeros(outputSize)
        this.hiddenBuffer = mk.zeros(hiddenSize)
        this.hiddenOut = mk.zeros(hiddenSize)
        this.hiddenBias = mk.d1array(hiddenSize) { rand.nextGaussian().toFloat() * hiddenToHiddenRange }
        this.outputBias = mk.d1array(outputSize) { rand.nextGaussian().toFloat() * hiddenToOutputRange }
        this.inputToHiddenWeights = mk.d2arrayIndices(hiddenSize, inputSize) { _, _ -> rand.nextGaussian().toFloat() * inputToHiddenRange }
        this.hiddenToHiddenWeights = mk.d2arrayIndices(hiddenSize, hiddenSize) { _, _ -> rand.nextGaussian().toFloat() * hiddenToHiddenRange }
        this.inputToOutputWeights = mk.d2arrayIndices(outputSize, inputSize) { _, _ -> rand.nextGaussian().toFloat() * inputToOutputRange }
        this.hiddenToOutputWeights = mk.d2arrayIndices(outputSize, hiddenSize) { _, _ -> rand.nextGaussian().toFloat() * hiddenToOutputRange}
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

//    override fun getParameters(): List<Float> = hiddenBias.toList() + outputBias.toList() +
//            inputToHiddenWeights.toList() + hiddenToHiddenWeights.toList() +
//            inputToOutputWeights.toList() + hiddenToOutputWeights.toList()

    override fun getParameters(): NDArray<Float, D1> =
        hiddenBias.append(outputBias)
            .append(inputToHiddenWeights.flatten()).append(hiddenToHiddenWeights.flatten())
            .append(inputToOutputWeights.flatten()).append(hiddenToOutputWeights.flatten())

    override fun setParameters(params: NDArray<Float, D1>) {
        var index = 0
        hiddenBias = params[0..hiddenBias.size].asDNArray().asD1Array()
        index += hiddenBias.size
        outputBias = params[index..index + outputBias.size].asDNArray().asD1Array()
        index += outputBias.size
        inputToHiddenWeights = params[index..index + inputToHiddenWeights.size].reshape(inputToHiddenWeights.shape[0], inputToHiddenWeights.shape[1]).asDNArray().asD2Array()
        index += inputToHiddenWeights.size
        hiddenToHiddenWeights = params[index..index + hiddenToHiddenWeights.size].reshape(hiddenToHiddenWeights.shape[0], hiddenToHiddenWeights.shape[1]).asDNArray().asD2Array()
        index += hiddenToHiddenWeights.size
        inputToOutputWeights = params[index..index + inputToOutputWeights.size].reshape(inputToOutputWeights.shape[0], inputToOutputWeights.shape[1]).asDNArray().asD2Array()
        index += inputToOutputWeights.size
        hiddenToOutputWeights = params[index..index + hiddenToOutputWeights.size].reshape(hiddenToOutputWeights.shape[0], hiddenToOutputWeights.shape[1]).asDNArray().asD2Array()
        index += hiddenToOutputWeights.size
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