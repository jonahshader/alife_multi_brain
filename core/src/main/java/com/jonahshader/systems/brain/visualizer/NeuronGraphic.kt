package com.jonahshader.systems.brain.visualizer

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.jonahshader.MultiBrain
import com.jonahshader.systems.brain.neurons.Neuron
import com.jonahshader.systems.scenegraph.Node2D
import ktx.math.plusAssign

class NeuronGraphic(private val neuron: Neuron, initLocalPosition: Vector2, private val static: Boolean) : Node2D() {
    companion object {
        const val DEFAULT_RADIUS = 8.0f
    }

    val color = Color(1f, 1f, 1f, 1f)
    var radius = DEFAULT_RADIUS
    var velocity = Vector2()
    var acceleration = Vector2()
    var force = Vector2()
    var mass = 1.0f

    init {
        localPosition.set(initLocalPosition)
    }

    override fun customUpdate(dt: Float) {
        if (!static) {
            force.scl(1/mass)
            acceleration.set(force)
            acceleration.scl(dt)
            velocity += acceleration
            localPosition.add(velocity.x * dt, velocity.y * dt)

            force.setZero()
        }
    }

    override fun customRender(batch: Batch) {
        // TODO: color by post-activation or something
        // or maybe change the size by magnitude
        MultiBrain.shapeDrawer.setColor(color)
        MultiBrain.shapeDrawer.filledCircle(globalPosition, radius)
    }
}