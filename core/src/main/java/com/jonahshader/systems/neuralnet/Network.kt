package com.jonahshader.systems.neuralnet

import com.badlogic.gdx.utils.Disposable
import com.jonahshader.systems.neuralnet.densecyclic.DenseCyclicNetwork
import org.jetbrains.kotlinx.multik.ndarray.data.D1
import org.jetbrains.kotlinx.multik.ndarray.data.NDArray

typealias NetworkBuilder = (Int, Int) -> Network

interface Network : Disposable {
    val multithreadable: Boolean
    fun setInput(index: Int, value: Float)
    fun getOutput(index: Int) : Float

    fun getInputSize() : Int
    fun getOutputSize() : Int

    fun mutateParameters(amount: Float)
    fun getParameters() : NDArray<Float, D1>
    fun setParameters(params: NDArray<Float, D1>)

    fun update(dt: Float)
    fun clone() : Network
    fun reset()
}