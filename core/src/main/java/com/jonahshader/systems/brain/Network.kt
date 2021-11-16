package com.jonahshader.systems.brain

import com.jonahshader.systems.brain.neurons.*
import java.util.*

class Network(inputs: Int, outputs: Int, var networkParams: NetworkParams, private val rand: Random) {
    private val inputNeurons = mutableListOf<InputNeuron>()
    private val outputNeurons = mutableListOf<OutputNeuron>()
    private val weights = mutableListOf<NeuronWeights>()

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
            addNeuron(LeakyReLUNeuron(rand, networkParams.weightInitSd))
        }

    }

    fun addNeuron(neuron: Neuron) {
        weights += NeuronWeights(neuron)
    }

    fun setInput(index: Int, value: Float) {
        inputNeurons[index].value = value
    }

    fun getOutput() {

    }

    fun mutate(rand: Random) {

    }

    fun addRandomWeight() : Boolean {
        // pick two neurons at random
        val receiverNeuron = weights.random()
        val sourceNeuron = weights.random().receivingNeuron

        return receiverNeuron.addWeight(Weight(sourceNeuron, rand.nextGaussian().toFloat() * networkParams.weightInitSd))
    }

    fun removeRandomWeight() : Boolean {
        return weights.random().removeRandomWeight(rand)
    }

    fun totalWeights() : Int = weights.fold(0) { acc, neuronWeights ->  acc + neuronWeights.weightCount() }

    fun connect(connectivity: Float) {
        var toAddOrRemove = (weights.size * weights.size * connectivity) - totalWeights()

        while (toAddOrRemove > 0) {
            if (addRandomWeight()) toAddOrRemove--
        }
        while (toAddOrRemove < 0) {
            if (removeRandomWeight()) toAddOrRemove++
        }
    }
}