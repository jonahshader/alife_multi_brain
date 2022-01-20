package com.jonahshader.systems.brain.visualizer

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.jonahshader.systems.brain.Network
import com.jonahshader.systems.scenegraph.Node2D
import kotlin.math.absoluteValue
import kotlin.math.pow

class NetworkVisualizer(var network: Network) : Node2D() {
    private val neurons = mutableListOf<NeuronGraphic>()
    private val mobileNeuronsSorted = mutableListOf<NeuronGraphic>()
    private val weights = mutableListOf<WeightSpringGraphic>()
    private val sc = SpringConstants(1.0f, 1.0f)

    companion object {
        const val ioNeuronPadding = 200.0f
        const val ioNeuronHorizontalSpacing = 800.0f
        const val PUSH_RADIUS = 100f
        const val PUSH_EXPONENT = 1.5f
        const val PUSH_FORCE = 80f
    }

    init {
        network.inputNeurons.forEachIndexed { i, it ->
            val iMid = (i - network.inputNeurons.size / 2f) + .5f
            neurons += NeuronGraphic(it, Vector2(-ioNeuronHorizontalSpacing/2, ioNeuronPadding * iMid))
        }
        network.hiddenNeurons.forEach {
            val n = NeuronGraphic(it, network.rand)
            neurons += n
            mobileNeuronsSorted += n
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

        // add as child of network visualizer
        neurons.forEach {
            addChild(it)
        }
    }

    override fun preUpdate(dt: Float) {
        update(dt)
    }

    private fun update(dt: Float) {
        val dist = Vector2()
        mobileNeuronsSorted.sortBy { it.localPosition.x }
        mobileNeuronsSorted.forEachIndexed { index, it ->
            var i = index - 1
            val xPos = it.localPosition.x
            while (i in mobileNeuronsSorted.indices && (mobileNeuronsSorted[i].localPosition.x - xPos).absoluteValue <= PUSH_RADIUS) {
                val ng = mobileNeuronsSorted[i]
                pushNeurons(dist, it, ng)
                i--
            }
            i = index + 1
            while (i in mobileNeuronsSorted.indices && (mobileNeuronsSorted[i].localPosition.x - xPos).absoluteValue <= PUSH_RADIUS) {
                val ng = mobileNeuronsSorted[i]
                pushNeurons(dist, it, ng)
                i++
            }
        }


        weights.forEach {
            it.update(dt)
        }
    }

    private fun pushNeurons(dist: Vector2, it: NeuronGraphic, ng: NeuronGraphic) {
        dist.set(it.localPosition).sub(ng.localPosition)
        val len = dist.len()

        if (len <= PUSH_RADIUS) {
            val force = ((PUSH_RADIUS - len) / PUSH_RADIUS).pow(PUSH_EXPONENT) * PUSH_FORCE
            dist.nor().scl(force)
            ng.force.sub(dist)
        }
    }

    override fun customRender(batch: Batch) {
        weights.forEach {
            it.render()
        }
    }
}