package com.jonahshader.systems.neuralnet

import com.badlogic.gdx.utils.Disposable
import com.jonahshader.systems.neuralnet.densecyclic.DenseCyclicNetwork

typealias NetworkBuilder = (Int, Int) -> Network

interface Network : Disposable {
    val multithreadable: Boolean
    fun setInput(index: Int, value: Float)
    fun getOutput(index: Int) : Float

    fun getInputSize() : Int
    fun getOutputSize() : Int

    fun mutateParameters(amount: Float)
    fun getParameters() : List<Float>
    fun setParameters(params: List<Float>)

    fun update(dt: Float)
    fun clone() : Network
    fun reset()
}