package com.jonahshader.systems.neuralnet.neurons

class OutputNeuron : Neuron() {
    init {
        neuronCategory = NeuronCategory.OUTPUT
        neuronType = NeuronType.Output
    }
    override fun update(dt: Float) {
        outputBuffer = inputSum + bias
    }
}