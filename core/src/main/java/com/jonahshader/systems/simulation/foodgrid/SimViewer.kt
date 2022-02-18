package com.jonahshader.systems.simulation.foodgrid

import com.badlogic.gdx.graphics.Camera
import com.jonahshader.systems.creatureparts.Creature
import com.jonahshader.systems.simulation.EvolutionStrategies

class SimViewer(private val sim: EvolutionStrategies) {
    private var creature: Creature? = null
    private var timestep = 0

    fun render() {
        creature?.environment?.render()
        creature?.render()
    }

    fun follow(cam: Camera) {
        if (creature != null) {
            cam.position.x = creature!!.pos.x
            cam.position.y = creature!!.pos.y
        }
        cam.update()
    }

    fun update() {
        if (timestep > sim.steps) {
            timestep = 0
            creature?.environment?.resetAndRandomize()
            creature = sim.getBestCopy()
        }

        if (creature == null) {
            creature = sim.getBestCopy()
        } else {
            timestep++
        }
        creature?.update(sim.dt)
    }
}