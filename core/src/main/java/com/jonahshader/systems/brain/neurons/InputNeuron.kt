package com.jonahshader.systems.brain.neurons

import java.util.*

class InputNeuron : Neuron {
    var value = 0.0f

    override fun update(inputSum: Float, dt: Float) {}
    override fun getOutput(): Float = value
    override fun updateOutput() {}
    override fun mutate(rand: Random, magnitude: Float) {}
    override fun removable() = false
}