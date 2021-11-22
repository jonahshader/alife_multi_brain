package com.jonahshader.systems.brain.neurons

class LeakyReLUNeuron : Neuron() {
    override fun update(inputSum: Float, dt: Float) {
        outputBuffer = inputSum + bias
        if (outputBuffer < 0) outputBuffer *= 0.025f
        // since this neuron has no upper bound, limit it
        outputBuffer = outputBuffer.coerceIn(-8192.0f, 8192.0f)
    }
}