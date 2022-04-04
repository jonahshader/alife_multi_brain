package com.jonahshader.systems.neuralnet

import org.nd4j.linalg.api.ndarray.INDArray

interface Layer {
    fun update(input: INDArray, dt: Float) : INDArray
    fun mutateParameters(amount: Float)
    fun getParameters() : INDArray
    fun setParameters(params: INDArray)
    fun clone() : Layer
    fun reset()
}