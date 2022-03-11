package com.jonahshader.systems.neuralnet.washboard

import com.jonahshader.systems.math.Metric.FEMTO
import com.jonahshader.systems.neuralnet.Network
import com.jonahshader.systems.neuralnet.NetworkBuilder
import com.jonahshader.systems.neuralnet.neurons.WashboardNeuron.Companion.B
import com.jonahshader.systems.neuralnet.neurons.WashboardNeuron.Companion.lSigma
import com.jonahshader.systems.neuralnet.neurons.WashboardNeuron.Companion.w_e
import com.jonahshader.systems.neuralnet.neurons.WashboardNeuron.Companion.w_ex
import com.jonahshader.systems.utils.Rand
import org.jetbrains.kotlinx.multik.api.d1array
import org.jetbrains.kotlinx.multik.api.d2arrayIndices
import org.jetbrains.kotlinx.multik.api.linalg.dot
import org.jetbrains.kotlinx.multik.api.math.sin
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.zeros
import org.jetbrains.kotlinx.multik.ndarray.data.*
import org.jetbrains.kotlinx.multik.ndarray.operations.*
import java.util.Random
import kotlin.math.sqrt

open class DenseWashboardCyclic : Network {
    /* this network will not have direct connections from input to output because
    this would allow the network to skip the washboard neurons which defeats the purpose*/

    companion object {
        fun makeBuilder(hiddenSize: Int) : NetworkBuilder = { input, output -> DenseWashboardCyclic(input, hiddenSize, output) }
        private const val BIAS_MEAN = 0.002f // 0.0024722111f
        private const val BIAS_SD = 0.0003f
        private const val DAMPENING = 0.01f
        const val INPUT_SCALING = .05f // 0.001
        private const val OUTPUT_SCALING = 1000f
        private const val DT = 3.979e-13f
        private const val ANGLE_INIT = .55f

        private const val WEIGHT_SCALE = 1.46685f // 24.46685f
    }

    private val rand: Random

    protected var inputVector: NDArray<Float, D1>
    protected var outputVector: NDArray<Float, D1>
    private var hiddenBuffer: NDArray<Float, D1>
    private var hiddenOut: NDArray<Float, D1>
    private var hiddenAngle: NDArray<Float, D1>
    private var hiddenAngleVel: NDArray<Float, D1>
    private val hiddenBias: NDArray<Float, D1>
    private val outputBias: NDArray<Float, D1>

    private val inputToHiddenWeights: NDArray<Float, D2>
    private val hiddenToHiddenWeights: NDArray<Float, D2>
    private val hiddenToOutputWeights: NDArray<Float, D2>

    override val multithreadable = true

    constructor(inputSize: Int, hiddenSize: Int, outputSize: Int, rand: Random = Rand.randx) {
        val sqrt6 = sqrt(6f)
        val inputToHiddenRange = sqrt6 / sqrt(1f + inputSize + hiddenSize)
        val hiddenToHiddenRange = sqrt6 / sqrt(1f + hiddenSize * 2)
        val hiddenToOutputRange = sqrt6 / sqrt(1f + hiddenSize + outputSize)


        this.rand = rand
        this.inputVector = mk.zeros(inputSize)
        this.outputVector = mk.zeros(outputSize)
        this.hiddenBuffer = mk.zeros(hiddenSize)
        this.hiddenOut = mk.zeros(hiddenSize)
        this.hiddenAngle = mk.d1array(hiddenSize) { ANGLE_INIT }
        this.hiddenAngleVel = mk.zeros(hiddenSize)
        this.hiddenBias = mk.d1array(hiddenSize) { ((BIAS_MEAN - BIAS_SD + rand.nextGaussian() * hiddenToHiddenRange * BIAS_SD) / WEIGHT_SCALE).toFloat() }
        this.outputBias = mk.d1array(outputSize) { rand.nextGaussian().toFloat() * hiddenToOutputRange / WEIGHT_SCALE }
        this.inputToHiddenWeights = mk.d2arrayIndices(hiddenSize, inputSize) { _, _ -> rand.nextGaussian().toFloat() * inputToHiddenRange }
        this.hiddenToHiddenWeights = mk.d2arrayIndices(hiddenSize, hiddenSize) { _, _ -> rand.nextGaussian().toFloat() * hiddenToHiddenRange }
        this.hiddenToOutputWeights = mk.d2arrayIndices(outputSize, hiddenSize) { _, _ -> rand.nextGaussian().toFloat() * hiddenToOutputRange}
    }

    constructor(toCopy: DenseWashboardCyclic) {
        this.rand = toCopy.rand
        this.inputVector = toCopy.inputVector.deepCopy()
        this.outputVector = toCopy.outputVector.deepCopy()
        this.hiddenBuffer = toCopy.hiddenBuffer.deepCopy()
        this.hiddenOut = toCopy.hiddenOut.deepCopy()
        this.hiddenAngle = toCopy.hiddenAngle.deepCopy()
        this.hiddenAngleVel = toCopy.hiddenAngleVel.deepCopy()
        this.hiddenBias = toCopy.hiddenBias.deepCopy()
        this.outputBias = toCopy.outputBias.deepCopy()
        this.inputToHiddenWeights = toCopy.inputToHiddenWeights.deepCopy()
        this.hiddenToHiddenWeights = toCopy.hiddenToHiddenWeights.deepCopy()
        this.hiddenToOutputWeights = toCopy.hiddenToOutputWeights.deepCopy()
    }

    override fun setInput(index: Int, value: Float) {
        inputVector[index] = value * INPUT_SCALING
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
        for (i in hiddenToOutputWeights.indices)
            hiddenToOutputWeights.data[i] += rand.nextGaussian().toFloat() * amount
    }

    override fun getParameters(): List<Float> = hiddenBias.toList() + outputBias.toList() +
            inputToHiddenWeights.toList() + hiddenToHiddenWeights.toList() +
            hiddenToOutputWeights.toList()

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
        for (i in hiddenToOutputWeights.indices)
            hiddenToOutputWeights.data[i] = params[index++]
    }

    override fun update(dt: Float) {
        hiddenBuffer = ((hiddenToHiddenWeights dot hiddenOut) + (inputToHiddenWeights dot inputVector) * INPUT_SCALING + hiddenBias) * WEIGHT_SCALE
//        outputVector = ((hiddenToOutputWeights dot hiddenOut) + outputBias) * (OUTPUT_SCALING) // scale here? might need custom scale?
        outputVector = ((hiddenToOutputWeights dot hiddenOut)) * (OUTPUT_SCALING) // scale here? might need custom scale?
        hiddenAngleVel = hiddenAngleVel + (hiddenBuffer * lSigma.toFloat() - DAMPENING*hiddenAngleVel - (w_e.toFloat()/2) * (hiddenAngle*2f).sin()) * w_ex.toFloat() * DT
        hiddenAngle = hiddenAngle + hiddenAngleVel * DT

        hiddenOut = hiddenAngleVel.map { it * B.toFloat() * FEMTO.toFloat() }
    }

    override fun clone() = DenseWashboardCyclic(this)

    override fun reset() {
        for (i in outputVector.indices)
            outputVector[i] = 0f
        for (i in hiddenBuffer.indices)
            hiddenBuffer[i] = 0f
        for (i in hiddenOut.indices)
            hiddenOut[i] = 0f
        for (i in hiddenAngle.indices)
            hiddenAngle[i] = ANGLE_INIT
        for (i in hiddenAngleVel.indices)
            hiddenAngleVel[i] = 0f

    }

    override fun dispose() {

    }
}