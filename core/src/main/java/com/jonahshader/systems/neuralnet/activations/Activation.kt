package com.jonahshader.systems.neuralnet.activations

interface Activation {
    fun activate(input: Float) : Float
}