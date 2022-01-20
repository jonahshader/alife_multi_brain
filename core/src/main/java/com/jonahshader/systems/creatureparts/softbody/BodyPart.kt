package com.jonahshader.systems.creatureparts.softbody

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.jonahshader.MultiBrain
import com.jonahshader.systems.collision.Bounded
import ktx.math.plusAssign

open class BodyPart(initLocalPosition: Vector2) : Bounded() {
    companion object {
        const val DEFAULT_RADIUS = 6.0f
    }

    private val bounds = Rectangle()
    val color = Color(1f, 1f, 1f, 1f)
    private var radius = DEFAULT_RADIUS
    var velocity = Vector2()
    var acceleration = Vector2()
    var force = Vector2()
    var mass = 1.0f

    init {
        bounds.width = radius * 2
        bounds.height = radius * 2
        localPosition.set(initLocalPosition)
    }

    override fun getBounds(): Rectangle = bounds

    override fun preUpdate(dt: Float) {
        force.scl(1/mass)
        acceleration.set(force)
        acceleration.scl(dt)
        velocity += acceleration
        localPosition.add(velocity.x * dt, velocity.y * dt)
        force.setZero()
    }

    override fun postUpdate(dt: Float) {
        bounds.x = globalPosition.x - radius
        bounds.y = globalPosition.y - radius
    }

    override fun customRender(batch: Batch) {
        MultiBrain.shapeDrawer.setColor(color)
        MultiBrain.shapeDrawer.filledCircle(globalPosition, radius)
    }
}