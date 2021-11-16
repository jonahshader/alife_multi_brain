package com.jonahshader.systems.brain

import com.jonahshader.systems.brain.neurons.Neuron
import java.util.*

class Weight(val sourceNeuron: Neuron, private var weight: Float) {
    override fun toString(): String {
        return sourceNeuron.toString()
    }

    fun calculateWeightedValue() : Float = sourceNeuron.getOutput() * weight

    fun mutate(rand: Random, magnitude: Float) {
        weight += rand.nextGaussian().toFloat() * magnitude
    }
}

class NeuronWeights(val receivingNeuron: Neuron) {
    private val weights = mutableListOf<Weight>()

    fun addWeight(weight: Weight): Boolean {
        return if (containsWeight(weight)) {
            false
        } else {
            weights += weight
            true
        }
    }

    fun disconnectSourceNeuron(sourceNeuron: Neuron): Boolean {
        for (i in weights.indices) {
            val w = weights[i]
            if (w.sourceNeuron.toString() == sourceNeuron.toString()) {
                weights.removeAt(i)
                return true
            }
        }
        return false
    }

    fun containsWeight(weight: Weight): Boolean {
        for (w in weights)
            if (w.toString() == weight.toString())
                return true
        return false
    }

    fun isSourceNeuron(neuron: Neuron): Boolean {
        for (w in weights)
            if (w.sourceNeuron.toString() == neuron.toString())
                return true
        return false
    }

    fun calculateSum() = weights.fold(0.0f) { acc, weight -> acc + weight.calculateWeightedValue() }

    fun mutate(rand: Random, magnitude: Float) {
        weights.forEach { it.mutate(rand, magnitude) }
    }

    fun weightCount(): Int = weights.size
    fun removeRandomWeight(rand: Random): Boolean {
        return if (weights.isNotEmpty()) {
            weights.removeAt(rand.nextInt(weights.size))
            true
        } else {
            false
        }
    }

    fun update(dt: Float) {
        receivingNeuron.update(calculateSum(), dt)
    }

    fun containsNeuron(neuronToRemove: Neuron) = neuronToRemove.toString() == receivingNeuron.toString()
}