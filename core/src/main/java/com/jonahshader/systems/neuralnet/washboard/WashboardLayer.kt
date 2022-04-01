package com.jonahshader.systems.neuralnet.washboard

import com.jonahshader.systems.neuralnet.Layer
import com.jonahshader.systems.utils.Rand
import org.jetbrains.kotlinx.multik.api.*
import org.jetbrains.kotlinx.multik.ndarray.data.D1
import org.jetbrains.kotlinx.multik.ndarray.data.D2
import org.jetbrains.kotlinx.multik.ndarray.data.NDArray

class WashboardLayer(inputSize: Int, outputSize: Int) : Layer {
    private var weights: NDArray<Float, D2>
    private var output: NDArray<Float, D1> = mk.zeros(outputSize)
    private var theta = mk.d1array(outputSize) { 0f }
    init {
        weights = mk.d2array(outputSize, inputSize) { Rand.randx.nextFloat() * 40f }
    }
    override fun update(input: NDArray<Float, D1>, dt: Float): NDArray<Float, D1> {
        TODO("Not yet implemented")
    }

    override fun mutateParameters(amount: Float) {
        TODO("Not yet implemented")
    }

    override fun getParameters(): NDArray<Float, D1> {
        TODO("Not yet implemented")
    }

    override fun setParameters(params: NDArray<Float, D1>) {
        TODO("Not yet implemented")
    }

    override fun clone(): Layer {
        TODO("Not yet implemented")
    }

    override fun reset() {
        TODO("Not yet implemented")
    }
}