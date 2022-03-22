package com.jonahshader.systems.simulation.fluidsim

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.jonahshader.MultiBrain
import com.jonahshader.systems.simulation.fluidsim.Particle.Companion.PARTICLE_RADIUS
import com.jonahshader.systems.utils.Rand
import ktx.graphics.circle
import ktx.math.minusAssign
import ktx.math.plusAssign
import kotlin.math.floor
import kotlin.math.sqrt
import kotlin.math.tanh

class Particle(val pos: Vector2, val vel: Vector2) {
    companion object {
        const val PARTICLE_RADIUS = 0.125f
    }
    private val temp = Vector2()
    private val inTiles = mutableListOf<MutableList<Particle>>()
//    fun updatePTile() {
//        pTileX = floor(pos.x).toInt()
//        pTileY = floor(pos.y).toInt()
//    }

    fun move(dt: Float) {
        temp.set(vel).scl(dt)
        pos += temp
    }

    fun moveBackward(dt: Float) {
        temp.set(vel).scl(dt)
        pos -= temp
    }

    fun handleWallCollision(sim: FluidSimParticles, dt: Float) {
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

    fun updateTilePresence(sim: FluidSimParticles) {
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

    fun renderParticle() {
//        temp.set(pos).scl(1/ PARTICLE_RADIUS)
        MultiBrain.shapeRenderer.circle(pos, PARTICLE_RADIUS, 3)
    }
}

class FluidSimParticles(internal val worldSize: Int) {
    private val particles = mutableListOf<Particle>()
    private val world = mutableListOf<MutableList<Particle>>()
    val walls = mutableListOf<Boolean>()
    private val temp = Vector2()
    private val temp2 = Vector2()
    private val temp3 = Vector2()
    private val vt1 = Vector2()
    private val vt2 = Vector2()
    private val tmpColor = Color()

    init {
        for (i in 0 until worldSize * worldSize) {
            world += mutableListOf<Particle>()
            walls += false
        }

        for (i in 0 until worldSize) {
            walls[i] = true
            walls[i + worldSize * (worldSize-1)] = true
            walls[i * worldSize] = true
            walls[i * worldSize + worldSize - 1] = true
        }
    }

    fun update(dt: Float) {

        particles.forEach { it.move(dt) }
        particles.forEach { it.handleWallCollision(this, dt) }
        particles.forEach { it.updateTilePresence(this) }
        world.forEach {
            it.forEachIndexed { index, p1 ->
                for (otherPIndex in index + 1 until it.size) {
                    val p2 = it.elementAt(otherPIndex)
                    checkAndHandleCollision(p1, p2, dt)
                }
            }
        }
    }

    fun renderParticles() {
        MultiBrain.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        MultiBrain.shapeRenderer.color = Color.WHITE
        particles.forEach { it.renderParticle() }
        MultiBrain.shapeRenderer.end()
    }

    fun renderFields() {
        // TODO: render fields ;)

        MultiBrain.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        for (y in 0 until worldSize) for (x in 0 until worldSize) {
            if (walls[x + y * worldSize]) {
                MultiBrain.shapeRenderer.color = Color.BROWN
                MultiBrain.shapeRenderer.rect(x.toFloat(), y.toFloat(), 1f, 1f)
            } else {
                var averageKe = 0f
                var numParticles = 0
                var averageXVel = 0f
                var averageYVel = 0f
                world[x + y * worldSize].filter { it.pos.x >= x && it.pos.x < x+1 && it.pos.y >= y && it.pos.y < y+1 } .forEach {
                    numParticles++
                    averageKe += it.vel.len2()
                    averageXVel += it.vel.x
                    averageYVel += it.vel.y
                }
//                val brightness = tanh(sqrt(averageKe / 20)) * numParticles / 10
                val brightness = numParticles / 10f
//                tmpColor.set(sigmoid(averageXVel / 20) * brightness, sigmoid(averageYVel / 20) * brightness, brightness * .5f, 1f)
                tmpColor.set(averageXVel / 20 + .5f, averageYVel / 20 + .5f, averageKe / 20, 1f).mul(brightness)
                MultiBrain.shapeRenderer.color = tmpColor
                MultiBrain.shapeRenderer.rect(x.toFloat(), y.toFloat(), 1f, 1f)
            }
        }

//        MultiBrain.shapeRenderer.color = Color.RED
//        particles.forEach { it.renderParticle() }
        MultiBrain.shapeRenderer.end()
    }

    private fun sigmoid(x: Float) = tanh(x) * .5f + .5f

    private fun checkAndHandleCollision(p1: Particle, p2: Particle, dt: Float) {
        temp.set(p1.pos).sub(p2.pos)
        if (temp.len2() < PARTICLE_RADIUS * 2 * PARTICLE_RADIUS * 2) {
            p1.moveBackward(dt / 2)
            p2.moveBackward(dt / 2)
//            // compute collision time
//            // modified from https://stackoverflow.com/questions/43577298/calculating-collision-times-between-two-circles-physics
//            val collisionTime: Float
//            val dist = PARTICLE_RADIUS * PARTICLE_RADIUS * 4
//            val a = (p1.vel.x - p2.vel.x) * (p1.vel.x - p2.vel.x) + (p1.vel.y - p2.vel.y) * (p1.vel.y - p2.vel.y)
//            val b = 2 * ((p1.pos.x - p2.pos.x) * (p1.vel.x - p2.vel.x) + (p1.pos.y - p2.pos.y) * (p1.vel.y - p2.vel.y))
//            val c = (p1.pos.x - p2.pos.x) * (p1.pos.x - p2.pos.x) + (p1.pos.y - p2.pos.y) * (p1.pos.y - p2.pos.y) - dist
//            val d = b * b - 4 * a * c
//            // ignore glancing collisions that may not cause a response due to limited precision and lead to an infinite loop
//            collisionTime = if (b > -1e-6 || d <= 0)
//                Float.NaN
//            else {
//                val e = sqrt(d)
//                val t1 = (-b - e) / (2 * a) // collision time, +ve or -ve
//                val t2 = (-b + e) / (2 * a) // exit time, +ve or -ve
//                // if we are overlapping and moving closer, collide now (hopefully doesn't happen. TODO: add asserts and see if this happens)
//                if (t1 < 0 && t2 > 0 && b <= -1e-6)
//                    0f
//                else
//                    t1
//            }
//            p1.move(collisionTime)
//            p2.move(collisionTime)
//            p1.vel.setZero()
//            p2.vel.setZero()
            val len2 = temp3.set(p1.pos).sub(p2.pos).len2()
            val dotp = temp.set(p1.vel).sub(p2.vel).dot(temp2.set(p1.pos).sub(p2.pos))
            val dotpOverLen2 = dotp / len2
            vt1.set(p1.vel)
//            vt1 -= temp2.set(p1.pos).sub(p2.pos).scl(temp.set(p1.vel).sub(p2.vel).dot(temp2.set(p1.pos).sub(p2.pos)) / len2)
            vt1 -= temp2.set(p1.pos).sub(p2.pos).scl(dotpOverLen2)

            vt2.set(p2.vel)
//            vt2 -= temp2.set(p2.pos).sub(p1.pos).scl(temp.set(p2.vel).sub(p1.vel).dot(temp2.set(p2.pos).sub(p1.pos)) / len2)
            vt2 -= temp2.set(p2.pos).sub(p1.pos).scl(dotpOverLen2)

            p1.vel.set(vt1)
            p2.vel.set(vt2)

            p1.move(dt/2)
            p2.move(dt/2)
//            v1.set(
        }
    }

    private fun addParticle(p: Particle) {
//        p.updatePTile()
        particles += p
        p.updateTilePresence(this)
    }

    private fun wrap(n: Int) : Int {
        var out = n % worldSize
        if (out < 0) out += worldSize
        return out
    }

    fun tileAtPos(xTile: Int, yTile: Int) = world[xTile + yTile * worldSize]
    fun fillWorld(particlesPerTile: Int, velSd: Float) {
        fill(particlesPerTile, velSd, Rectangle(0f, 0f, worldSize.toFloat(), worldSize.toFloat()))
    }
    fun fill(particlesPerTile: Int, velSd: Float, rect: Rectangle) {
        for (y in rect.y.toInt() until rect.y.toInt() + rect.height.toInt()) for (x in rect.x.toInt() until rect.x.toInt() + rect.width.toInt()) {
            for (i in 0 until particlesPerTile)
                if (!walls[x + y * worldSize]) {
                    val xVariance = Rand.randx.nextFloat()
                    val yVariance = Rand.randx.nextFloat()
                    addParticle(
                        Particle(
                            Vector2(x + xVariance, y + yVariance),
                            Vector2(
                                Rand.randx.nextGaussian().toFloat() * velSd,
                                Rand.randx.nextGaussian().toFloat() * velSd
                            )
                        )
                    )
                }
            }
    }

    fun addVelocityToAll(vel: Vector2) {
        particles.forEach {
            it.vel += vel
        }
    }

    fun addVelocityToRange(vel: Vector2, rect: Rectangle) {
        particles.forEach {
            if (rect.contains(it.pos)) {
                it.vel += vel
            }
        }
    }

    fun addVelocitySpiral(vel: Vector2, circle: Circle) {
        particles.forEach {
            if (circle.contains(it.pos)) {
                it.vel.set(it.pos).sub(circle.x, circle.y).rotateRad(vel.angleRad()).setLength(vel.len())
            }
        }
    }

    fun addWall(wallRange: Rectangle) {
        for (y in wallRange.y.toInt() until wallRange.y.toInt() + wallRange.height.toInt()) {
            for (x in wallRange.x.toInt() until wallRange.x.toInt() + wallRange.width.toInt()) {
                walls[x + y * worldSize] = true
            }
        }
    }

    fun removeWall(wallRange: Rectangle) {
        for (y in wallRange.y.toInt() until wallRange.y.toInt() + wallRange.height.toInt()) {
            for (x in wallRange.x.toInt() until wallRange.x.toInt() + wallRange.width.toInt()) {
                walls[x + y * worldSize] = false
            }
        }
    }

    fun removeWall(x: Int, y: Int) {
        walls[x + y * worldSize] = false
    }
//    fun tileAtPosWrapped(xTile: Int, yTile: Int) = world[wrap(xTile) + wrap(yTile) * worldSize]
}