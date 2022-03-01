package com.jonahshader.systems.simulation.selectmove

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.jonahshader.MultiBrain
import com.jonahshader.systems.creatureparts.CreatureBuilder
import com.jonahshader.systems.creatureparts.ReinforcementTask
import com.jonahshader.systems.neuralnet.Network
import com.jonahshader.systems.utils.Rand
import java.util.*
import kotlin.math.absoluteValue

class SelectMove(private val worldSize: Int, private val numBalls: Int, private val rand: Random = Rand.randx, networkBuilder: (Int, Int) -> Network) : ReinforcementTask {
    override val network = networkBuilder(worldSize * worldSize * 3, 2)

    private val world = Array(worldSize * worldSize) { CellEntity.NOTHING }
    private var xCursor = worldSize/2
    private var yCursor = worldSize/2
    private var fitness = 0

    companion object {
        private const val CELL_SIZE = 32f
        fun makeBuilder(worldSize: Int, numBalls: Int): CreatureBuilder = {
            SelectMove(worldSize, numBalls, networkBuilder = it)
        }
        val defaultBuilder: CreatureBuilder = { SelectMove(5, 4, networkBuilder = it) }
    }

    enum class CellEntity {
        NOTHING {
            override fun getColor() = nothingColor
        },
        HOLE {
            override fun getColor() = holeColor
        },
        BALL {
            override fun getColor() = ballColor
        },
        CURSOR {
            override fun getColor() = cursorColor
        };
        companion object {
            private val nothingColor = Color.BLACK
            private val ballColor = Color(0f, 1f, 1f, 1f)
            private val holeColor = Color(0f, 0f, 1f, 1f)
            private val cursorColor = Color(1f, 1f, 1f, 1f)
        }
        abstract fun getColor() : Color
    }

    init {
        initEnv()
    }

    override fun cloneAndReset(): ReinforcementTask {
        val newTask = SelectMove(worldSize, numBalls, rand = rand) { _, _ -> network.clone() }
        newTask.network.reset()
        return newTask
    }

    override fun restartAndRandomize() {
        // reset things back to initial values.
        network.reset()
        fitness = 0
        for (i in world.indices)
            world[i] = CellEntity.NOTHING
        // re init environment randomly
        initEnv()
    }

    // sets the position of balls, holes, and cursor
    // does NOT clear world (should be cleared manually before calling this)
    private fun initEnv() {
        for (i in 1..numBalls) {
            // spawn hole
            var spawnPos = rand.nextInt(world.size)
            while (world[spawnPos] != CellEntity.NOTHING) { // keep looking for a free spot
                spawnPos = rand.nextInt(world.size)
            }
            world[spawnPos] = CellEntity.HOLE

            // spawn ball
            while (world[spawnPos] != CellEntity.NOTHING) { // keep looking for a free spot
                spawnPos = rand.nextInt(world.size)
            }
            world[spawnPos] = CellEntity.BALL
        }
        // spawn cursor
        var spawnPos = rand.nextInt(world.size)
        while (world[spawnPos] != CellEntity.NOTHING) {
            spawnPos = rand.nextInt(world.size)
        }
        xCursor = spawnPos % worldSize
        yCursor = spawnPos / worldSize
    }

    override fun render() {
        // render world
        for (y in 0 until worldSize) for (x in 0 until worldSize) {
            val color = posToCellEntity(x, y).getColor()
            MultiBrain.shapeDrawer.filledRectangle(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE, color)
        }
    }

    override fun update(dt: Float) {
        val worldCells = worldSize * worldSize
        val inputSize = worldCells * 3

        // reset network inputs
        for (i in 0 until inputSize) {
            network.setInput(i, 0f)
        }

        // set network inputs
        for (i in 0 until worldCells) {
            when (posToCellEntity(i)) {
                CellEntity.HOLE -> network.setInput(i, 1f)
                CellEntity.BALL -> network.setInput(i + worldCells, 1f)
                CellEntity.CURSOR -> network.setInput(i + worldCells * 2, 1f)
                else -> {}
            }
        }

        // get outputs
        network.update(dt)
        val xAmount = network.getOutput(0)
        val yAmount = network.getOutput(1)

        val xMove: Int
        val yMove: Int
        if (xAmount.absoluteValue > yAmount.absoluteValue) {
            xMove = if (xAmount > 0) 1 else -1
            yMove = 0
        } else {
            xMove = 0
            yMove = if (yAmount > 0) 1 else -1
        }

        fitness += if (tryMove(xMove, yMove)) 1 else 0
//        for (y in 0 until worldSize) for (x in 0 until worldSize) {
//            if (world[x + y * worldSize] == CellEntity.BALL) {
//                network.setInput(x + y *)
//            }
//        }

    }

    override fun getFitness() = fitness.toFloat()

    override fun spectate(cam: Camera) {
        cam.position.x = xCursor * CELL_SIZE + CELL_SIZE/2
        cam.position.y = yCursor * CELL_SIZE + CELL_SIZE/2
    }

    private fun posToCellEntity(x: Int, y: Int) =
        if (x == xCursor && y == yCursor) {
            CellEntity.CURSOR
        } else { world[x + y * worldSize] }

    private fun posToCellEntity(index: Int) : CellEntity {
        val x = index % worldSize
        val y = index / worldSize
        return if (x == xCursor && y == yCursor) {
            CellEntity.CURSOR
        } else { world[index] }
    }

    /**
     * returns true if that move lead to a ball going into a hole
     */
    private fun tryMove(xMove: Int, yMove: Int) : Boolean {
        val xNew = xCursor + xMove
        val yNew = yCursor + yMove

        val inBounds = 0 until worldSize

        var moveSuccess = false
        val success = if (xNew in inBounds && yNew in inBounds) {
            // this is inbounds, so we can at least move in here
            // but if the new position has a ball, we need to make sure we can push it
            if (world[xNew + yNew * worldSize] == CellEntity.BALL) {
                if (xMove < 0) {
                    // moving left
                    if (xNew > 0) {
                        moveSuccess = true
                        moveBall(xNew, yNew, xMove, yMove)
                    }
                    else false
                } else if (xMove > 0) {
                    // moving right
                    if (xNew < worldSize - 1) {
                        moveSuccess = true
                        moveBall(xNew, yNew, xMove, yMove)
                    }
                    else false
                } else if (yMove < 0) {
                    // moving down
                    if (yNew > 0) {
                        moveSuccess = true
                        moveBall(xNew, yNew, xMove, yMove)
                    }
                    else false
                } else {
                    // moving up
                    if (yNew < worldSize - 1) {
                        moveSuccess = true
                        moveBall(xNew, yNew, xMove, yMove)
                    }
                    false
                }
            } else {
                moveSuccess = true
                false
            }
        } else false

        if (success || moveSuccess) {
            xCursor += xMove
            yCursor += yMove
        }

        return success
    }

    /**
     * moves a ball. if it lands on a hole, make the hole and ball disappear and return true
     * else, just move the ball and leave it. return false
     */
    private fun moveBall(xBall: Int, yBall: Int, xMove: Int, yMove: Int) : Boolean {
        world[xBall + yBall * worldSize] = CellEntity.NOTHING
        val xNew = xBall + xMove
        val yNew = yBall + yMove

        return if (world[xNew + yNew * worldSize] == CellEntity.HOLE) {
            world[xNew + yNew * worldSize] = CellEntity.NOTHING
            true
        } else {
            world[xNew + yNew * worldSize] = CellEntity.BALL
            false
        }
    }
}