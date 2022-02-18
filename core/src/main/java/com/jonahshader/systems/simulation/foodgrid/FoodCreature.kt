package com.jonahshader.systems.simulation.foodgrid

import com.badlogic.gdx.math.Vector2
import com.jonahshader.MultiBrain
import com.jonahshader.systems.creatureparts.Creature
import com.jonahshader.systems.creatureparts.CreatureBuilder
import com.jonahshader.systems.neuralnet.Network
import com.jonahshader.systems.simulation.Environment
import com.jonahshader.systems.simulation.foodgrid.FoodGrid.Companion.CELL_SIZE
import ktx.math.plusAssign
import kotlin.math.pow

class FoodCreature(networkBuilder: (Int, Int) -> Network) : Creature {
    companion object {
        private const val FOOD_SENSOR_GRID_WIDTH = 5
        private const val FOOD_SENSOR_GRID_HEIGHT = 5
        private const val EAT_PER_SECOND = .25f

        private const val GRAPHIC_SENSOR_RADIUS = 2f
        private const val GRAPHIC_BODY_RADIUS = 3f

        val builder: CreatureBuilder = { FoodCreature(it) }

    }

    private val foodSensorPos = mutableListOf<Vector2>()
    var totalFood = 0f

    override val pos = Vector2()
    private val vel = Vector2()
    private val tempSensor = Vector2()

    override val network = networkBuilder(FOOD_SENSOR_GRID_WIDTH * FOOD_SENSOR_GRID_HEIGHT, 4)
    private val foodGrid = FoodGrid()
    override val environment = foodGrid

    init {
        for (y in 0 until FOOD_SENSOR_GRID_HEIGHT) for (x in 0 until FOOD_SENSOR_GRID_WIDTH) {
            val xPos = (x - FOOD_SENSOR_GRID_WIDTH/2) * CELL_SIZE * .5f
            val yPos = (y - FOOD_SENSOR_GRID_HEIGHT/2) * CELL_SIZE * .5f

            foodSensorPos += Vector2(xPos, yPos)
        }
    }

    override fun update(dt: Float) {
        foodSensorPos.forEachIndexed { index, it ->
            tempSensor.set(pos).add(it)
            network.setInput(index, foodGrid.readFoodBilinear(tempSensor))
        }

        network.update(dt)

        vel.x = network.getOutput(0)
        vel.y = network.getOutput(1)
        val targetSpeed = network.getOutput(2)
        vel.setLength(targetSpeed)
        vel.scl(dt)

        // move or eat
        if (network.getOutput(3) < 0) {
            pos += vel
            totalFood -= .5f * ((targetSpeed / CELL_SIZE).pow(2) * EAT_PER_SECOND) * dt
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

    override fun getFitness(): Float {
        return totalFood
    }

    override fun render() {
        MultiBrain.shapeDrawer.setColor(.5f, .5f, .5f, 1f)
        foodSensorPos.forEach {
            tempSensor.set(pos).add(it)
            val brightness = foodGrid.readFoodBilinear(tempSensor)
            MultiBrain.shapeDrawer.setColor(brightness, brightness, brightness, 1f)
            MultiBrain.shapeDrawer.circle(tempSensor.x, tempSensor.y, GRAPHIC_SENSOR_RADIUS)
        }

//        for (i in 0 until 512) {
//            tempSensor.set(pos).add(Rand.randx.nextGaussian().toFloat() * 32, Rand.randx.nextGaussian().toFloat() * 32)
//            val brightness = foodGrid.readFoodBilinear(tempSensor)
//            MultiBrain.shapeDrawer.setColor(brightness, brightness, brightness, 1f)
//            MultiBrain.shapeDrawer.filledCircle(tempSensor, 2f)
//        }
        MultiBrain.shapeDrawer.setColor(1f, 1f, 1f, 1f)
        MultiBrain.shapeDrawer.filledCircle(pos, GRAPHIC_BODY_RADIUS)
    }

    override fun reset() {
        pos.setZero()
        vel.setZero()
        tempSensor.setZero()
        network.reset()
        totalFood = 0f
    }

    override fun cloneAndReset() : Creature {
        val newCreature = FoodCreature { _, _ -> network.clone() }
        newCreature.reset()
        return newCreature
    }
}