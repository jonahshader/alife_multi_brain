package com.jonahshader.systems.brain

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

//class Weight(val sourceNeuron: Neuron, var weight: Float) {
//    override fun toString(): String {
//        return sourceNeuron.toString()
//    }
//
//    override fun hashCode(): Int {
//        return sourceNeuron.hashCode()
//    }
//
//    fun calculateWeightedValue() : Float = sourceNeuron.out * weight
//
//    fun mutate(rand: Random, magnitude: Float) {
//        weight += rand.nextGaussian().toFloat() * magnitude
//    }
//
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (javaClass != other?.javaClass) return false
//
//        other as Weight
//
//        if (sourceNeuron != other.sourceNeuron) return false
//
//        return true
//    }
//}
//
//class NeuronWeights(val receivingNeuron: Neuron) {
//    val weights = mutableListOf<Weight>()
//
//    fun addWeight(weight: Weight): Boolean {
//        return if (containsWeight(weight)) {
//            false
//        } else {
//            weights += weight
//            println("added weight, now has ${weights.size} weights.")
//            true
//        }
//    }
//
//    fun disconnectSourceNeuron(sourceNeuron: Neuron): Boolean {
//        for (i in weights.indices) {
//            val w = weights[i]
//            if (w.sourceNeuron.toString() == sourceNeuron.toString()) {
//                weights.removeAt(i)
//                return true
//            }
//        }
//        return false
//    }
//
//    fun containsWeight(weight: Weight): Boolean {
//        for (w in weights)
//            if (w.toString() == weight.toString())
//                return true
//        return false
//    }
//
//    fun isSourceNeuron(neuron: Neuron): Boolean {
//        for (w in weights)
//            if (w.sourceNeuron.toString() == neuron.toString())
//                return true
//        return false
//    }
//
//    fun calculateSum() = weights.fold(0.0f) { acc, weight -> acc + weight.calculateWeightedValue() }
//
//    fun mutate(rand: Random, magnitude: Float) {
//        weights.forEach { it.mutate(rand, magnitude) }
//    }
//
//    fun weightCount(): Int = weights.size
//    fun removeRandomWeight(rand: Random): Boolean {
//        return if (weights.isNotEmpty()) {
//            weights.removeAt(rand.nextInt(weights.size))
//            true
//        } else {
//            false
//        }
//    }
//
//    fun update(dt: Float) {
//        receivingNeuron.update(calculateSum(), dt)
//    }
//
//    fun updateOutput() {
//        receivingNeuron.updateOutput()
//    }
//
//    fun containsNeuron(neuronToRemove: Neuron) = neuronToRemove.toString() == receivingNeuron.toString()
//}