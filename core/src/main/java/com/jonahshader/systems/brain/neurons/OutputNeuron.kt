package com.jonahshader.systems.brain.neurons

class OutputNeuron : Neuron() {
    init {
        removable = false
    }
    override fun update(inputSum: Float, dt: Float) {
        outputBuffer = inputSum + bias
    }
}