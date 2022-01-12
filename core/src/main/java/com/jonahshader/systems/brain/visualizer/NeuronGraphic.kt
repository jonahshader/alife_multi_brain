package com.jonahshader.systems.brain.visualizer

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.jonahshader.MultiBrain
import com.jonahshader.systems.brain.neurons.Neuron
import com.jonahshader.systems.scenegraph.Node2D
import ktx.math.minus
import ktx.math.plusAssign

class NeuronGraphic(private val neuron: Neuron, initLocalPosition: Vector2) : Node2D() {
    companion object {
        const val DEFAULT_RADIUS = 2.0f
        var MOUSE_POS = Vector2()
    }

    val color = Color(1f, 1f, 1f, 1f)
    var radius = DEFAULT_RADIUS
    var velocity = Vector2()
    var acceleration = Vector2()
    var force = Vector2()
    var mass = 1.0f

    init {
        localPosition.set(initLocalPosition)
        velocity.x += Math.random().toFloat() - .5f
        velocity.y += Math.random().toFloat() - .5f
    }

    override fun preUpdate(dt: Float) {
        if (neuron.neuronCategory == Neuron.NeuronCategory.HIDDEN) {
//        if (true) {
            force.scl(1/mass)
            acceleration.set(force)
            acceleration.scl(dt)
            velocity += acceleration
            localPosition.add(velocity.x * dt, velocity.y * dt)

            force.setZero()
        }

        if (Gdx.input.isTouched) {
            if ((MOUSE_POS - globalPosition).len2() < radius * radius) {
                // find delta
                val mouseDelta = MOUSE_POS - globalPosition
                localPosition += mouseDelta
            }
        }
    }

    override fun customRender(batch: Batch) {
        // TODO: color by post-activation or something
        // or maybe change the size by magnitude
        when (neuron.neuronCategory) {
            Neuron.NeuronCategory.INPUT -> MultiBrain.shapeDrawer.setColor(0.25f, 1.0f, 1.0f, 1.0f)
            Neuron.NeuronCategory.OUTPUT -> MultiBrain.shapeDrawer.setColor(1.0f, 1.0f, 0.25f, 1.0f)
            Neuron.NeuronCategory.HIDDEN -> MultiBrain.shapeDrawer.setColor(color)
        }

        MultiBrain.shapeDrawer.filledCircle(globalPosition, radius)
    }
}