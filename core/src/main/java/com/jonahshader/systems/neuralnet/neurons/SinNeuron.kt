package com.jonahshader.systems.neuralnet.neurons

import kotlin.math.sin

class SinNeuron : Neuron() {
    init {
        neuronName = NeuronName.Sin
        color.set(.8f, .2f, .2f, 1f)
    }

    override fun update(dt: Float) {
        outputBuffer = sin(inputSum + bias)
    }
}