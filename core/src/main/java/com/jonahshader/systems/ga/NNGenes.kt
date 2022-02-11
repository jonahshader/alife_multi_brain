package com.jonahshader.systems.ga

import com.jonahshader.systems.neuralnet.neurons.Neuron
import com.jonahshader.systems.neuralnet.neurons.NeuronName
import java.util.*

class WeightGene(var sourceNeuronIndex: Int, var destinationNeuronIndex: Int, var weight: Float) {
    fun mutateScalars(rand: Random, amount: Float) {
        weight += rand.nextGaussian().toFloat() * amount
    }

    fun mutateIndices(rand: Random, probability: Float, size: Int) {
        if (rand.nextFloat() < probability)
            sourceNeuronIndex = rand.nextInt(size)
        if (rand.nextFloat() < probability)
            destinationNeuronIndex = rand.nextInt(size)
    }
}

// only hidden neurons are represented. input and output neurons are always derived from body genetics
class NeuronGene(var neuron: NeuronName, var state: FloatArray){
    fun mutateIndices(rand: Random, probability: Float) {
        if (rand.nextFloat() < probability)
            neuron = NeuronName.getRandomHidden(rand)
    }

    fun mutateScalars(rand: Random, amount: Float) {
        for (i in state.indices) {
            state[i] += rand.nextGaussian().toFloat() * amount
        }
    }

    fun makeNeuron(): Neuron = NeuronName.make(this)
}

class NNGenes(var neuronGenes: MutableList<NeuronGene> = mutableListOf(),
              var weightGenes: MutableList<WeightGene> = mutableListOf(),
              var outputBiasGenes: MutableList<Float> = mutableListOf()
                 ) {
    fun mutateScalars(rand: Random, amount: Float) {
        neuronGenes.forEach { it.mutateScalars(rand, amount) }
        weightGenes.forEach { it.mutateScalars(rand, amount) }
        for (i in outputBiasGenes.indices) {
            outputBiasGenes[i] += rand.nextGaussian().toFloat() * amount
        }
    }

    // totalNeurons includes input output and hidden neurons
    fun mutateIndices(rand: Random, probability: Float, totalNeurons: Int) {
        neuronGenes.forEach { it.mutateIndices(rand, probability) }
        weightGenes.forEach { it.mutateIndices(rand, probability, totalNeurons) }
    }
}