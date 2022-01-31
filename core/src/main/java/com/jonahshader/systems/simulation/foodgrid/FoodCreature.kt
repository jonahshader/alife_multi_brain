package com.jonahshader.systems.simulation.foodgrid

import com.badlogic.gdx.math.Vector2
import com.jonahshader.MultiBrain
import com.jonahshader.systems.brain.Network
import com.jonahshader.systems.brain.densecyclic.DenseCyclicNetwork
import com.jonahshader.systems.simulation.foodgrid.FoodGrid.Companion.CELL_SIZE
import ktx.math.plusAssign

class FoodCreature(networkBuilder: (Int, Int) -> Network = {input, output -> DenseCyclicNetwork(input, 32, output) }) {
    companion object {
        private const val FOOD_SENSOR_GRID_WIDTH = 5
        private const val FOOD_SENSOR_GRID_HEIGHT = 5
        private const val EAT_PER_SECOND = .25f

        private const val GRAPHIC_SENSOR_RADIUS = 2f
        private const val GRAPHIC_BODY_RADIUS = 3f
    }

    private val foodSensorPos = mutableListOf<Vector2>()
    var totalFood = 0f

    private val pos = Vector2()
    private val vel = Vector2()
    private val tempSensor = Vector2()

    val network = networkBuilder(FOOD_SENSOR_GRID_WIDTH * FOOD_SENSOR_GRID_HEIGHT, 3)

    init {
        for (y in 0 until FOOD_SENSOR_GRID_HEIGHT) for (x in 0 until FOOD_SENSOR_GRID_WIDTH) {
            val xPos = (x - FOOD_SENSOR_GRID_WIDTH/2) * CELL_SIZE * .5f
            val yPos = (y - FOOD_SENSOR_GRID_HEIGHT/2) * CELL_SIZE * .5f

            foodSensorPos += Vector2(xPos, yPos)
        }
    }

    fun update(foodGrid: FoodGrid, dt: Float) {



        foodSensorPos.forEachIndexed { index, it ->
            tempSensor.set(pos).add(it)
            network.setInput(index, foodGrid.getFood(tempSensor))
        }

        network.update(dt)

        vel.x = network.getOutput(0)
        vel.y = network.getOutput(1)
        vel.scl(dt)

        // move or eat
        if (network.getOutput(2) < 0) {
            pos += vel
        } else {
            val toEat = EAT_PER_SECOND * dt
            val foodAtBody = foodGrid.getFood(pos)
            if (foodAtBody > toEat) {
                totalFood += toEat
                foodGrid.setFood(pos, foodAtBody - toEat)
            } else if (foodAtBody > 0) {
                totalFood += foodAtBody
                foodGrid.setFood(pos, 0f)
            }
        }



    }

    fun render() {
        MultiBrain.shapeDrawer.setColor(.5f, .5f, .5f, 1f)
        foodSensorPos.forEach {
            tempSensor.set(pos).add(it)
            MultiBrain.shapeDrawer.circle(tempSensor.x, tempSensor.y, GRAPHIC_SENSOR_RADIUS)
        }
        MultiBrain.shapeDrawer.setColor(1f, 1f, 1f, 1f)
        MultiBrain.shapeDrawer.filledCircle(pos.x, pos.y, GRAPHIC_BODY_RADIUS)
    }

    fun reset() {
        pos.setZero()
        vel.setZero()
        tempSensor.setZero()
        network.reset()
        totalFood = 0f
    }

    fun cloneAndReset() : FoodCreature {
        val newCreature = FoodCreature { _, _ -> network.clone() }
        newCreature.reset()
        return newCreature
    }
}