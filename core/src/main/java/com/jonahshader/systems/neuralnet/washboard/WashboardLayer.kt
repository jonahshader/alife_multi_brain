package com.jonahshader.systems.neuralnet.washboard

import com.jonahshader.systems.math.Metric.FEMTO
import com.jonahshader.systems.math.minus
import com.jonahshader.systems.math.plus
import com.jonahshader.systems.math.plusAssign
import com.jonahshader.systems.math.times
import com.jonahshader.systems.neuralnet.Layer
import com.jonahshader.systems.neuralnet.neurons.WashboardNeuron
import com.jonahshader.systems.neuralnet.neurons.WashboardNeuron.Companion.B
import com.jonahshader.systems.utils.Rand
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.sin

class WashboardLayer : Layer {
    constructor(inputSize: Int, outputSize: Int) {
//        this.weights = mk.d2array(outputSize, inputSize) { (Rand.randx.nextGaussian().toFloat() + 1f).absoluteValue }
//        this.theta = mk.d1array(outputSize) { THETA_INIT }
//        this.angularVel = mk.d1array(outputSize) { 0f }
//        this.weights = Nd4j.rand(Nd4j.getDistributions().createUniform(0.0, 1.0), outputSize.toLong(), inputSize.toLong())
        this.weights = Nd4j.rand(outputSize, inputSize)
        this.theta = Nd4j.zeros(outputSize).add(THETA_INIT)
        this.angularVel = Nd4j.zeros(outputSize)
    }

    constructor(toCopy: WashboardLayer) {
        this.weights = toCopy.weights.dup()
        this.theta = toCopy.theta.dup()
        this.angularVel = toCopy.angularVel.dup()
    }

    companion object {
        private const val DAMPENING = 0.01f
        private const val THETA_INIT = 0.56f
    }
    private var weights: INDArray
    private var theta: INDArray
    private var angularVel: INDArray
    private val bias = 0.0023f
    private val weightScalar = 8f

    private fun accel(inputCurrent: INDArray, theta: INDArray, angularVel: INDArray) = (WashboardNeuron.lSigma.toFloat() * inputCurrent - DAMPENING*angularVel - (WashboardNeuron.w_e.toFloat() /2)* Nd4j.math.sin(2f*theta)) * WashboardNeuron.w_ex.toFloat()
//    private fun accel(inputCurrent: INDArray, theta: INDArray, angularVel: INDArray): INDArray = (inputCurrent.mul(WashboardNeuron.lSigma).mul(-DAMPENING*angularVel) - (WashboardNeuron.w_e.toFloat() /2)* (2f*theta).sin()) * WashboardNeuron.w_ex.toFloat()


    override fun update(input: INDArray, dt: Float): INDArray {
        val inputCurrent = (weights.mul(input).mul(weightScalar)).add(bias)
//        val
        val k1y = dt * angularVel
        val k1v = dt * accel(inputCurrent, theta, angularVel)

        val k2y = dt * (angularVel + .5f * k1v)
        val k2v = dt * accel(inputCurrent, theta + .5f * k1y, angularVel + .5f * k1v)

        val k3y = dt * (angularVel + .5f * k2v)
        val k3v = dt * accel(inputCurrent, theta + .5f * k2y, angularVel + .5f * k2v)

        val k4y = dt * (angularVel + k3v)
        val k4v = dt * accel(inputCurrent, theta + k3y, angularVel + k3v)

        theta.plusAssign((k1y + 2f * k2y + 2f * k3y + k4y) * (1/6.0f))
        angularVel.plusAssign((k1v + 2f * k2v + 2f * k3v + k4v) * (1/6.0f))

        return angularVel * (B * FEMTO).toFloat()
    }

    override fun mutateParameters(amount: Float) {
        weights.plusAssign(Nd4j.randn(weights.shape()[0], weights.shape()[1]) * amount)
        weights = Nd4j.math.abs(weights)
//        weights.
//        for (i in weights.indices) {
//            weights.data[i] += Rand.randx.nextGaussian().toFloat() * amount
//            weights.data[i] = weights.data[1].absoluteValue
//        }
    }

    override fun getParameters() = Nd4j.toFlattened(weights).transpose()

    override fun setParameters(params: INDArray) {
        weights = Nd4j.math.max(params.dup(), Nd4j.zeros(weights.shape()[0], weights.shape()[1]))
    }

    override fun clone(): Layer = WashboardLayer(this)

    override fun reset() {
        this.theta = Nd4j.zeros(theta.size(0)).add(THETA_INIT)
        this.angularVel = Nd4j.zeros(angularVel.size(0))
    }
}


