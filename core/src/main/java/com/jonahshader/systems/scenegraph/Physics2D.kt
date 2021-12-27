package com.jonahshader.systems.scenegraph

import com.badlogic.gdx.math.Vector2
import ktx.math.plusAssign
import ktx.math.times

abstract class Physics2D : Node2D() {
    val velocity = Vector2()
    val acceleration = Vector2()
    var mass = 1.0f
    val force = Vector2()
    private val temp = Vector2()

    override fun update(parentPos: Vector2, parentRot: Float, dt: Float) {
        // add force to acceleration
        force.scl(1/mass)
        acceleration.add(force)
        acceleration.scl(dt)
        // integrate acceleration
        temp.set(acceleration)
        temp.scl(dt)
        velocity += temp
        // integrate velocity
        temp.set(velocity)
        temp.scl(dt)
        localPosition += temp
        // set accel to zero
        acceleration.setZero()
        super.update(parentPos, parentRot, dt)
    }
}