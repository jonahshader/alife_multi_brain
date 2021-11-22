package com.jonahshader.systems.brain.neurons

import kotlin.math.tanh

class TanhNeuron : Neuron() {
    override fun update(inputSum: Float, dt: Float) {
       outputBuffer = tanh(inputSum + bias)
    }
}