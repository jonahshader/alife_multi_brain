package com.jonahshader.systems.brain.neurons

import kotlin.math.sin

class SinNeuron : Neuron() {
    init {
        neuronType = NeuronType.Sin
    }

    override fun update(dt: Float) {
        outputBuffer = sin(inputSum + bias)
    }
}