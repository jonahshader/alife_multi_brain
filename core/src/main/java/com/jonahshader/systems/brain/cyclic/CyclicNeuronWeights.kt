package com.jonahshader.systems.brain.cyclic

import com.jonahshader.systems.brain.neurons.Neuron
import java.util.*

class Weight(val sourceNeuron: Neuron, val destNeuron: Neuron, var weight: Float) {
    private fun calculateWeightedValue() : Float = sourceNeuron.out * weight
    fun mutate(rand: Random, magnitude: Float) {
        weight += rand.nextGaussian().toFloat() * magnitude
    }

    fun forwardProp() {
        destNeuron.accumulate(calculateWeightedValue())
    }

    fun isConnectedToNeuron(neuron: Neuron) : Boolean = neuron == sourceNeuron || neuron == destNeuron
    fun isSameEdge(sNeuron: Neuron, dNeuron: Neuron) = sourceNeuron == sNeuron && destNeuron == dNeuron
    fun isSameEdge(w: Weight) = w.sourceNeuron == sourceNeuron && w.destNeuron == destNeuron
}
