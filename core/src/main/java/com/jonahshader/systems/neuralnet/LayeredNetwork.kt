package com.jonahshader.systems.neuralnet

import com.jonahshader.systems.neuralnet.washboard.WashboardLayer
import org.jetbrains.kotlinx.multik.ndarray.data.D1
import org.jetbrains.kotlinx.multik.ndarray.data.NDArray

class LayeredNetwork {
    private val layers = mutableListOf<Layer>()

    operator fun plusAssign(layer: Layer) {
        if (layer is WashboardLayer) {
            if (layers.last() is WashboardLayer) {
                layer.pLayer = layers.last() as WashboardLayer
            }
        }
    }

    fun update(input: NDArray<Float, D1>, dt: Float) : NDArray<Float, D1> {
        var output = layers[0].update(input, dt)
        (1 until layers.size).forEach {
            output = layers[it].update(output, dt)
        }

        return output
    }
}