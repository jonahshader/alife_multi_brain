package com.jonahshader.systems.brain.visualizer

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.jonahshader.systems.brain.Network
import com.jonahshader.systems.scenegraph.Node2D

class NetworkVisualizer(var network: Network) : Node2D() {
    private val neurons = mutableListOf<NeuronGraphic>()
    private val weights = mutableListOf<WeightSpringGraphic>()
    private val sc = SpringConstants(10.0f, 2.0f)

    private val ioNeuronPadding = 96.0f
    private val ioNeuronHorizontalSpacing = 400.0f

    init {
        network.inputNeurons.forEachIndexed { i, it ->
            val iMid = (i - network.inputNeurons.size / 2f) + .5f
            neurons += NeuronGraphic(it, Vector2(-ioNeuronHorizontalSpacing/2, ioNeuronPadding * iMid))
        }
        network.hiddenNeurons.forEach {
            neurons += NeuronGraphic(it, network.rand)
        }
        network.outputNeurons.forEachIndexed { i, it ->
            val iMid = (i - network.outputNeurons.size / 2f) + .5f
            neurons += NeuronGraphic(it, Vector2(ioNeuronHorizontalSpacing/2, ioNeuronPadding * iMid))
        }

        val allN = network.getAllNeurons()
        network.weights.forEach {
            val srcNeuronGraphic = neurons[allN.indexOf(it.sourceNeuron)]
            val destNeuronGraphic = neurons[allN.indexOf(it.destNeuron)]
            weights += WeightSpringGraphic(srcNeuronGraphic, destNeuronGraphic, it, sc = sc)
        }

        // TODO: prune?

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