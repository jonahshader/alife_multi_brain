package com.jonahshader.systems.neuralnet.densecyclic

import com.jonahshader.systems.math.minus
import com.jonahshader.systems.math.plus
import com.jonahshader.systems.math.plusAssign
import com.jonahshader.systems.math.times
import com.jonahshader.systems.neuralnet.Network
import com.jonahshader.systems.neuralnet.NetworkBuilder
import com.jonahshader.systems.utils.Rand
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.indexing.NDArrayIndex.interval
import java.util.*
import kotlin.math.sqrt
import kotlin.math.tanh

class DenseCyclicNetwork : Network {
    companion object {
        fun makeBuilder(hiddenSize: Int): NetworkBuilder =
            { input, output -> DenseCyclicNetwork(input, hiddenSize, output) }
    }

    private val rand: Random

    private val inputVector: INDArray
    private var outputVector: INDArray
    private var hiddenBuffer: INDArray
    private var hiddenOut: INDArray
    private var hiddenBias: INDArray
    private var outputBias: INDArray

    private var inputToHiddenWeights: INDArray
    private var hiddenToHiddenWeights: INDArray
    private var inputToOutputWeights: INDArray
    private var hiddenToOutputWeights: INDArray

    constructor(inputSize: Int, hiddenSize: Int, outputSize: Int, rand: Random = Rand.randx) {

        println("making deep cyclic network with $inputSize, $hiddenSize, $outputSize")
        val sqrt6 = sqrt(6f)
        val inputToHiddenRange = sqrt6 / sqrt(1f + inputSize + hiddenSize)
        val inputToOutputRange = sqrt6 / sqrt(1f + inputSize + outputSize)
        val hiddenToHiddenRange = sqrt6 / sqrt(1f + hiddenSize * 2)
        val hiddenToOutputRange = sqrt6 / sqrt(1f + hiddenSize + outputSize)


        this.rand = rand
        this.inputVector = Nd4j.zeros(inputSize, 1)
        this.outputVector = Nd4j.zeros(outputSize, 1)
        this.hiddenBuffer = Nd4j.zeros(hiddenSize, 1)
        this.hiddenOut = Nd4j.zeros(hiddenSize, 1)

//        this.hiddenBias = Nd4j.d1array(hiddenSize) { rand.nextGaussian().toFloat() * hiddenToHiddenRange }
//        this.outputBias = Nd4j.d1array(outputSize) { rand.nextGaussian().toFloat() * hiddenToOutputRange }
        this.hiddenBias = Nd4j.randn(hiddenSize.toLong(), 1) * hiddenToHiddenRange
        this.outputBias = Nd4j.randn(outputSize.toLong(), 1) * hiddenToOutputRange

        this.inputToHiddenWeights = Nd4j.randn(hiddenSize.toLong(), inputSize.toLong()) * inputToHiddenRange
        this.hiddenToHiddenWeights = Nd4j.randn(hiddenSize.toLong(), hiddenSize.toLong()) * hiddenToHiddenRange
        this.inputToOutputWeights = Nd4j.randn(outputSize.toLong(), inputSize.toLong()) * inputToOutputRange
        this.hiddenToOutputWeights = Nd4j.randn(outputSize.toLong(), hiddenSize.toLong()) * hiddenToOutputRange

//        println("hiddenBias: ${hiddenBias.shape().toList()}")
        println("hiddenBuffer: ${hiddenBuffer.shape().toList()}")
    }

    constructor(toCopy: DenseCyclicNetwork) {
        this.rand = toCopy.rand
        this.inputVector = toCopy.inputVector.dup()
        this.outputVector = toCopy.outputVector.dup()
        this.hiddenBuffer = toCopy.hiddenBuffer.dup()
        this.hiddenOut = toCopy.hiddenOut.dup()
        this.hiddenBias = toCopy.hiddenBias.dup()
        this.outputBias = toCopy.outputBias.dup()
        this.inputToHiddenWeights = toCopy.inputToHiddenWeights.dup()
        this.hiddenToHiddenWeights = toCopy.hiddenToHiddenWeights.dup()
        this.inputToOutputWeights = toCopy.inputToOutputWeights.dup()
        this.hiddenToOutputWeights = toCopy.hiddenToOutputWeights.dup()
    }

    override val multithreadable = false // TODO

    override fun setInput(index: Int, value: Float) {
        inputVector.putScalar(index.toLong(), value)
    }

    override fun getOutput(index: Int): Float {
        return outputVector.getFloat(index.toLong())
    }

    override fun getInputSize(): Int {
        return inputVector.size(0).toInt()
    }

    override fun getOutputSize(): Int {
        return outputVector.size(0).toInt()
    }

    override fun mutateParameters(amount: Float) {
        // mutate bias and weights
        hiddenBias.plusAssign(Nd4j.randn(hiddenBias.shape()[0], hiddenBias.shape()[1]) * amount)
        println("hiddenBias mParams: ${hiddenBias.shape().toList()}")
        outputBias.plusAssign(Nd4j.randn(outputBias.shape()[0], outputBias.shape()[1]) * amount)
        inputToHiddenWeights.plusAssign(Nd4j.randn(inputToHiddenWeights.shape()[0], inputToHiddenWeights.shape()[1]) * amount)
        hiddenToHiddenWeights.plusAssign(Nd4j.randn(hiddenToHiddenWeights.shape()[0], hiddenToHiddenWeights.shape()[1]) * amount)
        inputToOutputWeights.plusAssign(Nd4j.randn(inputToOutputWeights.shape()[0], inputToOutputWeights.shape()[1]) * amount)
        hiddenToOutputWeights.plusAssign(Nd4j.randn(hiddenToOutputWeights.shape()[0], hiddenToOutputWeights.shape()[1]) * amount)
    }

    override fun getParameters(): INDArray = Nd4j.toFlattened(
        hiddenBias, outputBias,
        inputToHiddenWeights, hiddenToHiddenWeights, inputToOutputWeights, hiddenToOutputWeights
    )

    //    hiddenBias.toList() + outputBias.toList() +
//    inputToHiddenWeights.toList() + hiddenToHiddenWeights.toList() +
//    inputToOutputWeights.toList() + hiddenToOutputWeights.toList()
    override fun setParameters(params: INDArray) {
        var index = 0
        hiddenBias = params.get(interval(index, index + hiddenBias.size(0).toInt())).dup()
//        println("hiddenBuffer setParams: ${hiddenBuffer.shape().toList()}")
        index += hiddenBias.size(0).toInt()
        outputBias = params.get(interval(index, index + outputBias.size(0).toInt())).dup()
        index += outputBias.size(0).toInt()
        inputToHiddenWeights = params.get(interval(index, d2Size(inputToHiddenWeights)))
            .reshape(d2Shape(hiddenToHiddenWeights)).dup()
        index += d2Size(inputToHiddenWeights)
        hiddenToHiddenWeights = params.get(interval(index, index + d2Size(hiddenToHiddenWeights)))
            .reshape(d2Shape(hiddenToHiddenWeights))
        index += d2Size(hiddenToHiddenWeights)

        inputToOutputWeights = params.get(interval(index, index + d2Size(inputToOutputWeights)))
            .reshape(d2Shape(inputToOutputWeights))
        index += d2Size(inputToOutputWeights)

        hiddenToOutputWeights = params.get(interval(index, index + d2Size(hiddenToOutputWeights)))
            .reshape(d2Shape(hiddenToOutputWeights))
        index += d2Size(hiddenToOutputWeights)

//        var index = 0
//        for (i in 0 until hiddenBias.rows()) {
//            hiddenBias.putScalar(index.toLong(), params.getFloat(index.toLong()))
//            index++
//        }
//        for (i in 0 until outputBias.rows())
//            outputBias.data[i] = params[index++]
//        for (i in inputToHiddenWeights.indices)
//            inputToHiddenWeights.data[i] = params[index++]
//        for (i in hiddenToHiddenWeights.indices)
//            hiddenToHiddenWeights.data[i] = params[index++]
//        for (i in inputToOutputWeights.indices)
//            inputToOutputWeights.data[i] = params[index++]
//        for (i in hiddenToOutputWeights.indices)
//            hiddenToOutputWeights.data[i] = params[index++]
    }

    private fun d2Size(indarr: INDArray): Int = (indarr.shape()[0] * indarr.shape()[1]).toInt()
    private fun d2Shape(indarr: INDArray): IntArray = intArrayOf(indarr.shape()[0].toInt(), indarr.shape()[1].toInt())

    //    private fun sigmoid(x: NDArray<Float, D1>) : NDArray<Float, D1> = x.exp() / (1f + x.exp())
    private fun sigmoidFaster(x: INDArray): INDArray {
        val ex = Nd4j.math.exp(x)
        return ex / (1f + ex)
    }

    override fun update(dt: Float) {
//        println("hiddenBuffer update: ${hiddenBuffer.shape().toList()}")
//        println(hiddenBuffer.shape().map { "$it " })
//        println(hiddenToHiddenWeights.shape().map { "$it " })
//        println(hiddenOut.shape().map { "$it " })
//        println(inputToHiddenWeights.shape().map { "$it " })
//        println(inputVector.shape().map { "$it " })
        hiddenBuffer = (hiddenToHiddenWeights.mmul(hiddenOut)) + (inputToHiddenWeights.mmul(inputVector)) + hiddenBias
        outputVector = (hiddenToOutputWeights.mmul(hiddenOut)) + (inputToOutputWeights.mmul(inputVector)) + outputBias

//        hiddenOut = hiddenBuffer.map { tanh(it) }

        hiddenOut = 2f * sigmoidFaster(hiddenBuffer * 2f) - 1f


//        hiddenOut = hiddenBuffer.map { (if (it < 0) it/32 else it).coerceIn(-1f, 2f) }
    }

    override fun clone() = DenseCyclicNetwork(this)
    override fun reset() {
//        for (i in outputVector.indices)
//            outputVector[i] = 0f
//        for (i in hiddenBuffer.indices)
//            hiddenBuffer[i] = 0f
//        for (i in hiddenOut.indices)
//            hiddenOut[i] = 0f

//        outputVector = mk.zeros(outputVector.size)
//        hiddenBuffer = mk.zeros(hiddenBuffer.size)
//        hiddenOut = mk.zeros(hiddenOut.size)

        outputVector = Nd4j.zeros(outputVector.size(0), 1)
        hiddenBuffer = Nd4j.zeros(hiddenBuffer.size(0), 1)
//        println("hidden buffer rows: ${hiddenBuffer.rows()}")
//        println("hidden buffer cols: ${hiddenBuffer.columns()}")
//        println("hidden buffer size(0): ${hiddenBuffer.size(0)}")
        hiddenOut = Nd4j.zeros(hiddenOut.size(0), 1)
    }

    override fun dispose() {
        // empty
    }
}