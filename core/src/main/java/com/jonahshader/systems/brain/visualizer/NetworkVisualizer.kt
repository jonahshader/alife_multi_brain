package com.jonahshader.systems.brain.visualizer

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.jonahshader.systems.brain.Network
import com.jonahshader.systems.brain.neurons.Neuron
import com.jonahshader.systems.scenegraph.Node2D

class NetworkVisualizer(var network: Network) : Node2D() {
    private val neurons = mutableListOf<NeuronGraphic>()
    private val weights = mutableListOf<WeightSpringGraphic>()
    private val sc = SpringConstants(1.0f, 0.5f)

    init {
        network.inputNeurons.forEachIndexed { i, it ->
            neurons += NeuronGraphic(it, Vector2(0.0f, NeuronGraphic.DEFAULT_RADIUS * i), true)
        }
        network.outputNeurons.forEachIndexed { i, it ->
            neurons += NeuronGraphic(it, Vector2(NeuronGraphic.DEFAULT_RADIUS * 6.0f, NeuronGraphic.DEFAULT_RADIUS * i), true)
        }
        network.weights.forEach {
            it.weights.forEach { w ->
//                if (neurons.contains
                // need to make these reference the already existing input and output graphic neurons somehow
                val sourceNeuron = NeuronGraphic(w.sourceNeuron, Vector2(NeuronGraphic.DEFAULT_RADIUS * 4.0f + Math.random().toFloat() * 8, Math.random().toFloat() * 8), !it.receivingNeuron.removable)
                val destNeuron = NeuronGraphic(it.receivingNeuron, Vector2(NeuronGraphic.DEFAULT_RADIUS * 4.0f + Math.random().toFloat() * 8, Math.random().toFloat() * 8), !it.receivingNeuron.removable)
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
    override fun customUpdate(dt: Float) {
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