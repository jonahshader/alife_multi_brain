package com.jonahshader.systems.simulation.foodgrid

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.utils.Disposable
import com.jonahshader.systems.creatureparts.ReinforcementTask
import com.jonahshader.systems.training.EvolutionStrategies

class SimViewer(private val sim: EvolutionStrategies) : Disposable {
    private var creature: ReinforcementTask? = null
    private var timestep = 0
    private var disposed = false

    fun render() {
        creature?.render()
    }

    fun follow(cam: Camera) {
        if (creature != null) {
            creature!!.spectate(cam)
        }
    }

    fun update() {
        if (!disposed) {
            if (creature?.done() == true) {
                timestep = 0
                creature?.restartAndRandomize()
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