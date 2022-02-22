package com.jonahshader.systems.neuralnet.washboard

import com.jonahshader.systems.neuralnet.Network

class WashboardCyclic : Network {
    override val multithreadable = true
    override fun setInput(index: Int, value: Float) {
        TODO("Not yet implemented")
    }

    override fun getOutput(index: Int): Float {
        TODO("Not yet implemented")
    }

    override fun getInputSize(): Int {
        TODO("Not yet implemented")
    }

    override fun getOutputSize(): Int {
        TODO("Not yet implemented")
    }

    override fun mutateParameters(amount: Float) {
        TODO("Not yet implemented")
    }

    override fun getParameters(): List<Float> {
        TODO("Not yet implemented")
    }

    override fun setParameters(params: List<Float>) {
        TODO("Not yet implemented")
    }

    override fun update(dt: Float) {
        TODO("Not yet implemented")
    }

    override fun clone(): Network {
        TODO("Not yet implemented")
    }

    override fun reset() {
        TODO("Not yet implemented")
    }

    override fun dispose() {
        TODO("Not yet implemented")
    }
}