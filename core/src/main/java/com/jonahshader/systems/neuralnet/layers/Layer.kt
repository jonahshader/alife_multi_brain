package com.jonahshader.systems.neuralnet.layers

import org.jetbrains.kotlinx.multik.ndarray.data.D1
import org.jetbrains.kotlinx.multik.ndarray.data.NDArray

interface Layer {
    fun update(input: NDArray<Float, D1>, dt: Float) : NDArray<Float, D1>
    fun mutateParameters(amount: Float)
    fun getParameters() : NDArray<Float, D1>
    fun getParamCount() : Int
    fun setParameters(params: NDArray<Float, D1>)
    fun clone() : Layer
    fun reset()
}