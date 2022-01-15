package com.jonahshader.systems.brain

import com.jonahshader.systems.brain.neurons.*
import com.jonahshader.systems.ga.NNGenes
import com.jonahshader.systems.ga.NeuronGene
import com.jonahshader.systems.ga.WeightGene
import com.jonahshader.systems.utils.Rand
import java.util.*
import kotlin.math.roundToInt
import kotlin.random.asKotlinRandom

class Network {
    var networkParams: NetworkParams = NetworkParams()
    internal val rand: Random
    private val kRand: kotlin.random.Random
    val inputNeurons = mutableListOf<InputNeuron>()
    val outputNeurons = mutableListOf<OutputNeuron>()
    val hiddenNeurons = mutableListOf<Neuron>()
    val weights = mutableListOf<Weight>()
    val sourceNeurons: List<Neuron> get() = inputNeurons + hiddenNeurons
    val destNeurons: List<Neuron> get() = hiddenNeurons + outputNeurons

    constructor(inputs: Int, outputs: Int, networkParams: NetworkParams, rand: Random = Rand.randx) {
        this.networkParams = networkParams
        this.rand = rand
        kRand = rand.asKotlinRandom()
        for (i in 0 until inputs) {
            inputNeurons += InputNeuron()
        }
        for (i in 0 until outputs) {
            outputNeurons += OutputNeuron()
        }
        for (i in 0 until networkParams.hiddenNeuronCountInit) {
            val neuron = LeakyReLUNeuron()
            neuron.mutateScalars(rand, 1.25f)
            hiddenNeurons += neuron
        }
        connect(networkParams.connectivityInit)
    }

    constructor(otherNetwork: Network) : this(otherNetwork.makeGenes(), otherNetwork.inputNeurons.size, otherNetwork.outputNeurons.size)

    constructor(genes: NNGenes, inputs: Int, outputs: Int, rand: Random = Rand.randx) {
        this.rand = rand
        kRand = rand.asKotlinRandom()
        for (i in 0 until inputs) {
            val newNeuron = InputNeuron()
            inputNeurons += newNeuron
        }
        for (i in 0 until outputs) {
            val newNeuron = OutputNeuron()
            newNeuron.bias = genes.outputBiasGenes[i]
            outputNeurons += newNeuron
        }
        for (n in genes.neuronGenes) {
            val newNeuron = NeuronType.make(n.neuron)
            newNeuron.bias = n.bias
            hiddenNeurons += newNeuron
        }

        val allN = getAllNeurons()
        for (w in genes.weightGenes) {
            val srcNeuron = allN[w.sourceNeuronIndex]
            val destNeuron = allN[w.destinationNeuronIndex]
            val newWeight = Weight(srcNeuron, destNeuron, w.weight)
            weights += newWeight
        }
    }

    fun update(dt: Float) {
        weights.forEach { it.forwardProp() }

        inputNeurons.forEach { it.update(dt) }
        hiddenNeurons.forEach { it.update(dt) }
        outputNeurons.forEach { it.update(dt) }

        inputNeurons.forEach { it.updateOutput() }
        hiddenNeurons.forEach { it.updateOutput() }
        outputNeurons.forEach { it.updateOutput() }
    }

    private fun removeRandomNeuron() : Boolean =
        if (hiddenNeurons.isNotEmpty()) {
            val toRemove = hiddenNeurons.random(kRand)
            weights.removeIf { it.isConnectedToNeuron(toRemove) }
            hiddenNeurons.remove(toRemove)
            true
        } else { false }


    fun setInput(index: Int, value: Float) {
        inputNeurons[index].value = value
    }

    fun getOutput(index: Int) = outputNeurons[index].out

    private fun generateRandomHiddenNeuron() : Neuron {
        val n = NeuronType.makeRandomHidden(rand)
        n.bias = networkParams.weightInitSd * rand.nextGaussian().toFloat()
        return n
    }

    fun mutate() {
        var addRemoveNeuronCount = (rand.nextGaussian() * networkParams.addRemoveNeuronSd).roundToInt()
        var addRemoveWeightCount = (rand.nextGaussian() * networkParams.addRemoveWeightSd).roundToInt()

        addRemoveNeuronCount = addRemoveNeuronCount.coerceAtLeast(-hiddenNeurons.size)
        addRemoveWeightCount = addRemoveWeightCount.coerceAtLeast(-weights.size)

        while (addRemoveNeuronCount < 0) {
            removeRandomNeuron()
            addRemoveNeuronCount++
        }

        while (addRemoveNeuronCount > 0) {
            hiddenNeurons += generateRandomHiddenNeuron()
            addRemoveNeuronCount--
        }

        while (addRemoveWeightCount < 0) {
            removeRandomWeight()
            addRemoveWeightCount++
        }

        while (addRemoveWeightCount > 0) {
            if (addRandomWeight()) addRemoveWeightCount--
        }

        weights.forEach {
            it.mutate(rand, networkParams.mutateWeightSd)
        }
        hiddenNeurons.forEach {
            it.mutateScalars(rand, networkParams.mutateWeightSd) // bias is a weight basically
        }
        outputNeurons.forEach {
            it.mutateScalars(rand, networkParams.mutateWeightSd)
        }
    }

    fun addRandomWeight() : Boolean {
        // pick two neurons at random
        val sourceNeuron = sourceNeurons.random()
        val destNeuron = destNeurons.random()

        return if (!weightExists(sourceNeuron, destNeuron)) {
            weights += Weight(sourceNeuron, destNeuron, networkParams.weightInitSd * rand.nextGaussian().toFloat())
            true
        } else {
            false
        }
    }

    fun removeRandomWeight() : Boolean {
        return if (weights.isNotEmpty()) {
            weights.removeAt(rand.nextInt(weights.size))
            true
        } else {
            false
        }
    }

    fun connect(connectivity: Float) {
        var toAddOrRemove = ((sourceNeurons.size * destNeurons.size * connectivity) - weights.size).roundToInt()
        println("to add or remove: $toAddOrRemove")

        while (toAddOrRemove > 0) {
            if (addRandomWeight()) toAddOrRemove--
        }
        while (toAddOrRemove < 0) {
            if (removeRandomWeight()) toAddOrRemove++
        }
    }

    fun weightExists(sourceNeuron: Neuron, destNeuron: Neuron) : Boolean {
        for (w in weights) {
            if (w.isSameEdge(sourceNeuron, destNeuron)) return true
        }
        return false
    }

    fun makeGenes() : NNGenes {
        val genes = NNGenes()
        hiddenNeurons.forEach {
            genes.neuronGenes += NeuronGene(it.neuronType, it.bias)
        }
        outputNeurons.forEach {
            genes.outputBiasGenes += it.bias
        }
        val allN = getAllNeurons()
        weights.forEach {
            val srcIndex = allN.indexOf(it.sourceNeuron)
            val destIndex = allN.indexOf(it.destNeuron)
            genes.weightGenes += WeightGene(srcIndex, destIndex, it.weight)
        }
        return genes
    }

    internal fun getAllNeurons() : List<Neuron> = inputNeurons + hiddenNeurons + outputNeurons
}