package com.jonahshader.systems.brain

import com.jonahshader.systems.brain.densecyclic.DenseCyclicNetwork

typealias NetworkBuilder = (Int, Int) -> Network

fun makeDenseNetworkBuilder(hiddenSize: Int) : NetworkBuilder = { input, output -> DenseCyclicNetwork(input, hiddenSize, output) }

interface Network {
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