package com.jonahshader.systems.neuralnet.layers

import org.jetbrains.kotlinx.multik.ndarray.data.D1
import org.jetbrains.kotlinx.multik.ndarray.data.D2
import org.jetbrains.kotlinx.multik.ndarray.data.NDArray

class StandardLayer : Layer {
    private val weights: NDArray<Float, D2>
    private val biases: NDArray<Float, D1>

    override fun update(input: NDArray<Float, D1>, dt: Float): NDArray<Float, D1> {
        TODO("Not yet implemented")
    }

    override fun mutateParameters(amount: Float) {
        TODO("Not yet implemented")
    }

    override fun getParameters(): NDArray<Float, D1> {
        TODO("Not yet implemented")
    }

    override fun getParamCount(): Int {
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