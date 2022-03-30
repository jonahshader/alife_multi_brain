package com.jonahshader.systems.simulation

import com.jonahshader.systems.simulation.fluidsim.Particle
import com.jonahshader.systems.simulation.fluidsim.TileParticle

class FluidSimSorted(internal val worldSize: Int) {
    private val particles = mutableListOf<Particle>()
    val walls = mutableListOf<Boolean>()

    init {
        for (i in 0 until worldSize * worldSize) {
            walls += false
        }

        for (i in 0 until worldSize) {
            walls[i] = true
            walls[i + worldSize * (worldSize-1)] = true
            walls[i * worldSize] = true
            walls[i * worldSize + worldSize - 1] = true
        }
    }
}