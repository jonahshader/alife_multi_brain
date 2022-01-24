package com.jonahshader.systems.brain.neurons

import kotlin.math.sin

class SinNeuron : Neuron() {
    override fun update(dt: Float) {
        outputBuffer = inputSum + bias
        outputBuffer = sin(outputBuffer)
    }
}