package com.jonahshader.systems.neuralnet.cudacyclic

import com.jonahshader.systems.math.CudaMatrix
import com.jonahshader.systems.math.CudaVector
import com.jonahshader.systems.neuralnet.Network
import com.jonahshader.systems.neuralnet.NetworkBuilder
import com.jonahshader.systems.neuralnet.densecyclic.DenseCyclicNetwork
import com.jonahshader.systems.utils.Rand
import org.jetbrains.kotlinx.multik.ndarray.data.set
import org.jetbrains.kotlinx.multik.ndarray.operations.toList
import java.util.*

class CudaCyclicNetwork : Network {
    companion object {
        fun makeBuilder(hiddenSize: Int) : NetworkBuilder = { input, output -> CudaCyclicNetwork(input, hiddenSize, output) }
    }
    private val rand: Random

    private val inputVector: CudaVector
    private var outputVector: CudaVector
    private var hiddenBuffer: CudaVector
    private var hiddenOut: CudaVector
    private val hiddenBias: CudaVector
    private val outputBias: CudaVector

    private val inputToHiddenWeights: CudaMatrix
    private val hiddenToHiddenWeights: CudaMatrix
    private val inputToOutputWeights: CudaMatrix
    private val hiddenToOutputWeights: CudaMatrix

    private var inputDirty = false
    private var outputDirty = false
    private var paramsDirty = false

    override val multithreadable = false


    constructor(inputSize: Int, hiddenSize: Int, outputSize: Int, rand: Random = Rand.randx) {
        this.rand = rand
        inputVector = CudaVector(inputSize)
        outputVector = CudaVector(outputSize)
        hiddenBuffer = CudaVector(hiddenSize)
        hiddenOut = CudaVector(hiddenSize)
        hiddenBias = CudaVector(hiddenSize) { rand.nextGaussian().toFloat() }
        outputBias = CudaVector(outputSize) { rand.nextGaussian().toFloat() }

        inputToHiddenWeights = CudaMatrix(hiddenSize, inputSize) { rand.nextGaussian().toFloat() }
        hiddenToHiddenWeights = CudaMatrix(hiddenSize, hiddenSize) { rand.nextGaussian().toFloat() }
        inputToOutputWeights = CudaMatrix(outputSize, inputSize) { rand.nextGaussian().toFloat() }
        hiddenToOutputWeights = CudaMatrix(outputSize, hiddenSize) { rand.nextGaussian().toFloat() }
        uploadAll()
    }

    constructor(toCopy: CudaCyclicNetwork) {
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
        uploadAll()
    }

    override fun setInput(index: Int, value: Float) {
        inputDirty = true
        inputVector[index] = value
    }

    override fun getOutput(index: Int): Float {
        if (outputDirty) {
            outputVector.download()
            outputDirty = false
        }
        return outputVector[index]
    }

    override fun getInputSize(): Int {
        return inputVector.size
    }

    override fun getOutputSize(): Int {
        return outputVector.size
    }

    override fun mutateParameters(amount: Float) {
        if (paramsDirty) {
            downloadWeightsAndBiases()
            paramsDirty = false
        }
        for (i in hiddenBias.indices)
            hiddenBias[i] += rand.nextGaussian().toFloat() * amount
        for (i in outputBias.indices)
            outputBias[i] += rand.nextGaussian().toFloat() * amount
        for (i in inputToHiddenWeights.indices)
            inputToHiddenWeights[i] += rand.nextGaussian().toFloat() * amount
        for (i in hiddenToHiddenWeights.indices)
            hiddenToHiddenWeights[i] += rand.nextGaussian().toFloat() * amount
        for (i in inputToOutputWeights.indices)
            inputToOutputWeights[i] += rand.nextGaussian().toFloat() * amount
        for (i in hiddenToOutputWeights.indices)
            hiddenToOutputWeights[i] += rand.nextGaussian().toFloat() * amount
        uploadWeightsAndBiases()
    }

    override fun getParameters(): List<Float> {
        if (paramsDirty) {
            downloadWeightsAndBiases()
            paramsDirty = false
        }
        return hiddenBias.toList() + outputBias.toList() +
                inputToHiddenWeights.toList() + hiddenToHiddenWeights.toList() +
                inputToOutputWeights.toList() + hiddenToOutputWeights.toList()
    }

    override fun setParameters(params: List<Float>) {
        var index = 0
        for (i in hiddenBias.indices)
            hiddenBias[i] = params[index++]
        for (i in outputBias.indices)
            outputBias[i] = params[index++]
        for (i in inputToHiddenWeights.indices)
            inputToHiddenWeights[i] = params[index++]
        for (i in hiddenToHiddenWeights.indices)
            hiddenToHiddenWeights[i] = params[index++]
        for (i in inputToOutputWeights.indices)
            inputToOutputWeights[i] = params[index++]
        for (i in hiddenToOutputWeights.indices)
            hiddenToOutputWeights[i] = params[index++]

        uploadWeightsAndBiases()
        paramsDirty = false
    }

    override fun update(dt: Float) {
        if (inputDirty) {
            inputVector.upload()
            inputDirty = false
        }

        hiddenBuffer.copyFrom(hiddenBias)
        hiddenToHiddenWeights.multiply(hiddenOut, hiddenBuffer, 1f)
        inputToHiddenWeights.multiply(inputVector, hiddenBuffer, 1f)

        outputVector.copyFrom(outputBias)
        hiddenToOutputWeights.multiply(hiddenOut, outputVector, 1f)
        inputToOutputWeights.multiply(inputVector, outputVector, 1f)

        // TODO: activate
    }

    override fun clone() = CudaCyclicNetwork(this)

    override fun reset() {
        for (i in outputVector.indices)
            outputVector[i] = 0f
        for (i in hiddenBuffer.indices)
            hiddenBuffer[i] = 0f
        for (i in hiddenOut.indices)
            hiddenOut[i] = 0f

        outputVector.upload()
        hiddenBuffer.upload()
        hiddenOut.upload()
    }

    override fun dispose() {
        inputVector.dispose()
        outputVector.dispose()
        hiddenBuffer.dispose()
        hiddenOut.dispose()
        hiddenBias.dispose()
        outputBias.dispose()

        inputToHiddenWeights.dispose()
        hiddenToHiddenWeights.dispose()
        inputToOutputWeights.dispose()
        hiddenToOutputWeights.dispose()
    }

    private fun uploadAll() {
        inputVector.upload()
        outputVector.upload()
        hiddenBuffer.upload()
        hiddenOut.upload()
        uploadWeightsAndBiases()
    }

    private fun uploadWeightsAndBiases() {
        hiddenBias.upload()
        outputBias.upload()

        inputToHiddenWeights.upload()
        hiddenToHiddenWeights.upload()
        inputToOutputWeights.upload()
        hiddenToOutputWeights.upload()
    }

    private fun downloadWeightsAndBiases() {
        hiddenBias.download()
        outputBias.download()

        inputToHiddenWeights.download()
        hiddenToHiddenWeights.download()
        inputToOutputWeights.download()
        hiddenToOutputWeights.download()
    }
}