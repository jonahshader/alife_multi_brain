package com.jonahshader.systems.brain.neurons

import kotlin.math.tanh

class TanhNeuron : Neuron() {
    init {
        neuronType = NeuronType.Tanh
    }
    override fun update(dt: Float) {
       outputBuffer = tanh(inputSum + bias)
    }
}