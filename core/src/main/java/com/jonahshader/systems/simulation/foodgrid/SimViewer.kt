package com.jonahshader.systems.simulation.foodgrid

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.utils.Disposable
import com.jonahshader.systems.creatureparts.Creature
import com.jonahshader.systems.simulation.EvolutionStrategies

class SimViewer(private val sim: EvolutionStrategies) : Disposable {
    private var creature: Creature? = null
    private var timestep = 0
    private var disposed = false

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
        if (!disposed) {
            if (timestep > sim.steps) {
                timestep = 0
                creature?.environment?.resetAndRandomize()
                creature?.network?.dispose()
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

    override fun dispose() {
        creature?.network?.dispose()
        disposed = true
    }
}