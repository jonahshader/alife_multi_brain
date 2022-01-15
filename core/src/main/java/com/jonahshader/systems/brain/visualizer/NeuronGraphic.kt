package com.jonahshader.systems.brain.visualizer

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.jonahshader.MultiBrain
import com.jonahshader.systems.brain.neurons.Neuron
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
            localPosition.add(velocity.x * dt, velocity.y * dt)

            force.setZero()
        }


    }

    override fun customRender(batch: Batch) {
        // TODO: color by post-activation or something
        // or maybe change the size by magnitude
        var brightness = (neuron.out / 2f).coerceIn(0f, 1f)
        when (neuron.neuronCategory) {
            Neuron.NeuronCategory.INPUT -> MultiBrain.shapeDrawer.setColor(0.25f * brightness, 1.0f * brightness, 1.0f * brightness, 1.0f)
            Neuron.NeuronCategory.OUTPUT -> MultiBrain.shapeDrawer.setColor(1.0f * brightness, 1.0f * brightness, 0.25f * brightness, 1.0f)
            Neuron.NeuronCategory.HIDDEN -> MultiBrain.shapeDrawer.setColor(color.r * brightness, color.g * brightness, color.b * brightness, 1.0f)
        }

        MultiBrain.shapeDrawer.filledCircle(globalPosition, radius)

        brightness = 1f
        when (neuron.neuronCategory) {
            Neuron.NeuronCategory.INPUT -> MultiBrain.shapeDrawer.setColor(0.25f * brightness, 1.0f * brightness, 1.0f * brightness, 1.0f)
            Neuron.NeuronCategory.OUTPUT -> MultiBrain.shapeDrawer.setColor(1.0f * brightness, 1.0f * brightness, 0.25f * brightness, 1.0f)
            Neuron.NeuronCategory.HIDDEN -> MultiBrain.shapeDrawer.setColor(color.r * brightness, color.g * brightness, color.b * brightness, 1.0f)
        }
        MultiBrain.shapeDrawer.circle(globalPosition.x, globalPosition.y, radius)
    }
}