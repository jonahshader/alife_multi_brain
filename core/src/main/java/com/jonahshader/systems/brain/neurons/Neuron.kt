package com.jonahshader.systems.brain.neurons

import java.util.*

abstract class Neuron {
    enum class NeuronCategory {
        INPUT,
        OUTPUT,
        HIDDEN
    }
    protected var outputBuffer = 0.0f
    var out = 0.0f
        private set
    var bias = 0.0f
    protected var inputSum = 0f

    // indicates if this can be removed by random mutations
    // this should be false for IO related neurons
    var neuronCategory = NeuronCategory.HIDDEN
    protected set
    var neuronType = NeuronType.Input
        protected set

    /**
     * computes output, stores it in outputBuffer
     */
    open fun update(dt: Float){}

    /**
     * resets accumulative values (besides out, outputBuffer)
     */
    open fun resetStateInternals(){}

    /**
     * makes the output the most recent computed value
     * also resets accumulation
     */
    fun updateOutput() {
        out = outputBuffer
        inputSum = 0f
    }

    /**
     * accumulate weighted output from another neuron
     */
    fun accumulate(acc: Float) {
        inputSum += acc
    }

    /**
     * mutates internal variables (i can only think of bias)
     */
    open fun mutateScalars(rand: Random, amount: Float) {
        bias += rand.nextGaussian().toFloat() * amount
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