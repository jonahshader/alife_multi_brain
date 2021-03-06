package com.jonahshader.systems.creatureparts.softbody

import com.badlogic.gdx.math.Vector2
import com.jonahshader.MultiBrain
import com.jonahshader.systems.neuralnet.visualizer.SpringConstants
import com.jonahshader.systems.creatureparts.Controllable
import com.jonahshader.systems.ga.MuscleGene
import ktx.math.minusAssign

class Muscle(private val bodyPartA: BodyPart, private val bodyPartB: BodyPart,
private val minLength: Float, private val maxLength: Float) : Controllable {
    constructor(muscleGene: MuscleGene, bodyGrippers: MutableList<Gripper>) :
            this(bodyGrippers[muscleGene.bodyPartA], bodyGrippers[muscleGene.bodyPartB],
                muscleGene.minLength, muscleGene.maxLength)

    companion object {
        val sc = SpringConstants(10f, 1f)
    }
    private var lengthProgress = .5f
    private var targetLength = (minLength + maxLength) * .5f
    private var aToB = Vector2(bodyPartB.localPosition).sub(bodyPartA.localPosition)
    private var pLength = targetLength
    private val direction = Vector2(aToB).nor()

    override fun setControllableValue(index: Int, value: Float) {
        lengthProgress = value.coerceIn(0f, 1f)
        targetLength = ((maxLength - minLength) * lengthProgress) + minLength
    }

    fun update(dt: Float) {
        aToB.set(bodyPartB.localPosition)
        aToB -= bodyPartA.localPosition
        direction.set(aToB).nor()
        val length = aToB.len()
        val lengthVelocity = (length - pLength) / dt
        pLength = length
        val error = length - targetLength
        val force = (error * -sc.newtonsPerMeter) + (lengthVelocity * -sc.newtonsPerMeterPerSecond)

        bodyPartB.force.add(direction.x * force, direction.y * force)
        bodyPartA.force.add(-direction.x * force, -direction.y * force)
    }

    fun render() {
        MultiBrain.shapeDrawer.setColor(1-lengthProgress, .25f, lengthProgress, 1.0f)
        MultiBrain.shapeDrawer.line(bodyPartA.globalPosition, bodyPartB.globalPosition, 2.5f - lengthProgress)
    }

    fun makeGene(grippers: MutableList<Gripper>) = MuscleGene(grippers.indexOf(bodyPartA), grippers.indexOf(bodyPartB), minLength, maxLength)
    fun isSameMuscle(bpA: BodyPart, bpB: BodyPart) = bodyPartA == bpA && bodyPartB == bpB
    fun isConnectedToGripper(gripper: Gripper) : Boolean = bodyPartA == gripper || bodyPartB == gripper
}