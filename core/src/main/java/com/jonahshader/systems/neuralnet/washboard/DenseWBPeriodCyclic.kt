package com.jonahshader.systems.neuralnet.washboard

import com.jonahshader.systems.neuralnet.Network
import com.jonahshader.systems.neuralnet.NetworkBuilder
import com.jonahshader.systems.utils.Rand
import org.jetbrains.kotlinx.multik.api.d1array
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.zeros
import org.jetbrains.kotlinx.multik.ndarray.data.D1
import org.jetbrains.kotlinx.multik.ndarray.data.NDArray
import org.jetbrains.kotlinx.multik.ndarray.data.set
import org.jetbrains.kotlinx.multik.ndarray.operations.inplace
import org.jetbrains.kotlinx.multik.ndarray.operations.map
import org.jetbrains.kotlinx.multik.ndarray.operations.plusAssign
import org.jetbrains.kotlinx.multik.ndarray.operations.times
import java.util.*

class DenseWBPeriodCyclic : DenseWashboardCyclic
{
    companion object {
        fun makeBuilder(hiddenSize: Int) : NetworkBuilder = { input, output -> DenseWBPeriodCyclic(input, hiddenSize, output) }
        private const val ITERATIONS_PER_PERIOD = 80
        private const val CURRENT_PEAK_DURATION = 10f
        private const val CURRENT_PEAK = 0.003f // 0.003
    }
    constructor(toCopy: DenseWashboardCyclic) : super(toCopy)
    constructor(inputSize: Int, hiddenSize: Int, outputSize: Int, rand: Random = Rand.randx) : super(
        inputSize,
        hiddenSize,
        outputSize,
        rand
    )

    private val initialInputVector: NDArray<Float, D1> = mk.zeros(inputVector.size)
    private var avgOutputVector: NDArray<Float, D1> = mk.zeros(outputVector.size)

    override fun setInput(index: Int, value: Float) {
        initialInputVector[index] = value
    }

    override fun update(dt: Float) {
        reset()
        avgOutputVector = mk.zeros(outputVector.size)
        for (i in 0 until ITERATIONS_PER_PERIOD) {
            val p = i / CURRENT_PEAK_DURATION
            val inputCurve = (p * (1-p) * 4).coerceAtLeast(0f) * CURRENT_PEAK / INPUT_SCALING
            inputVector = initialInputVector.map { it * inputCurve }
            super.update(dt)
            avgOutputVector += outputVector * (1f/ITERATIONS_PER_PERIOD)
        }
        outputVector = avgOutputVector.deepCopy()
    }

}