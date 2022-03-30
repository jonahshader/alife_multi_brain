package com.jonahshader.systems.simulation.fluidsim

import com.badlogic.gdx.math.Vector2
import com.jonahshader.MultiBrain
import ktx.graphics.circle
import ktx.math.minusAssign
import ktx.math.plusAssign
import ktx.math.timesAssign
import kotlin.math.floor

open class Particle(val pos: Vector2, val vel: Vector2) {
    companion object {
        const val PARTICLE_RADIUS = 0.125f
    }
    private val temp = Vector2()

    fun move(dt: Float) {
        temp.set(vel) *= dt
        pos += temp
    }

    fun moveBackward(dt: Float) {
        temp.set(vel) *= dt
        pos -= temp
    }

    fun handleWallCollision(sim: FluidSimGrid, dt: Float) {
        var tileX = floor(pos.x).toInt()
        var tileY = floor(pos.y).toInt()
        while (tileX !in 0 until sim.worldSize || tileY !in 0 until sim.worldSize) {
            moveBackward(dt)
            tileX = floor(pos.x).toInt()
            tileY = floor(pos.y).toInt()
            println("moved backward to undo clipping through wall i guess")
        }
        if (sim.walls[tileX + tileY * sim.worldSize]) {
            moveBackward(dt)
            val pTileX = floor(pos.x).toInt()
            val pTileY = floor(pos.y).toInt()
//            move(dt/2)
            if (pTileX != tileX)
                vel.x = -vel.x
            if (pTileY != tileY)
                vel.y = -vel.y
//            move(dt/2)
        }
    }
    fun renderParticle() {
//        temp.set(pos).scl(1/ PARTICLE_RADIUS)
        MultiBrain.shapeRenderer.circle(pos, PARTICLE_RADIUS, 3)
    }
}