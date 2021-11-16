package com.jonahshader.systems.brain.neurons

import java.util.*

class LeakyReLUNeuron : Neuron{
    private var outputBuffer = 0.0f
    private var output = 0.0f
    private var bias = 0.0f

    constructor(rand: Random, magnitude: Float) {
        bias += rand.nextGaussian().toFloat() * magnitude
    }

    override fun update(inputSum: Float, dt: Float) {
        outputBuffer = inputSum + bias
        if (outputBuffer < 0) outputBuffer *= 0.025f
    }

    override fun getOutput(): Float {
        return output
    }

    override fun updateOutput() {
        output = outputBuffer
    }

    override fun mutate(rand: Random, magnitude: Float) {
        bias += rand.nextGaussian().toFloat() * magnitude
    }
}