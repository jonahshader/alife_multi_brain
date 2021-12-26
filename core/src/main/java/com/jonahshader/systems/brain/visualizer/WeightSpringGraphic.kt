package com.jonahshader.systems.brain.visualizer

import com.badlogic.gdx.math.Vector2
import com.jonahshader.MultiBrain
import com.jonahshader.systems.brain.Weight

class WeightSpringGraphic(val start: NeuronGraphic, val end: NeuronGraphic, val weight: Weight,
                          val targetLength: Float = 64.0f, val sc: SpringConstants) {
    private var startToEnd = Vector2(end.localPosition).sub(start.localPosition)
    private var pLength = targetLength
    private val direction = Vector2(startToEnd).nor()

    fun update(dt: Float) {
        startToEnd.set(end.localPosition)
        startToEnd.sub(start.localPosition)
        direction.set(startToEnd).nor()
        val length = startToEnd.len()
        val lengthVelocity = (length - pLength) / dt
        pLength = length
        val error = length - targetLength
        val force = (error * -sc.newtonsPerMeter) + (lengthVelocity * -sc.newtonsPerMeterPerSecond)

        end.force.add(direction.x * force, direction.y * force)
        start.force.add(-direction.x * force, -direction.y * force)
    }

    fun render() {
//        weight.calculateWeightedValue()
        MultiBrain.shapeDrawer.setColor(1.0f, 1.0f, 1.0f, 1.0f)
        MultiBrain.shapeDrawer.line(start.globalPosition, end.globalPosition, 1.0f)
    }

}