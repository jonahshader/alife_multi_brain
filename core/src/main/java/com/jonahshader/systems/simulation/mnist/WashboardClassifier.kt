package com.jonahshader.systems.simulation.mnist

import com.badlogic.gdx.graphics.Camera
import com.jonahshader.systems.creatureparts.ReinforcementTask
import com.jonahshader.systems.neuralnet.Network
import com.jonahshader.systems.simulation.mnist.MnistData.IMAGE_WIDTH_HEIGHT

class WashboardClassifier(networkBuilder: (Int, Int) -> Network) : ReinforcementTask {
    override val network: Network

    init {
        network = networkBuilder(IMAGE_WIDTH_HEIGHT * IMAGE_WIDTH_HEIGHT, 10)
    }
    override fun cloneAndReset(): ReinforcementTask {
        TODO("Not yet implemented")
    }

    override fun restartAndRandomize() {
        TODO("Not yet implemented")
    }

    override fun render() {
        TODO("Not yet implemented")
    }

    override fun update(dt: Float) {
        TODO("Not yet implemented")
    }

    override fun getFitness(): Float {
        TODO("Not yet implemented")
    }

    override fun spectate(cam: Camera) {
        TODO("Not yet implemented")
    }
}