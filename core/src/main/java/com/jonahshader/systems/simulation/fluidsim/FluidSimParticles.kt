package com.jonahshader.systems.simulation.fluidsim

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.jonahshader.MultiBrain
import com.jonahshader.systems.simulation.fluidsim.Particle.Companion.PARTICLE_RADIUS
import com.jonahshader.systems.utils.Rand
import ktx.graphics.circle
import ktx.math.minusAssign
import ktx.math.plusAssign
import kotlin.math.floor

class Particle(val pos: Vector2, val vel: Vector2) {
    companion object {
        const val PARTICLE_RADIUS = 0.1f
    }
    private val temp = Vector2()
    private val inTiles = mutableListOf<MutableSet<Particle>>()
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


//        if (dtx in -1..1 && dty in -1..1) {
//            for (y in -1..1) for (x in -1..1)
//                sim.tileAtPos(x + pTileX, y + pTileY) -= this
//            for (y in -1..1) for (x in -1..1)
//                sim.tileAtPos(x + tileX, y + tileY) += this
//        } else {
//            error("Ball moved too fast! Velocity of ${vel.len()}, $vel")
//        }

//        pTileX = tileX
//        pTileY = tileY

//        if (dtx == 1) {
//            if (dty == 1) {
//
//            } else if (dty == -1) {
//
//            } else if (dty == 0) {
//
//            } else {
//                error("Ball moved too fast! Velocity of ${vel.len()}, $vel")
//            }
//
//        } else if (dtx == -1) {
//
//        } else if (dtx == 0) {
//
//        } else {
//            error("Ball moved too fast! Velocity of ${vel.len()}, $vel")
//        }
    }

    fun render() {
        MultiBrain.shapeDrawer.circle(pos.x, pos.y, PARTICLE_RADIUS)
    }

    fun renderParticle() {
//        temp.set(pos).scl(1/ PARTICLE_RADIUS)
        MultiBrain.shapeRenderer.circle(pos, PARTICLE_RADIUS, 12)
    }
}

class FluidSimParticles(internal val worldSize: Int) {
    private val particles = mutableListOf<Particle>()
    private val world = mutableListOf<MutableSet<Particle>>()
    val walls = mutableListOf<Boolean>()
    private val temp = Vector2()
    private val temp2 = Vector2()
    private val temp3 = Vector2()
    private val vt1 = Vector2()
    private val vt2 = Vector2()

    init {
        for (i in 0 until worldSize * worldSize) {
            world += mutableSetOf<Particle>()
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

    fun render() {
        MultiBrain.shapeDrawer.setColor(Color.WHITE)
        particles.forEach { it.render() }
    }

    fun renderParticles() {
        // TODO: render fields ;)
        MultiBrain.shapeRenderer.color = Color.BROWN
        MultiBrain.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        for (y in 0 until worldSize) for (x in 0 until worldSize) {
            if (walls[x + y * worldSize]) {
                MultiBrain.shapeRenderer.rect(x.toFloat(), y.toFloat(), 1f, 1f)
            }
        }
        MultiBrain.shapeRenderer.end()

        MultiBrain.shapeRenderer.color = Color.WHITE
        MultiBrain.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        particles.forEach { it.renderParticle() }
        MultiBrain.shapeRenderer.end()
    }

//    public double TimeToCollision(Ball other)
//    {
//        double distance = (Radius + other.Radius) * (Radius + other.Radius);
//        double a = (Xvel - other.Xvel) * (Xvel - other.Xvel) + (Yvel - other.Yvel) * (Yvel - other.Yvel);
//        double b = 2 * ((X - other.X) * (Xvel - other.Xvel) + (Y - other.Y) * (Yvel - other.Yvel));
//        double c = (X - other.X) * (X - other.X) + (Y - other.Y) * (Y - other.Y) - distance;
//        double d = b * b - 4 * a * c;
//        // Ignore glancing collisions that may not cause a response due to limited precision and lead to an infinite loop
//        if (b > -1e-6 || d <= 0)
//            return double.NaN;
//        double e = Math.Sqrt(d);
//        double t1 = (-b - e) / (2 * a);    // Collison time, +ve or -ve
//        double t2 = (-b + e) / (2 * a);    // Exit time, +ve or -ve
//        // b < 0 => Getting closer
//        // If we are overlapping and moving closer, collide now
//        if (t1 < 0 && t2 > 0 && b <= -1e-6)
//            return 0;
//        return t1;
//    }

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
            vt1.set(p1.vel)
            vt1 -= temp2.set(p1.pos).sub(p2.pos).scl(temp.set(p1.vel).sub(p2.vel).dot(temp2.set(p1.pos).sub(p2.pos)) / len2)

            vt2.set(p2.vel)
            vt2 -= temp2.set(p2.pos).sub(p1.pos).scl(temp.set(p2.vel).sub(p1.vel).dot(temp2.set(p2.pos).sub(p1.pos)) / len2)

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
    fun fillWorld(particlesPerTile: Int) {
        for (y in 0 until worldSize) for (x in 0 until worldSize) {
            for (i in 0 until particlesPerTile)
                if (!walls[x + y * worldSize])
                    addParticle(Particle(Vector2(x + .5f, y + .5f), Vector2(Rand.randx.nextGaussian().toFloat(), Rand.randx.nextGaussian().toFloat())))
        }
    }

    fun addVelocityToAll(vel: Vector2) {
        particles.forEach {
            it.vel += vel
        }
    }
//    fun tileAtPosWrapped(xTile: Int, yTile: Int) = world[wrap(xTile) + wrap(yTile) * worldSize]
}