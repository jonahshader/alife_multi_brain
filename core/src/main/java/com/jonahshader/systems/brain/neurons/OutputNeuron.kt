package com.jonahshader.systems.brain.neurons

class OutputNeuron : Neuron() {
    init {
        neuronCategory = NeuronCategory.OUTPUT
    }
    override fun update(inputSum: Float, dt: Float) {
        outputBuffer = inputSum + bias
    }
}