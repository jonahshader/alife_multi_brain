package com.jonahshader.systems.neuralnet.layers

import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.zeros
import org.jetbrains.kotlinx.multik.ndarray.data.D1
import org.jetbrains.kotlinx.multik.ndarray.data.D1Array
import org.jetbrains.kotlinx.multik.ndarray.data.NDArray

class LayerIO(val value: D1Array<Float>, val k1: D1Array<Float>, val k2: D1Array<Float>, val k3: D1Array<Float>) {
    constructor(value: D1Array<Float>) : this(value, mk.zeros(value.size), mk.zeros(value.size),mk.zeros(value.size))
}
interface Layer {
    fun update(input: LayerIO, dt: Float) : LayerIO
    fun mutateParameters(amount: Float)
    fun getParameters() : NDArray<Float, D1>
    fun getParamCount() : Int
    fun setParameters(params: NDArray<Float, D1>)
    fun clone() : Layer
    fun reset()
}