package com.jonahshader.systems.neuralnet.neurons

class OutputNeuron : Neuron() {
    init {
        neuronCategory = NeuronCategory.OUTPUT
        neuronName = NeuronName.Output
        color.set(1f, 1f, .125f, 1f)
    }
    override fun update(dt: Float) {
        outputBuffer = inputSum + bias
    }
}