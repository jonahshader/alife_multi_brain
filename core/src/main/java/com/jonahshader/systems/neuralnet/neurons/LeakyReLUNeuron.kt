package com.jonahshader.systems.neuralnet.neurons

class LeakyReLUNeuron : Neuron() {
    init {
        neuronName = NeuronName.LeakyReLU
        color.set(.25f, 1f, .5f, 1f)
    }

    override fun update(dt: Float) {
        outputBuffer = inputSum + bias
        if (outputBuffer < 0) outputBuffer *= 0.025f
        // since this neuron has no upper bound, limit it
        outputBuffer = outputBuffer.coerceIn(-4.0f, 4.0f)
    }
}