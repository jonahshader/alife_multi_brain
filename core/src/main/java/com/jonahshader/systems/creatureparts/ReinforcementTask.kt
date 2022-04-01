package com.jonahshader.systems.creatureparts

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.utils.viewport.ScalingViewport
import com.jonahshader.systems.neuralnet.Network
import com.jonahshader.systems.neuralnet.NetworkBuilder

typealias TaskBuilder = (networkBuilder: NetworkBuilder) -> ReinforcementTask

interface ReinforcementTask {
    val network: Network
    fun cloneAndReset() : ReinforcementTask
    fun restartAndRandomize()
    fun render(viewport: ScalingViewport)
    fun update(dt: Float)
    fun getFitness() : Float
    fun spectate(cam: Camera)

    var maxIterations: Int
    var currentIteration: Int
    fun done() = currentIteration >= maxIterations
}