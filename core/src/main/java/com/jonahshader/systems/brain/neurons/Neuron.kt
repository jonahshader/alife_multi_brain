package com.jonahshader.systems.brain.neurons

import java.util.*

abstract class Neuron {
    enum class NeuronType {
        INPUT,
        OUTPUT,
        HIDDEN
    }
    protected var outputBuffer = 0.0f
    var out = 0.0f
        private set
    protected var bias = 0.0f

    // indicates if this can be removed by random mutations
    // this should be false for IO related neurons
    var neuronType = NeuronType.HIDDEN

    /**
     * computes output, stores it in outputBuffer
     */
    open fun update(inputSum: Float, dt: Float){}

    /**
     * resets accumulative values (besides out, outputBuffer)
     */
    open fun resetStateInternals(){}

    /**
     * makes the output the most recent computed value
     */
    fun updateOutput() {
        out = outputBuffer
    }

    /**
     * mutates internal variables (i can only think of bias)
     */
    open fun mutate(rand: Random, magnitude: Float) {
        bias += rand.nextGaussian().toFloat() * magnitude
    }

    /**
     * resetState() reset any integrator variables or anything representing state
     * from a previous time step. when duplicating
     */
    fun resetState() {
        outputBuffer = 0.0f
        out = 0.0f
        resetStateInternals()
    }
}