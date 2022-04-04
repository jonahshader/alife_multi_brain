package com.jonahshader.systems.neuralnet.washboard

import com.jonahshader.systems.neuralnet.Network
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.zeros
import org.jetbrains.kotlinx.multik.ndarray.data.D1
import org.jetbrains.kotlinx.multik.ndarray.data.NDArray
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.data.set
import org.jetbrains.kotlinx.multik.ndarray.operations.*
import org.nd4j.linalg.api.ndarray.INDArray

class WashboardInputConverter : Network {
    companion object {
        private const val CURRENT_PEAK_DURATION = 4.0e-12f
        private const val CURRENT_PEAK = 0.00127f
        private const val SPIKE_SHIFT_PER_INPUT_UNIT = CURRENT_PEAK_DURATION * 4
    }

    private val wbNetwork: Network
    private val oneSpike: Boolean
    private var input: NDArray<Float, D1>
    private var time: Float

    constructor(wbNetwork: Network, oneSpike: Boolean) {
        this.wbNetwork = wbNetwork
        this.oneSpike = oneSpike
        this.input = mk.zeros(wbNetwork.getInputSize())
        time = 0f
    }

    constructor(toCopy: WashboardInputConverter) {
        this.wbNetwork = toCopy.wbNetwork.clone()
        this.oneSpike = toCopy.oneSpike
        this.input = toCopy.input.deepCopy()
        this.time = toCopy.time
    }

    override val multithreadable = true

    override fun setInput(index: Int, value: Float) {
        input[index] = value
    }

    override fun getOutput(index: Int) = wbNetwork.getOutput(index)

    override fun getInputSize() = wbNetwork.getInputSize()

    override fun getOutputSize() = wbNetwork.getOutputSize()

    override fun mutateParameters(amount: Float) {
        wbNetwork.mutateParameters(amount)
    }

    override fun getParameters() = wbNetwork.getParameters()
    override fun setParameters(params: INDArray) {
        TODO("Not yet implemented")
    }

//    override fun setParameters(params: List<Float>) {
//        wbNetwork.setParameters(params)
//    }

    override fun update(dt: Float) {
        val p = (time - input * SPIKE_SHIFT_PER_INPUT_UNIT) * (1/CURRENT_PEAK_DURATION)
        var inputCurrent = mk.math.max((p * (1f-p)) * 4f)
        wbNetwork.update(dt)
        time += dt
    }

    override fun clone() = WashboardInputConverter(this)

    override fun reset() {
        wbNetwork.reset()
        time = 0f
    }

    override fun dispose() {
        wbNetwork.dispose()
    }
}