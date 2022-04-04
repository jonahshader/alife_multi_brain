package com.jonahshader.systems.neuralnet

import com.badlogic.gdx.utils.Disposable
import org.nd4j.linalg.api.ndarray.INDArray

typealias NetworkBuilder = (Int, Int) -> Network

interface Network : Disposable {
    val multithreadable: Boolean
    fun setInput(index: Int, value: Float)
    fun getOutput(index: Int) : Float

    fun getInputSize() : Int
    fun getOutputSize() : Int

    fun mutateParameters(amount: Float)
    fun getParameters() : INDArray
    fun setParameters(params: INDArray)

    fun update(dt: Float)
    fun clone() : Network
    fun reset()
}