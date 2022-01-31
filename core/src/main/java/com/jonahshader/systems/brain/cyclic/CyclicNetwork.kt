package com.jonahshader.systems.brain.cyclic

import com.jonahshader.systems.brain.Network
import com.jonahshader.systems.brain.neurons.*
import com.jonahshader.systems.ga.NNGenes
import com.jonahshader.systems.ga.WeightGene
import com.jonahshader.systems.utils.Rand
import java.util.*
import kotlin.math.roundToInt
import kotlin.random.asKotlinRandom

class CyclicNetwork : Network {
    var networkParams: CyclicNetworkParams = CyclicNetworkParams()
    internal val rand: Random
    private val kRand: kotlin.random.Random
    val inputNeurons = mutableListOf<InputNeuron>()
    val outputNeurons = mutableListOf<OutputNeuron>()
    val hiddenNeurons = mutableListOf<Neuron>()
    val weights = mutableListOf<Weight>()
    val sourceNeurons: List<Neuron> get() = inputNeurons + hiddenNeurons
    val destNeurons: List<Neuron> get() = hiddenNeurons + outputNeurons

    constructor(inputs: Int, outputs: Int, networkParams: CyclicNetworkParams, rand: Random = Rand.randx) {
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

    constructor(otherNetwork: CyclicNetwork) : this(otherNetwork.makeGenes(), otherNetwork.inputNeurons.size, otherNetwork.outputNeurons.size)

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

    override fun update(dt: Float) {
        weights.forEach { it.forwardProp() }

        inputNeurons.forEach { it.update(dt) }
        hiddenNeurons.forEach { it.update(dt) }
        outputNeurons.forEach { it.update(dt) }

        inputNeurons.forEach { it.updateOutput() }
        hiddenNeurons.forEach { it.updateOutput() }
        outputNeurons.forEach { it.updateOutput() }
    }

    override fun clone() = CyclicNetwork(this)
    override fun reset() {
        inputNeurons.forEach {
            it.value = 0f
        }
        destNeurons.forEach {
            it.resetState()
        }
    }

    private fun removeRandomNeuron() : Boolean =
        if (hiddenNeurons.isNotEmpty()) {
            val toRemove = hiddenNeurons.random(kRand)
            weights.removeIf { it.isConnectedToNeuron(toRemove) }
            hiddenNeurons.remove(toRemove)
            true
        } else { false }


    override fun setInput(index: Int, value: Float) {
        inputNeurons[index].value = value
    }

    override fun getOutput(index: Int) = outputNeurons[index].out
    override fun getInputSize(): Int {
        return inputNeurons.size
    }

    override fun getOutputSize(): Int {
        return outputNeurons.size
    }

    private fun generateRandomHiddenNeuron() : Neuron {
        val n = NeuronType.makeRandomHidden(rand)
        n.bias = networkParams.weightInitSd * rand.nextGaussian().toFloat()
        return n
    }

    override fun mutateParameters(amount: Float) {
        weights.forEach {
            it.mutate(rand, networkParams.mutateWeightSd * amount)
        }
        hiddenNeurons.forEach {
            it.mutateScalars(rand, networkParams.mutateWeightSd * amount) // bias is a weight basically
        }
        outputNeurons.forEach {
            it.mutateScalars(rand, networkParams.mutateWeightSd * amount)
        }
    }

    override fun getParameters(): List<Float> = getAllNeurons().flatMap { neuron -> neuron.getParameters() } + weights.map { weight -> weight.weight }

    override fun setParameters(params: List<Float>) {
        var index = 0
        for(n in getAllNeurons()) {
            val size = n.getParameters().size
            n.setParameters(params.subList(index, index + size))
            index += size
        }

        for (w in weights) {
            w.weight = params[index]
            index++
        }
    }

    fun mutateTopology(amount: Float) {
        var addRemoveNeuronCount = (rand.nextGaussian() * networkParams.addRemoveNeuronSd * amount).roundToInt()
        addRemoveNeuronCount = addRemoveNeuronCount.coerceAtLeast(-hiddenNeurons.size)

        while (addRemoveNeuronCount < 0) {
            removeRandomNeuron()
            addRemoveNeuronCount++
        }

        while (addRemoveNeuronCount > 0) {
            hiddenNeurons += generateRandomHiddenNeuron()
            addRemoveNeuronCount--
        }

        var addRemoveWeightCount = (rand.nextGaussian() * networkParams.addRemoveWeightSd * amount).roundToInt()
        addRemoveWeightCount = addRemoveWeightCount.coerceAtLeast(-weights.size)

        while (addRemoveWeightCount < 0) {
            removeRandomWeight()
            addRemoveWeightCount++
        }

        while (addRemoveWeightCount > 0) {
            if (addRandomWeight()) addRemoveWeightCount--
        }
    }

    private fun addRandomWeight() = addRandomWeight(sourceNeurons, destNeurons)

    private fun addRandomWeight(sourceList: List<Neuron>, destList: List<Neuron>) : Boolean {
        // pick two neurons at random
        val sourceNeuron = sourceList.random(kRand)
        val destNeuron = destList.random(kRand)

        return if (!weightExists(sourceNeuron, destNeuron)) {
            weights += Weight(sourceNeuron, destNeuron, networkParams.weightInitSd * rand.nextGaussian().toFloat())
            true
        } else {
            false
        }
    }

    private fun removeRandomWeight() : Boolean {
        return if (weights.isNotEmpty()) {
            weights.removeAt(rand.nextInt(weights.size))
            true
        } else {
            false
        }
    }

    private fun connect(connectivity: Float) {
        var toAddOrRemove = ((sourceNeurons.size * destNeurons.size * connectivity) - weights.size).roundToInt()

        while (toAddOrRemove > 0) {
            if (addRandomWeight()) toAddOrRemove--
        }
        while (toAddOrRemove < 0) {
            if (removeRandomWeight()) toAddOrRemove++
        }
    }

    private fun prune() {
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

    private fun weightExists(sourceNeuron: Neuron, destNeuron: Neuron) : Boolean {
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

    fun connectNewOutputs(connectivity: Float) {
        val sourceList = sourceNeurons
        val destList = outputNeurons
        var toAddOrRemove = ((sourceList.size * destList.size * connectivity) - weights.size).roundToInt()

        while (toAddOrRemove > 0) {
            if (addRandomWeight(sourceList, destList)) toAddOrRemove--
        }
//        while (toAddOrRemove < 0) {
//            if (removeRandomWeight()) toAddOrRemove++
//        }
    }

    private fun removeOutputNeuronNoPrune() : Boolean {
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