package com.jonahshader.systems.neuralnet.washboard

import com.jonahshader.systems.math.Metric.FEMTO
import com.jonahshader.systems.neuralnet.Layer
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
import org.jetbrains.kotlinx.multik.ndarray.operations.times

class WashboardLayer(inputSize: Int, outputSize: Int) : Layer {
    var pLayer: WashboardLayer? = null
    private var weights: NDArray<Float, D2>
    private var bias = 0.002f
    private var output: NDArray<Float, D1> = mk.zeros(outputSize)
    private var pOutput = output
    private var theta = mk.d1array(outputSize) { .55f }
    private var angularVel = mk.zeros<Float>(outputSize)
    private var k1pos = mk.zeros<Float>(outputSize)
    private var k2pos = mk.zeros<Float>(outputSize)
    private var k3pos = mk.zeros<Float>(outputSize)
    private var k4pos = mk.zeros<Float>(outputSize)

    private var k1vel = mk.zeros<Float>(outputSize)
    private var k2vel = mk.zeros<Float>(outputSize)
    private var k3vel = mk.zeros<Float>(outputSize)
    private var k4vel = mk.zeros<Float>(outputSize)

    init {
        weights = mk.d2array(outputSize, inputSize) { Rand.randx.nextFloat() }
    }

    private fun accel(inputCurrent: NDArray<Float, D1>, theta: NDArray<Float, D1>, angularVel: NDArray<Float, D1>): NDArray<Float, D1> =
        (lSigma.toFloat() * inputCurrent - .1f*angularVel - (w_e/2).toFloat()*(2f*theta).sin() * w_ex.toFloat())

    override fun update(input: NDArray<Float, D1>, dt: Float): NDArray<Float, D1> {
        pOutput = output

        if (pLayer == null) {
            val weightedBiasedInput = (weights dot input) + bias
            k1pos = dt * angularVel
            k1vel = dt * accel(weightedBiasedInput, theta, angularVel)

            k2pos = dt * (angularVel + .5f * k1vel)
            k2vel = dt * accel(weightedBiasedInput, theta + .5f * k1pos, angularVel + .5f * k1vel)

            k3pos = dt * (angularVel + .5f * k2vel)
            k3vel = dt * accel(weightedBiasedInput, theta + .5f * k2pos, angularVel + .5f * k2vel)

            k4pos = dt * (angularVel + k3vel)
            k4vel = dt * accel(weightedBiasedInput, theta + k3pos, angularVel + k3vel)

            theta += (k1pos + 2f * k2pos + 2f * k3pos + k4pos) * (1/6f)
            angularVel += (k1vel + 2f * k2vel + 2f * k3vel + k4vel) * (1/6f)
        } else {
            val pl = pLayer!!
            val weightedBiasedInput = (weights dot pl.pOutput) + bias

            // TODO: calcualte

            k1pos = dt * angularVel
            k1vel = dt * accel(weightedBiasedInput, theta, angularVel)

            k2pos = dt * (angularVel + .5f * k1vel)
            k2vel = dt * accel(weightedBiasedInput, theta + .5f * k1pos, angularVel + .5f * k1vel)

            k3pos = dt * (angularVel + .5f * k2vel)
            k3vel = dt * accel(weightedBiasedInput, theta + .5f * k2pos, angularVel + .5f * k2vel)

            k4pos = dt * (angularVel + k3vel)
            k4vel = dt * accel(weightedBiasedInput, theta + k3pos, angularVel + k3vel)

            theta += (k1pos + 2f * k2pos + 2f * k3pos + k4pos) * (1/6f)
            angularVel += (k1vel + 2f * k2vel + 2f * k3vel + k4vel) * (1/6f)
        }

        output = (angularVel * (B * FEMTO).toFloat())
    }

    override fun mutateParameters(amount: Float) {
        TODO("Not yet implemented")
    }

    override fun getParameters(): NDArray<Float, D1> {
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