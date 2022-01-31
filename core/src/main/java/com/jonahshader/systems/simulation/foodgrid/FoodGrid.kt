package com.jonahshader.systems.simulation.foodgrid

import com.badlogic.gdx.math.Vector2
import com.jonahshader.MultiBrain
import com.jonahshader.systems.utils.Rand
import java.util.*
import kotlin.math.floor
import kotlin.math.sqrt

class FoodGrid(private val rand: Random = Rand.randx) {
    companion object {
        const val CELL_SIZE = 16f
        const val WORLD_RADIUS = 512 // in cells
    }

    private val food = HashMap<Int, Float>()

    private fun posToKey(pos: Vector2) = posToKey(worldToCell(pos.x), worldToCell(pos.y))
    private fun posToKey(xWorld: Int, yWorld: Int) : Int = (xWorld shl 16) or (yWorld and 0xFFFF)
    private fun keyToXPos(key: Int) : Int = key shr 16
    private fun keyToYPos(key: Int) : Int = key.toShort().toInt()
    private fun worldToCell(pos: Float) : Int = floor(pos/ CELL_SIZE).toInt() + WORLD_RADIUS
    private fun cellToWorld(world: Int) : Float = (world - WORLD_RADIUS) * CELL_SIZE
    private fun inRange(pos: Vector2) : Boolean = pos.len2() < WORLD_RADIUS * WORLD_RADIUS * CELL_SIZE * CELL_SIZE

    fun getFood(pos: Vector2) : Float {
        val key = posToKey(pos)

        return if (inRange(pos)) {
            if (food.containsKey(key)) {
                food[key]!!
            } else {
                val newFood = 1f - sqrt(rand.nextFloat())
                food[key] = newFood
                newFood
            }
        } else {
            0f
        }
    }

    fun setFood(pos: Vector2, newFood: Float) {
        if (inRange(pos)) {
            food[posToKey(pos)] = newFood
        }
    }

    fun render() {
        food.entries.forEach {
            val xWorld = cellToWorld(keyToXPos(it.key))
            val yWorld = cellToWorld(keyToYPos(it.key))
            val f = it.value
            MultiBrain.shapeDrawer.setColor(f, f, f, 1f)
            MultiBrain.shapeDrawer.filledRectangle(xWorld, yWorld, CELL_SIZE, CELL_SIZE)
        }
    }

    fun reset() {
        food.clear()
    }
}