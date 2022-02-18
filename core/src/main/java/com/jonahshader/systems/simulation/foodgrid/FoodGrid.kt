package com.jonahshader.systems.simulation.foodgrid

import com.badlogic.gdx.math.Vector2
import com.jonahshader.MultiBrain
import com.jonahshader.systems.simulation.Environment
import com.jonahshader.systems.utils.Rand
import java.util.*
import kotlin.math.floor

class FoodGrid(private val rand: Random = Rand.randx) : Environment {
    companion object {
        const val CELL_SIZE = 16f
        const val WORLD_RADIUS = 512 // in cells
    }

//    private val noise = FastNoiseLite(seed)
//    init {
//        noise.SetNoiseType(FastNoiseLite.NoiseType.Value)
//        noise.SetFrequency(1f)
//    }


    private val food = HashMap<Int, Float>()

    private fun posToKey(pos: Vector2) = posToKey(worldToCell(pos.x), worldToCell(pos.y))
    private fun posToKey(xCell: Int, yCell: Int) : Int = (xCell shl 16) or (yCell and 0xFFFF)
    private fun keyToXPos(key: Int) : Int = key shr 16
    private fun keyToYPos(key: Int) : Int = key.toShort().toInt()
    private fun worldToCell(pos: Float) : Int = floor(pos/ CELL_SIZE).toInt() + WORLD_RADIUS
    private fun cellToWorld(world: Int) : Float = (world - WORLD_RADIUS) * CELL_SIZE
    private fun inRange(pos: Vector2) : Boolean = pos.len2() < WORLD_RADIUS * WORLD_RADIUS * CELL_SIZE * CELL_SIZE

    fun getFood(pos: Vector2) : Float {
        val xCell = worldToCell(pos.x)
        val yCell = worldToCell(pos.y)

        return if (inRange(pos)) {
            getFood(xCell, yCell)
        } else {
            0f
        }
    }

    private fun getFood(xCell: Int, yCell: Int) : Float {
        val key = posToKey(xCell, yCell)
        return if (food.containsKey(key)) {
            food[key]!!
        } else {
//                val newFood = 1f - sqrt(rand.nextFloat())
            val newFood = if (rand.nextFloat() > .75) 1f else 0f
//            val newFood = if (noise.GetNoise(xCell.toFloat(), yCell.toFloat()) > .33) 1f else 0f
//            val newFood = noise.GetNoise(xCell.toFloat(), yCell.toFloat()) * .5f + .5f
            food[key] = newFood
            newFood
        }
    }

    fun readFoodBilinear(pos: Vector2) : Float {
        return if (inRange(pos)) {
            val xCellF = ((pos.x + CELL_SIZE/2f) / CELL_SIZE) + WORLD_RADIUS
            val yCellF = ((pos.y + CELL_SIZE/2f) / CELL_SIZE) + WORLD_RADIUS
            val xCellI = worldToCell(pos.x + CELL_SIZE/2f)
            val yCellI = worldToCell(pos.y + CELL_SIZE/2f)
            val xWorldFR = xCellF - xCellI
            val yWorldFR = yCellF - yCellI

            val topLeftTopRight = lerp(xWorldFR, getFood(xCellI - 1, yCellI), getFood(xCellI, yCellI))
            val bottomLeftBottomRight = lerp(xWorldFR, getFood(xCellI - 1, yCellI - 1), getFood(xCellI, yCellI - 1))

            val topBottom = lerp(yWorldFR, bottomLeftBottomRight, topLeftTopRight)
            topBottom
        } else {
            0f
        }
    }

    private fun lerp(p: Float, min: Float, max: Float) = (1-p) * min + p * max

    fun setFood(pos: Vector2, newFood: Float) {
        if (inRange(pos)) {
            food[posToKey(pos)] = newFood
        }
    }

    override fun render() {
        food.entries.forEach {
            val xWorld = cellToWorld(keyToXPos(it.key))
            val yWorld = cellToWorld(keyToYPos(it.key))
            val f = it.value
            MultiBrain.shapeDrawer.setColor(f, f, f, 1f)
            MultiBrain.shapeDrawer.filledRectangle(xWorld, yWorld, CELL_SIZE, CELL_SIZE)
        }
    }

    override fun resetAndRandomize() {
        food.clear()
    }

//    fun setSeed(seed: Int) {
//        noise.SetSeed(seed)
//    }

}