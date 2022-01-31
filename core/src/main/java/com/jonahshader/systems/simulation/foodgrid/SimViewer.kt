package com.jonahshader.systems.simulation.foodgrid

class SimViewer(private val sim: FoodSim) {
    private val foodGrid = FoodGrid()
    private var creature: FoodCreature? = null
    private var timestep = 0

    fun render() {
        foodGrid.render()
        creature?.render()
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