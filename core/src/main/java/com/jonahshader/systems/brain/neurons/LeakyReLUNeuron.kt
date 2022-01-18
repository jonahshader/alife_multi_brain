package com.jonahshader.systems.brain.neurons

class LeakyReLUNeuron : Neuron() {
    init {
        neuronType = NeuronType.LeakyReLU
    }
    override fun update(dt: Float) {
        outputBuffer = inputSum + bias
        if (outputBuffer < 0) outputBuffer *= 0.025f
        // since this neuron has no upper bound, limit it
        outputBuffer = outputBuffer.coerceIn(-4.0f, 4.0f)
    }
}