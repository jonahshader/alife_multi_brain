package com.jonahshader.systems.creatureparts.softbody

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.jonahshader.systems.creatureparts.Controllable
import com.jonahshader.systems.ga.GripperGene
import ktx.math.plusAssign
import java.util.*

class Gripper(val initLocalPosition: Vector2) : BodyPart(initLocalPosition), Controllable {
    constructor(gripperGene: GripperGene) : this(Vector2(gripperGene.xInit, gripperGene.yInit))
                private var grip = 0f
    constructor(rand: Random, posMaxRadius: Float) : this(Vector2(rand.nextFloat() * posMaxRadius, 0f)
        .rotateRad(rand.nextFloat() * Math.PI.toFloat() * 2))

    private val counterForce = Vector2()

    companion object {
        const val MAX_GRIP = 25f
        const val MIN_GRIP = 5f
    }

    override fun setControllableValue(index: Int, value: Float) {
        grip = value.coerceIn(0f, 1f)
    }

    override fun preUpdate(dt: Float) {
        force.scl(1/mass)
        acceleration.set(force)
        acceleration.scl(dt)
        velocity += acceleration
        val dragAmount = ((MAX_GRIP - MIN_GRIP) * grip + MIN_GRIP) * dt
        if (velocity.len2() > dragAmount * dragAmount) {
            velocity.setLength(velocity.len() - dragAmount)
        } else {
            velocity.setZero()
        }
        localPosition.add(velocity.x * dt, velocity.y * dt)
        force.setZero()
    }

    override fun customRender(batch: Batch, cam: Camera) {
        color.set(1-grip, 1-grip, .5f, 1f)
        super.customRender(batch, cam)
    }

    fun makeGene() = GripperGene(initLocalPosition.x, initLocalPosition.y)

}