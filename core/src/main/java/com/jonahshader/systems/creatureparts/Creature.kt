package com.jonahshader.systems.creatureparts

import com.badlogic.gdx.math.Vector2
import com.jonahshader.systems.neuralnet.Network
import com.jonahshader.systems.neuralnet.NetworkBuilder
import com.jonahshader.systems.simulation.Environment

typealias CreatureBuilder = (networkBuilder: NetworkBuilder) -> Creature

interface Creature {
    val network: Network
    val pos: Vector2
    val environment: Environment
    fun cloneAndReset() : Creature
    fun reset()
    fun render()
    fun update(dt: Float)
    fun getFitness() : Float
}