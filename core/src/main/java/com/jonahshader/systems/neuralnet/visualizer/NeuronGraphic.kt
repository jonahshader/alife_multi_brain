package com.jonahshader.systems.neuralnet.visualizer

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.jonahshader.MultiBrain
import com.jonahshader.systems.neuralnet.neurons.Neuron
import com.jonahshader.systems.neuralnet.neurons.NeuronName
import com.jonahshader.systems.neuralnet.visualizer.NetworkVisualizer.Companion.ioNeuronHorizontalSpacing
import com.jonahshader.systems.neuralnet.visualizer.NetworkVisualizer.Companion.ioNeuronPadding
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

    var radius = DEFAULT_RADIUS
    var velocity = Vector2()
    var acceleration = Vector2()
    var force = Vector2()
    var mass = 1.0f

    init {
        localPosition.set(initLocalPosition)
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
            velocity.add(-velocity.x * 5f * dt, -velocity.y * 5f * dt)
            val dragAmount = DRAG_METERS_PER_SECOND * dt
            if (velocity.len2() > dragAmount * dragAmount) {
                velocity.setLength(velocity.len() - dragAmount)
            } else {
                velocity.setZero()
            }
//            if (velocity.len2() > SPEED_LIMIT * SPEED_LIMIT) {
//                velocity.setLength(SPEED_LIMIT)
//            }
            localPosition.add(velocity.x * dt, velocity.y * dt)

            force.setZero()
        }
    }

    override fun isVisible(cam: Camera) = cam.frustum.sphereInFrustum(globalPosition.x, globalPosition.y, 0f, radius)

    override fun customRender(batch: Batch, cam: Camera) {
//        // activation color
//        val brightness = (neuron.out / 2f).coerceIn(0f, 1f)
//        MultiBrain.shapeDrawer.setColor(brightness, brightness, brightness, 1f)
//        MultiBrain.shapeDrawer.filledCircle(globalPosition, radius)
//
//        // shell color
//        MultiBrain.shapeDrawer.setColor(color)
//        MultiBrain.shapeDrawer.circle(globalPosition.x, globalPosition.y, radius)
        neuron.render(globalPosition)
    }
}