package com.jonahshader.systems.brain.visualizer

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.jonahshader.systems.brain.Network
import com.jonahshader.systems.brain.neurons.Neuron
import com.jonahshader.systems.scenegraph.Node2D

class NetworkVisualizer(var network: Network) : Node2D() {
    private val neurons = mutableSetOf<NeuronGraphic>()
    private val weights = mutableListOf<WeightSpringGraphic>()
    private val sc = SpringConstants(10.0f, 2.0f)

    private val ioNeuronPadding = 96.0f
    private val ioNeuronHorizontalSpacing = 400.0f

    init {
        val connectedNeurons = HashMap<Neuron, NeuronGraphic>()

        network.inputNeurons.forEachIndexed { i, it ->
            val iMid = (i - network.inputNeurons.size / 2f) + .5f
            neurons += NeuronGraphic(it, Vector2(-ioNeuronHorizontalSpacing/2, ioNeuronPadding * iMid))
            connectedNeurons[it] = neurons.last()
        }
        network.outputNeurons.forEachIndexed { i, it ->
            val iMid = (i - network.outputNeurons.size / 2f) + .5f
            neurons += NeuronGraphic(it, Vector2(ioNeuronHorizontalSpacing/2, ioNeuronPadding * iMid))
            connectedNeurons[it] = neurons.last()
        }

        network.weights.forEach {
            it.weights.forEach { w ->
//                if (neurons.contains
                // need to make these reference the already existing input and output graphic neurons somehow
                val sourceNeuron = if (connectedNeurons.containsKey(w.sourceNeuron)) connectedNeurons[w.sourceNeuron]!! else
                    NeuronGraphic(w.sourceNeuron, Vector2(NeuronGraphic.DEFAULT_RADIUS * 8.0f + Math.random().toFloat() * 64, Math.random().toFloat() * 64))
                val destNeuron = if (connectedNeurons.containsKey(it.receivingNeuron)) connectedNeurons[it.receivingNeuron]!! else
                    NeuronGraphic(it.receivingNeuron, Vector2(NeuronGraphic.DEFAULT_RADIUS * 8.0f + Math.random().toFloat() * 64, Math.random().toFloat() * 64))

                connectedNeurons[w.sourceNeuron] = sourceNeuron
                connectedNeurons[it.receivingNeuron] = destNeuron
                neurons += sourceNeuron
                neurons += destNeuron
                weights += WeightSpringGraphic(sourceNeuron, destNeuron, w, sc = sc)
            }
        }

        // add as child of network visualizer
        neurons.forEach {
            addChild(it)
        }
    }

    override fun preUpdate(dt: Float) {
        weights.forEach {
            it.update(dt)
        }
    }

    override fun customRender(batch: Batch) {
        weights.forEach {
            it.render()
        }
    }
}