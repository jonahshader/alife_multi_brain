package com.jonahshader.systems.simulation.foodgrid

import com.badlogic.gdx.graphics.Camera
import com.jonahshader.systems.ui.Window

class SimViewer(private val sim: FoodSim) {
    private val foodGrid = FoodGrid()
    private var creature: FoodCreature? = null
    private var timestep = 0

    fun render() {
        foodGrid.render()
        creature?.render(foodGrid)
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
            foodGrid.reset()
            creature = sim.getBestCopy()
        }

        if (creature == null) {
            creature = sim.getBestCopy()
        } else {
            timestep++
        }
        creature?.update(foodGrid, sim.dt)
    }
}