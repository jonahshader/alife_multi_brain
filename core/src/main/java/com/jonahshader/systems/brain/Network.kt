package com.jonahshader.systems.brain

import com.jonahshader.systems.brain.neurons.*
import java.util.*
import kotlin.math.roundToInt

class Network(inputs: Int, outputs: Int, var networkParams: NetworkParams, private val rand: Random) {
    val inputNeurons = mutableListOf<InputNeuron>()
    val outputNeurons = mutableListOf<OutputNeuron>()
    val weights = mutableListOf<NeuronWeights>()

    init {
        for (i in 0 until inputs) {
            val newNeuron = InputNeuron()
            inputNeurons += newNeuron
            addNeuron(newNeuron)
        }
        for (i in 0 until outputs) {
            val newNeuron = OutputNeuron()
            outputNeurons += newNeuron
            addNeuron(newNeuron)
        }

        // for now just add leaky relu. needs to be configurable in the future
        for (i in 0 until networkParams.hiddenNeuronCountInit) {
            val neuron = LeakyReLUNeuron()
            neuron.mutate(rand, 1.25f)
            addNeuron(neuron)
        }

        connect(networkParams.connectivityInit)
    }

    fun update(dt: Float) {
        weights.forEach {
            it.update(dt)
        }
        weights.forEach {
            it.updateOutput()
        }
    }

    fun addNeuron(neuron: Neuron) {
        weights += NeuronWeights(neuron)
    }

    fun removeRandomNeuron() : Boolean {
        if (weights.size > (inputNeurons.size + outputNeurons.size)) {
            while (true) {
                val neuronToRemove = weights.random()
                if (neuronToRemove.receivingNeuron.neuronType == Neuron.NeuronType.HIDDEN) {
                    weights.remove(neuronToRemove)
                    return true
                }
            }
        } else {
            return false
        }
    }

    fun setInput(index: Int, value: Float) {
        inputNeurons[index].value = value
    }

    fun getOutput(index: Int) = outputNeurons[index].out

    fun mutate() {
        var addRemoveNeuronCount = (rand.nextGaussian() * networkParams.addRemoveNeuronSd).roundToInt()
        var addRemoveWeightCount = (rand.nextGaussian() * networkParams.addRemoveWeightSd).roundToInt()

        while (addRemoveNeuronCount > 0) {
            removeRandomNeuron()
            addRemoveNeuronCount--
        }

        while (addRemoveNeuronCount < 0) {
            addNeuron(LeakyReLUNeuron())
            addRemoveNeuronCount++
        }

        while (addRemoveWeightCount > 0) {
            removeRandomWeight()
            addRemoveWeightCount--
        }

        while (addRemoveWeightCount < 0) {
            if (addRandomWeight()) addRemoveWeightCount++
        }
    }

    fun addRandomWeight() : Boolean {
        // pick two neurons at random
        val receiverNeuron = weights.random()
        val sourceNeuron = weights.random().receivingNeuron

        return receiverNeuron.addWeight(Weight(sourceNeuron, rand.nextGaussian().toFloat() * networkParams.weightInitSd))
    }

    fun removeRandomWeight() : Boolean {
        val toRemove = weights.randomOrNull()
        return if (toRemove != null) {
            toRemove.removeRandomWeight(rand)
            true
        } else {
            false
        }
    }

    fun totalWeights() : Int = weights.fold(0) { acc, neuronWeights ->  acc + neuronWeights.weightCount() }

    fun connect(connectivity: Float) {
        var toAddOrRemove = ((weights.size * weights.size * connectivity) - totalWeights()).roundToInt()
        println("to add or remove: $toAddOrRemove")

        while (toAddOrRemove > 0) {
            if (addRandomWeight()) toAddOrRemove--
        }
        while (toAddOrRemove < 0) {
            if (removeRandomWeight()) toAddOrRemove++
        }
    }
}