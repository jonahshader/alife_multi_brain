package com.jonahshader.systems.brain.visualizer

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.jonahshader.MultiBrain
import com.jonahshader.systems.brain.neurons.Neuron
import com.jonahshader.systems.brain.neurons.NeuronType
import com.jonahshader.systems.brain.visualizer.NetworkVisualizer.Companion.ioNeuronHorizontalSpacing
import com.jonahshader.systems.brain.visualizer.NetworkVisualizer.Companion.ioNeuronPadding
import com.jonahshader.systems.scenegraph.Node2D
import ktx.math.minus
import ktx.math.plusAssign
import java.util.*

class NeuronGraphic(val neuron: Neuron, initLocalPosition: Vector2) : Node2D() {
    constructor(neuron: Neuron, rand: Random) : this(neuron, Vector2(
        (rand.nextFloat()-.5f) * (ioNeuronHorizontalSpacing - ioNeuronPadding),
        (rand.nextFloat()-.5f) * ioNeuronHorizontalSpacing * .75f))
    companion object {
        const val DEFAULT_RADIUS = 4.0f
        var MOUSE_POS = Vector2()
        private const val DRAG_METERS_PER_SECOND = 15f // 15f
        private const val SPEED_LIMIT = 280f
    }

    private val color = Color(1f, 1f, 1f, 1f)
    val colorModifier = Color(1f, 1f, 1f, 1f)
    var radius = DEFAULT_RADIUS
    var velocity = Vector2()
    var acceleration = Vector2()
    var force = Vector2()
    var mass = 1.0f

    init {
        localPosition.set(initLocalPosition)
//        velocity.x += Math.random().toFloat() - .5f
//        velocity.y += Math.random().toFloat() - .5f

        when (neuron.neuronType) {
            NeuronType.Input -> color.set(.125f, 1f, 1f, 1f)
            NeuronType.LeakyReLU -> color.set(.25f, 1f, .5f, 1f)
            NeuronType.Output -> color.set(1f, 1f, .125f, 1f)
            NeuronType.Tanh -> color.set(.8f, .2f, .8f, 1f)
            NeuronType.Sin -> color.set(.8f, .2f, .2f, 1f)
            NeuronType.LeakyIntegrateAndFire -> color.set(.2f, .8f, .2f, 1f)
        }
    }

    override fun preUpdate(dt: Float) {
        if (Gdx.input.isTouched && (MOUSE_POS - globalPosition).len2() < radius * radius * 4) {
            // find delta
            val mouseDelta = MOUSE_POS - globalPosition
            localPosition += mouseDelta
            velocity.setZero()
            acceleration.setZero()
            force.setZero()
        } else if (neuron.neuronCategory == Neuron.NeuronCategory.HIDDEN) {
//        if (true) {
            force.scl(1/mass)
            acceleration.set(force)
            acceleration.scl(dt)
            velocity += acceleration
            val dragAmount = DRAG_METERS_PER_SECOND * dt
            if (velocity.len2() > dragAmount * dragAmount) {
                velocity.setLength(velocity.len() - dragAmount)
            } else {
                velocity.setZero()
            }
            if (velocity.len2() > SPEED_LIMIT * SPEED_LIMIT) {
                velocity.setLength(SPEED_LIMIT)
            }
            localPosition.add(velocity.x * dt, velocity.y * dt)

            force.setZero()
        }
    }

    override fun isVisible(cam: Camera) = cam.frustum.sphereInFrustum(globalPosition.x, globalPosition.y, 0f, radius)

    override fun customRender(batch: Batch, cam: Camera) {
        // activation color
        val brightness = (neuron.out / 2f).coerceIn(0f, 1f)
        MultiBrain.shapeDrawer.setColor(brightness, brightness, brightness, 1f)
        MultiBrain.shapeDrawer.filledCircle(globalPosition, radius)

        // shell color
        MultiBrain.shapeDrawer.setColor(color)
        MultiBrain.shapeDrawer.circle(globalPosition.x, globalPosition.y, radius)
    }
}