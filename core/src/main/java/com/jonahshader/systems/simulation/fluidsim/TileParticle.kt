package com.jonahshader.systems.simulation.fluidsim

import com.badlogic.gdx.math.Vector2
import kotlin.math.floor

class TileParticle(pos: Vector2, vel: Vector2) : Particle(pos, vel){
//    companion object {
//        const val PARTICLE_RADIUS = 0.125f
//    }
//    private val temp = Vector2()
    private val inTiles = mutableListOf<MutableList<TileParticle>>()
////    fun updatePTile() {
////        pTileX = floor(pos.x).toInt()
////        pTileY = floor(pos.y).toInt()
////    }
//
//    fun move(dt: Float) {
//        temp.set(vel) *= dt
//        pos += temp
//    }
//
//    fun moveBackward(dt: Float) {
//        temp.set(vel) *= dt
//        pos -= temp
//    }
//
//    fun handleWallCollision(sim: FluidSimParticles, dt: Float) {
//        var tileX = floor(pos.x).toInt()
//        var tileY = floor(pos.y).toInt()
//        while (tileX !in 0 until sim.worldSize || tileY !in 0 until sim.worldSize) {
//            moveBackward(dt)
//            tileX = floor(pos.x).toInt()
//            tileY = floor(pos.y).toInt()
//            println("moved backward to undo clipping through wall i guess")
//        }
//        if (sim.walls[tileX + tileY * sim.worldSize]) {
//            moveBackward(dt)
//            val pTileX = floor(pos.x).toInt()
//            val pTileY = floor(pos.y).toInt()
////            move(dt/2)
//            if (pTileX != tileX)
//                vel.x = -vel.x
//            if (pTileY != tileY)
//                vel.y = -vel.y
////            move(dt/2)
//        }
//    }
//
    fun updateTilePresence(sim: FluidSimGrid) {
//        val tileX = floor(pos.x).toInt()
//        val tileY = floor(pos.y).toInt()
//
//        val dtx = tileX - pTileX
//        val dty = tileY - pTileY

        inTiles.forEach { it -= this }
        inTiles.clear()

        val tileXMin = floor(pos.x - PARTICLE_RADIUS).toInt()
        val tileXMax = floor(pos.x + PARTICLE_RADIUS).toInt()
        val tileYMin = floor(pos.y - PARTICLE_RADIUS).toInt()
        val tileYMax = floor(pos.y + PARTICLE_RADIUS).toInt()

        for (y in tileYMin..tileYMax) {
            for (x in tileXMin..tileXMax) {
                if (x in 0 until sim.worldSize && y in 0 until sim.worldSize) {
                    val tileAtPos = sim.tileAtPos(x, y)
                    inTiles += tileAtPos
                    tileAtPos += this
                }
            }
        }
    }
//
//    fun renderParticle() {
////        temp.set(pos).scl(1/ PARTICLE_RADIUS)
//        MultiBrain.shapeRenderer.circle(pos, PARTICLE_RADIUS, 3)
//    }
}