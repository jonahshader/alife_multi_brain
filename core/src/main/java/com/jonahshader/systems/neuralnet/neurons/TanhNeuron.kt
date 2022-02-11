package com.jonahshader.systems.neuralnet.neurons

import kotlin.math.tanh

class TanhNeuron : Neuron() {
    init {
        neuronName = NeuronName.Tanh
        color.set(.8f, .2f, .8f, 1f)
    }
    override fun update(dt: Float) {
       outputBuffer = tanh(inputSum + bias)
    }
}