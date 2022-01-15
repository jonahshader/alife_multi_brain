package com.jonahshader.systems.brain.neurons

class OutputNeuron : Neuron() {
    init {
        neuronCategory = NeuronCategory.OUTPUT
        neuronType = NeuronType.Output
    }
    override fun update(dt: Float) {
        outputBuffer = inputSum + bias
    }
}