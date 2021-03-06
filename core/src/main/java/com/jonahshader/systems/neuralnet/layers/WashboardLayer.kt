package com.jonahshader.systems.neuralnet.layers

import com.jonahshader.systems.math.Metric.FEMTO
import com.jonahshader.systems.neuralnet.neurons.WashboardNeuron.Companion.B
import com.jonahshader.systems.neuralnet.neurons.WashboardNeuron.Companion.lSigma
import com.jonahshader.systems.neuralnet.neurons.WashboardNeuron.Companion.w_e
import com.jonahshader.systems.neuralnet.neurons.WashboardNeuron.Companion.w_ex
import com.jonahshader.systems.utils.Rand
import org.jetbrains.kotlinx.multik.api.*
import org.jetbrains.kotlinx.multik.api.linalg.dot
import org.jetbrains.kotlinx.multik.api.math.sin
import org.jetbrains.kotlinx.multik.ndarray.data.D1
import org.jetbrains.kotlinx.multik.ndarray.data.D2
import org.jetbrains.kotlinx.multik.ndarray.data.NDArray
import org.jetbrains.kotlinx.multik.ndarray.operations.minus
import org.jetbrains.kotlinx.multik.ndarray.operations.plus
import org.jetbrains.kotlinx.multik.ndarray.operations.plusAssign
import org.jetbrains.kotlinx.multik.ndarray.operations.times

class WashboardLayer : Layer {
    var weightScale = 6f
    private val inputSize: Int
    private val outputSize: Int
    var pLayer: WashboardLayer? = null
    private var weights: NDArray<Float, D2>
    var bias = 0.0025f // .002
    private var dampening = 0.01f
    private var thetaInit = 0.55f
    private var output: NDArray<Float, D1>
    private var pOutput: NDArray<Float, D1>
    private var theta: NDArray<Float, D1>
    private var angularVel: NDArray<Float, D1>

    private var k1vel: NDArray<Float, D1>
    private var k2vel: NDArray<Float, D1>
    private var k3vel: NDArray<Float, D1>

    var euler = false
    constructor(inputSize: Int, outputSize: Int) {
        this.inputSize = inputSize
        this.outputSize = outputSize
        this.output = mk.zeros(outputSize)
        this.pOutput = output
        this.theta = mk.d1array(outputSize) { thetaInit }
        this.angularVel = mk.zeros(outputSize)
        this.k1vel = mk.zeros(outputSize)
        this.k2vel = mk.zeros(outputSize)
        this.k3vel = mk.zeros(outputSize)
        weights = mk.d2array(outputSize, inputSize) { Rand.randx.nextGaussian().toFloat() }
    }

    constructor(c: WashboardLayer) {
        inputSize = c.inputSize
        outputSize = c.outputSize
        output = c.output.deepCopy()
        pOutput = c.pOutput.deepCopy()
        theta = c.theta.deepCopy()
        angularVel = c.angularVel.deepCopy()
        k1vel = c.k1vel.deepCopy()
        k2vel = c.k2vel.deepCopy()
        k3vel = c.k3vel.deepCopy()
        weights = c.weights.deepCopy()
    }

    private fun accel(ic: NDArray<Float, D1>, theta: NDArray<Float, D1>, angularVel: NDArray<Float, D1>) : NDArray<Float, D1> =
        (ic * lSigma.toFloat() - dampening*angularVel - (w_e.toFloat()/2) * (theta*2f).sin()) * w_ex.toFloat()


    override fun update(input: LayerIO, dt: Float): LayerIO {
        pOutput = output
        val scaledWeights = weights * weightScale
        val weightedBiasedInput = (scaledWeights dot input.value) + bias
//
        val k1pos = dt * angularVel
        k1vel = dt * accel(weightedBiasedInput, theta, angularVel)

        val k2pos = dt * (angularVel + .5f * k1vel)
        k2vel = dt * accel(weightedBiasedInput + (.5f * scaledWeights dot input.k1), theta + .5f * k1pos, angularVel + .5f * k1vel)

        val k3pos = dt * (angularVel + .5f * k2vel)
        k3vel = dt * accel(weightedBiasedInput + (.5f * scaledWeights dot input.k2), theta + .5f * k2pos, angularVel + .5f * k2vel)

        val k4pos = dt * (angularVel + k3vel)
        val k4vel = dt * accel(weightedBiasedInput + (scaledWeights dot input.k3), theta + k3pos, angularVel + k3vel)

        theta.plusAssign((k1pos + 2f * k2pos + 2f * k3pos + k4pos) * (1/6f))
        angularVel.plusAssign((k1vel + 2f * k2vel + 2f * k3vel + k4vel) * (1/6f))

        val outputScale = ((B * FEMTO).toFloat())
        output = angularVel * outputScale
        return LayerIO(output, k1vel * outputScale, k2vel * outputScale, k3vel * outputScale)
    }

//    override fun update(input: NDArray<Float, D1>, dt: Float): NDArray<Float, D1> {
//        pOutput = output
//        if (euler) {
//            val weightedBiasedInput = (weightScale * (weights dot input)) + bias
//            val accel = accel(weightedBiasedInput, theta, angularVel)
//            angularVel.plusAssign(accel * dt)
//            theta.plusAssign(angularVel * dt)
//        } else {
//            if (pLayer == null) {
//                val weightedBiasedInput = (weightScale * (weights dot input)) + bias
//                val k1pos = dt * angularVel
//                k1vel = dt * accel(weightedBiasedInput, theta, angularVel)
//
//                val k2pos = dt * (angularVel + .5f * k1vel)
//                k2vel = dt * accel(weightedBiasedInput, theta + .5f * k1pos, angularVel + .5f * k1vel)
//
//                val k3pos = dt * (angularVel + .5f * k2vel)
//                k3vel = dt * accel(weightedBiasedInput, theta + .5f * k2pos, angularVel + .5f * k2vel)
//
//                val k4pos = dt * (angularVel + k3vel)
//                val k4vel = dt * accel(weightedBiasedInput, theta + k3pos, angularVel + k3vel)
//
//                theta.plusAssign((k1pos + 2f * k2pos + 2f * k3pos + k4pos) * (1/6f))
//                angularVel.plusAssign((k1vel + 2f * k2vel + 2f * k3vel + k4vel) * (1/6f))
//            } else {
//                val pl = pLayer!!
//                val weightedBiasedInput = (weightScale * weights dot pl.pOutput) + bias
//
//                val k1pos = dt * angularVel
//                k1vel = dt * accel(weightedBiasedInput, theta, angularVel)
//
//                val k2pos = dt * (angularVel + .5f * k1vel)
//                k2vel = dt * accel(weightedBiasedInput + ((.5f * weights dot pl.k1vel) * (B * FEMTO).toFloat()), theta + .5f * k1pos, angularVel + .5f * k1vel)
//
//                val k3pos = dt * (angularVel + .5f * k2vel)
//                k3vel = dt * accel(weightedBiasedInput + ((.5f * weights dot pl.k2vel) * (B * FEMTO).toFloat()), theta + .5f * k2pos, angularVel + .5f * k2vel)
//
//                val k4pos = dt * (angularVel + k3vel)
//                val k4vel = dt * accel(weightedBiasedInput + ((weights dot pl.k3vel) * (B * FEMTO).toFloat()), theta + k3pos, angularVel + k3vel)
//
//                theta.plusAssign((k1pos + 2f * k2pos + 2f * k3pos + k4pos) * (1/6f))
//                angularVel.plusAssign((k1vel + 2f * k2vel + 2f * k3vel + k4vel) * (1/6f))
//            }
//        }
//
//        output = angularVel *((B * FEMTO).toFloat())
//        return output
//    }


    override fun mutateParameters(amount: Float) {
        weights.plusAssign(mk.d2array(outputSize, inputSize) { Rand.randx.nextGaussian().toFloat() * amount })
    }

    override fun getParameters(): NDArray<Float, D1> {
        return weights.reshape(weights.size)
    }

    override fun getParamCount(): Int {
        return inputSize * outputSize
    }

    override fun setParameters(params: NDArray<Float, D1>) {
        weights = params.reshape(outputSize, inputSize)
    }

    override fun clone() = WashboardLayer(this)

    override fun reset() {
        this.output = mk.zeros(outputSize)
        this.pOutput = output
        this.theta = mk.d1array(outputSize) { thetaInit }
        this.angularVel = mk.zeros(outputSize)
        this.k1vel = mk.zeros(outputSize)
        this.k2vel = mk.zeros(outputSize)
        this.k3vel = mk.zeros(outputSize)
    }
}