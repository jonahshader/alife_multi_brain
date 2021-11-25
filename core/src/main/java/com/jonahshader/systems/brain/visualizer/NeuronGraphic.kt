package com.jonahshader.systems.brain.visualizer

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.jonahshader.MultiBrain
import com.jonahshader.systems.scenegraph.Node2D
import ktx.math.plusAssign

abstract class NeuronGraphic : Node2D() {
    val weights = mutableListOf<WeightGraphic>() // for rendering ??? might not need
    val color = Color(1f, 1f, 1f, 1f)
    var radius = 32f
    var velocity = Vector2()
    var acceleration = Vector2()
    var force = Vector2()
    var mass = 1.0f

    override fun customUpdate(dt: Float) {
        force.scl(1/mass)
        acceleration.set(force)
        acceleration.scl(dt)
        velocity += acceleration
        localPosition.add(velocity.x * dt, velocity.y * dt)

        force.setZero()

    }

    override fun customRender(batch: Batch) {
        MultiBrain.shapeDrawer.setColor(color)
        MultiBrain.shapeDrawer.filledCircle(globalPosition, radius)
    }
}