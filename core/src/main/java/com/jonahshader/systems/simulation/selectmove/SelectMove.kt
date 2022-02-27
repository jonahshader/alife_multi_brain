package com.jonahshader.systems.simulation.selectmove

import com.badlogic.gdx.math.Vector2
import com.jonahshader.systems.creatureparts.ReinforcementTask
import com.jonahshader.systems.neuralnet.Network
import com.jonahshader.systems.utils.Rand
import java.util.*

class SelectMove(private val worldSize: Int, private val numBalls: Int, private val rand: Random = Rand.randx, networkBuilder: (Int, Int) -> Network) : ReinforcementTask {
    override val pos: Vector2 = Vector2.Zero
    override val network = networkBuilder(worldSize * worldSize * 3, 2)

    private val world = ByteArray(worldSize * worldSize) { 0 }
    private var xCursor = 0
    private var yCursor = 0

    companion object {
        private const val CELL_SIZE = 32f
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
        network.reset()
        for (i in world.indices)
            world[i] = 0
        initEnv()
    }

    private fun initEnv() {
        for (i in 1..numBalls) {
            var spawnPos = rand.nextInt(world.size)
            while (world[spawnPos] != 0.toByte()) {
                spawnPos = rand.nextInt(world.size)
            }
            world[spawnPos] = i.toByte()
            while (world[spawnPos] != 0.toByte()) {
                spawnPos = rand.nextInt(world.size)
            }
            world[spawnPos] = (-i).toByte()
        }
        var spawnPos = rand.nextInt(world.size)
        while (world[spawnPos] != 0.toByte()) {
            spawnPos = rand.nextInt(world.size)
        }
        xCursor = spawnPos % worldSize
        yCursor = spawnPos / worldSize
    }

    override fun render() {
        // render world
        for (y in 0 until worldSize) for (x in 0 until worldSize) {
            // TODO: render balls and ball holes using the method used for varying neuron plot colors
            // make the holes have lower value (in hsv, maybe .5 or .25) and the balls have high
            // non solid tiles will be zero and the cursor will be white
            // might be cool to add walls at some point. these could be grey
        }
    }

    override fun update(dt: Float) {

        TODO("Not yet implemented")
    }

    override fun getFitness(): Float {
        TODO("Not yet implemented")
    }
}