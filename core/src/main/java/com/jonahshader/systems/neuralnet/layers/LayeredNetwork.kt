package com.jonahshader.systems.neuralnet.layers

import org.jetbrains.kotlinx.multik.ndarray.data.D1
import org.jetbrains.kotlinx.multik.ndarray.data.NDArray
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.operations.append

class LayeredNetwork : Layer {
    private val layers: MutableList<Layer>
    private var paramCount = 0

    constructor() {
        layers = mutableListOf()
    }
    constructor(toCopy: LayeredNetwork) {
        layers = toCopy.layers.map { it.clone() }.toMutableList()
        paramCount = layers.sumOf { it.getParamCount() }
    }

    operator fun plusAssign(layer: Layer) {
        if (layer is WashboardLayer) {
            if (layers.isNotEmpty()) {
                if (layers.last() is WashboardLayer) {
                    layer.pLayer = layers.last() as WashboardLayer
                }
            }
        }
        layers += layer
        paramCount += layer.getParamCount()
    }

    override fun update(input: LayerIO, dt: Float): LayerIO {
        var output = layers[0].update(input, dt)
        (1 until layers.size).forEach {
            output = layers[it].update(output, dt)
        }
        return output
    }

    override fun mutateParameters(amount: Float) {
        layers.forEach {
            it.mutateParameters(amount)
        }
    }

    override fun getParameters(): NDArray<Float, D1> {
        return layers.map { it.getParameters() }.reduce {
            acc, ndArray -> acc.append(ndArray)
        }
    }

    override fun getParamCount(): Int {
        return paramCount
    }

    override fun setParameters(params: NDArray<Float, D1>) {
        var index = 0
        layers.forEach {
            it.setParameters(params[index..(index + it.getParamCount())] as NDArray<Float, D1>)
            index += it.getParamCount()
        }
    }

    override fun clone() = LayeredNetwork(this)

    override fun reset() {
        layers.forEach {
            it.reset()
        }
    }
}