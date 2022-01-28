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
//            val neuron = LeakyReLUNeuron()
//            val neuron = NeuronType.makeRandomHidden(rand)
            val neuron = LeakyIntegrateAndFireNeuron()
            neuron.mutateScalars(rand, 1.25f)
            hiddenNeurons += neuron
        }
        connect(networkParams.connectivityInit)

        prune()
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
            val newNeuron = n.makeNeuron()
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

    fun mutate(overallScale: Float) {
        var addRemoveNeuronCount = (rand.nextGaussian() * networkParams.addRemoveNeuronSd * overallScale).roundToInt()
        var addRemoveWeightCount = (rand.nextGaussian() * networkParams.addRemoveWeightSd * overallScale).roundToInt()

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
            it.mutate(rand, networkParams.mutateWeightSd * overallScale)
        }
        hiddenNeurons.forEach {
            it.mutateScalars(rand, networkParams.mutateWeightSd * overallScale) // bias is a weight basically
        }
        outputNeurons.forEach {
            it.mutateScalars(rand, networkParams.mutateWeightSd * overallScale)
        }
    }

    private fun addRandomWeight() : Boolean {
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

        while (toAddOrRemove > 0) {
            if (addRandomWeight()) toAddOrRemove--
        }
        while (toAddOrRemove < 0) {
            if (removeRandomWeight()) toAddOrRemove++
        }
    }

    fun prune() {
        val neurons = getAllNeurons()
        // build directed graph
        val edges = mutableListOf<Pair<Int, Int>>()
        for (w in weights) {
            edges += Pair(neurons.indexOf(w.sourceNeuron), neurons.indexOf(w.destNeuron))
        }
        // trace back from output neurons
        val connectedToOutput = BooleanArray(neurons.size) { neurons[it].neuronCategory == Neuron.NeuronCategory.OUTPUT }
        val connectedToInput = BooleanArray(neurons.size) { neurons[it].neuronCategory == Neuron.NeuronCategory.INPUT }
        for (j in neurons.indices) {
            for (i in edges.indices) {
                if (connectedToOutput[edges[i].second]) {
                    connectedToOutput[edges[i].first] = true
                }
                if (connectedToInput[edges[i].first]) {
                    connectedToInput[edges[i].second] = true
                }
            }
        }


        val toRemove = mutableListOf<Neuron>()
        connectedToOutput.indices
            .asSequence()
            .filterNot { connectedToInput[it] && connectedToOutput[it] }
            .forEach { toRemove += neurons[it] }
        hiddenNeurons -= toRemove.toSet()
        toRemove.forEach { n ->
            weights.removeIf {
                it.isConnectedToNeuron(n)
            }
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
            genes.neuronGenes += it.makeGenetics()
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

    fun resizeOutputs(outputs: Int) {
        var addRemove = outputs - outputNeurons.size

        if (addRemove < 0) {
            while (addRemove < 0) {
                removeOutputNeuronNoPrune()
                addRemove++
            }
//            prune()
        }


        while (addRemove > 0) {
            outputNeurons += OutputNeuron()
            addRemove--
        }
    }

    fun removeOutputNeuronNoPrune() : Boolean {
        return if (outputNeurons.isNotEmpty()) {
            val toRemove = outputNeurons.last()
            weights.removeIf { it.isConnectedToNeuron(toRemove) }
            outputNeurons.remove(toRemove)
            true
        } else {
            false
        }
    }
}