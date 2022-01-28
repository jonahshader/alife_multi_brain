package com.jonahshader.systems.brain.visualizer

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.math.MathUtils.randomTriangular
import com.badlogic.gdx.math.Vector2
import com.jonahshader.MultiBrain
import com.jonahshader.systems.brain.Weight
import com.jonahshader.systems.brain.neurons.Neuron
import com.jonahshader.systems.brain.visualizer.NeuronGraphic.Companion.DEFAULT_RADIUS
import ktx.math.minus
import kotlin.math.absoluteValue
import kotlin.math.pow

class WeightSpringGraphic(val start: NeuronGraphic, val end: NeuronGraphic, val weight: Weight,
//                          var targetLength: Float = neuronGDist(start, end) + randomTriangular(15.0f),
                          var targetLength: Float = 130f, private val forceScalar: Float = 1f,
                          val sc: SpringConstants) {
    private var startToEnd = Vector2(end.localPosition).sub(start.localPosition)
    private var pLength = targetLength
    private val direction = Vector2(startToEnd).nor()
    private val temp = Vector2()
    private var arrowPos = 0f

    companion object {
        private const val THICKNESS_SCALE = 2f
        private const val MIN_THICKNESS = .5f
        private fun neuronGDist(a: NeuronGraphic, b: NeuronGraphic) : Float = (a.localPosition - b.localPosition).len()
    }

//    init {
//        if (start.neuron.neuronCategory == Neuron.NeuronCategory.INPUT || start.neuron.neuronCategory == Neuron.NeuronCategory.OUTPUT ||
//                    end.neuron.neuronCategory == Neuron.NeuronCategory.INPUT || end.neuron.neuronCategory == Neuron.NeuronCategory.OUTPUT) {
//            targetLength *= .1f
//            pLength = targetLength
//        }
//    }

    fun update(dt: Float) {
        startToEnd.set(end.localPosition)
        startToEnd.sub(start.localPosition)
        direction.set(startToEnd).nor()
        val length = startToEnd.len()
        val lengthVelocity = (length - pLength) / dt
        pLength = length
        val error = length - targetLength
        var force = (((error * -sc.newtonsPerMeter) + (lengthVelocity * -sc.newtonsPerMeterPerSecond)))
//        force *= weight.weight.absoluteValue.pow(1/2f)
        force *= weight.weight.absoluteValue
        force *= forceScalar

        end.force.add(direction.x * force, direction.y * force)
        start.force.add(-direction.x * force, -direction.y * force)

        arrowPos += (dt / length) * 40
    }

    private fun isVisible(cam: Camera) : Boolean {
        temp.set(start.globalPosition).add(end.globalPosition).scl(.5f)
        return cam.frustum.boundsInFrustum(temp.x, temp.y, 0f,
            (start.globalPosition.x - end.globalPosition.x).absoluteValue * .5f,
            (start.globalPosition.y - end.globalPosition.y).absoluteValue * .5f,
            0f)
    }

    fun render(cam: Camera) {
        if (!isVisible(cam)) return
//        weight.calculateWeightedValue()
        val colorOffset = (weight.weight * .3f).coerceIn(-.5f, .5f)
//        val colorOffset = if (weight.weight > 0) .3f else -.3f
        MultiBrain.shapeDrawer.setColor(.5f + colorOffset, .25f, .5f - colorOffset, 1f)
        MultiBrain.shapeDrawer.line(start.globalPosition, end.globalPosition, (weight.weight.absoluteValue * THICKNESS_SCALE).coerceIn(MIN_THICKNESS, 10f))


        var arrowProgress = ((arrowPos) % 1f)
        arrowProgress *= arrowProgress * (3f - 2f * arrowProgress)

        val arrowProgressInv = 1-arrowProgress

        val arrowProgressDerivative = 6 * (arrowProgress - arrowProgress * arrowProgress)

        temp.setZero()
        temp.add(start.globalPosition.x * arrowProgressInv, start.globalPosition.y * arrowProgressInv)
        temp.add(end.globalPosition.x * arrowProgress, end.globalPosition.y * arrowProgress)

//        temp.set(start.globalPosition).add(end.globalPosition).scl(.5f)

        val wa = weight.weight.absoluteValue.coerceAtLeast(1f) * arrowProgressDerivative.pow(1/2f)
        val x1 = temp.x
        val y1 = temp.y
        temp.set(direction).scl(8f * wa).rotateDeg(140f).add(x1, y1)
        val x2 = temp.x
        val y2 = temp.y
        temp.set(direction).scl(8f * wa).rotateDeg(-140f).add(x1, y1)
        val x3 = temp.x
        val y3 = temp.y

//        MultiBrain.shapeDrawer.set
//        MultiBrain.shapeDrawer.setColor(.5f + colorOffset, .25f, .5f - colorOffset, 0.5f)
        MultiBrain.shapeDrawer.filledTriangle(x1, y1, x2, y2, x3, y3)
    }



}