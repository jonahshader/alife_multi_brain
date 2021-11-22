package com.jonahshader.systems.brain.neurons

import java.util.*

/**
 * this neuron type has a variable "value" that can be
 * modified by Network. this should not be used to
 * represent anything other than inputs (don't use this
 * as a bias neuron, bias is handled per neuron)
 */
class InputNeuron : Neuron() {
    var value = 0.0f
    init {
        // input neurons should not be removed by default
        removable = false
    }
    // don't have anything to mutate
    override fun mutate(rand: Random, magnitude: Float) {}

    override fun update(inputSum: Float, dt: Float) {
        outputBuffer = value
    }
}