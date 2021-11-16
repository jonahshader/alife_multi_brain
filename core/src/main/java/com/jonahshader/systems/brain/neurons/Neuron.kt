package com.jonahshader.systems.brain.neurons

import java.util.*

interface Neuron {
    fun update(inputSum: Float, dt: Float) // computes output, stores it somewhere to be swapped by updateOutput
    fun getOutput() : Float // gets output
    fun updateOutput() // makes the output the most recent computed value
    fun mutate(rand: Random, magnitude: Float) // mutates internal variables (i can only think of bias)
    fun removable() : Boolean
}