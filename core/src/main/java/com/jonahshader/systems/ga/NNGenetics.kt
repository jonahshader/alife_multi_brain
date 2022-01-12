package com.jonahshader.systems.ga

import com.jonahshader.systems.brain.neurons.HiddenType
import com.jonahshader.systems.brain.neurons.Neuron
import java.util.*
import kotlin.math.roundToInt

class WeightGene(var sourceNeuronIndex: Int, var destinationNeuronIndex: Int, var weight: Float) {
    fun mutateScalars(rand: Random, amount: Float) {
        weight += rand.nextGaussian().toFloat() * amount

    }

    fun mutateIndices(rand: Random, amount: Float, size: Int) {
        if (rand.nextFloat() < .2f * amount)
            sourceNeuronIndex = rand.nextInt(size)
        if (rand.nextFloat() < .2f * amount)
            destinationNeuronIndex = rand.nextInt(size)
    }
}

// only hidden neurons are represented. input and output neurons are always derived from body genetics
class NeuronGene(var neuron: HiddenType, var bias: Float){
    fun mutateIndices(rand: Random, amount: Float, size: Int) {
        if (rand.nextFloat() < .2f * amount)
            neuron = HiddenType.getRandom(rand)
    }

    fun mutateScalars(rand: Random, amount: Float) {
        bias += rand.nextGaussian().toFloat() * amount
    }
}

class NNGenetics(var neuronGenes: MutableList<NeuronGene> = mutableListOf(),
                 var weightGenes: MutableList<WeightGene> = mutableListOf()
                 ) {
    fun mutateScalars(rand: Random, amount: Float) {
        neuronGenes.forEach { it.mutateScalars(rand, amount) }
        weightGenes.forEach { it.mutateScalars(rand, amount) }
    }

    fun mutateIndices(rand: Random, amount: Float, size: Int) {
        neuronGenes.forEach { it.mutateIndices(rand, amount, size) }
        weightGenes.forEach { it.mutateIndices(rand, amount, size) }
    }
}