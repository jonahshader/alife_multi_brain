package com.jonahshader.systems.brain.neurons

class OutputNeuron : Neuron() {
    init {
        neuronType = NeuronType.OUTPUT
    }
    override fun update(inputSum: Float, dt: Float) {
        outputBuffer = inputSum + bias
    }
}