package com.jonahshader.systems.neuralnet.visualizer

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.jonahshader.systems.neuralnet.cyclic.CyclicNetwork
import com.jonahshader.systems.neuralnet.neurons.Neuron
import com.jonahshader.systems.scenegraph.Node2D
import kotlin.math.absoluteValue
import kotlin.math.min
import kotlin.math.pow

class NetworkVisualizer(var network: CyclicNetwork) : Node2D() {
    private val neurons = mutableListOf<NeuronGraphic>()
    private val mobileNeuronsSorted = mutableListOf<NeuronGraphic>()
    private val weights = mutableListOf<WeightSpringGraphic>()
    private val sc = SpringConstants(6.0f, 0.0f)

    companion object {
        const val ioNeuronPadding = 120.0f
        const val ioNeuronHorizontalSpacing = 800.0f
        const val PUSH_RADIUS = 110f
        const val PUSH_EXPONENT = 2f
        const val PUSH_FORCE = 16000f
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
            var forceScalar = min(network.inputNeurons.size, network.outputNeurons.size).toFloat()
            if (srcNeuronGraphic.neuron.neuronCategory == Neuron.NeuronCategory.INPUT) {
                forceScalar /= network.inputNeurons.size.coerceAtLeast(1)
            }
            if (destNeuronGraphic.neuron.neuronCategory == Neuron.NeuronCategory.OUTPUT) {
                forceScalar /= network.outputNeurons.size.coerceAtLeast(1)
            }
            weights += WeightSpringGraphic(srcNeuronGraphic, destNeuronGraphic, it, sc = sc, forceScalar = forceScalar)
        }

        // add as child of network visualizer
        neurons.forEach {
            addChild(it)
        }
    }

    fun incrementSpringLength(increment: Float) {
        weights.forEach {
            it.targetLength += increment
        }
    }

    override fun preUpdate(dt: Float) {
        netUpdate(dt)
    }

    private fun netUpdate(dt: Float) {
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

    override fun customRender(batch: Batch, cam: Camera) {
        weights.forEach {
            it.render(cam)
        }
    }
}