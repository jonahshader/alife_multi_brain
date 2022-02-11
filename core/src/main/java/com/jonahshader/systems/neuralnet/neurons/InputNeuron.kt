package com.jonahshader.systems.neuralnet.neurons

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
        neuronCategory = NeuronCategory.INPUT
        neuronName = NeuronName.Input
        color.set(.125f, 1f, 1f, 1f)
    }
    // don't have anything to mutate
    override fun mutateScalars(rand: Random, amount: Float) {}

    override fun update(dt: Float) {
        outputBuffer = value
    }

    override fun getParameters() = listOf<Float>()
    override fun setParameters(params: List<Float>) {}
}