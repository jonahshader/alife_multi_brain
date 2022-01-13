package com.jonahshader.systems.creatureparts.softbody

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.jonahshader.systems.creatureparts.Controllable
import ktx.math.plusAssign

class Gripper(initLocalPosition: Vector2) : BodyPart(initLocalPosition), Controllable {
    private var grip = 0f
    private val counterForce = Vector2()

    companion object {
        const val GRIP_SCALE = 1f
    }

    override fun setControllableValue(index: Int, value: Float) {
        grip = value.coerceIn(0f, 1f)
    }

    override fun preUpdate(dt: Float) {
        counterForce.set(velocity).nor().scl(-dt * grip * mass * GRIP_SCALE)
        force += counterForce
        super.preUpdate(dt)
    }

    override fun customRender(batch: Batch) {
        color.set(1-grip, 1-grip, .5f, 1f)
        super.customRender(batch)
    }

}