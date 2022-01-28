package com.jonahshader.systems.creatureparts.softbody

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.jonahshader.systems.creatureparts.Controllable
import com.jonahshader.systems.creatureparts.Sensor
import com.jonahshader.systems.ga.BodyGenes
import com.jonahshader.systems.scenegraph.Node2D
import com.jonahshader.systems.utils.Rand
import ktx.math.plusAssign
import java.util.*
import kotlin.math.roundToInt
import kotlin.random.asKotlinRandom

open class SoftBody : Node2D, Controllable, Sensor {
    val grippers = mutableListOf<Gripper>()
    protected val muscles = mutableListOf<Muscle>()
    private val rand: Random
    private val kRand: kotlin.random.Random
    private val center = Vector2()
    private var params = SoftBodyParams()

    // brain inputs and outputs
    private var inputs = 0
    protected var outputs = 0

    constructor(rand: Random = Rand.randx, params: SoftBodyParams) {
        this.rand = rand
        this.params = params
        kRand = rand.asKotlinRandom()
        for (i in 0 until params.gripperCountInit) {
            grippers += Gripper(rand, params.gripperInitPositionMaxRadius)
        }

        connect(params.connectivityInit, params.minMuscleLength, params.maxMuscleExtention)
//        prune
        outputs = muscles.size + grippers.size
        grippers.forEach {
            addChild(it)
        }
    }

    constructor(rand: Random = Rand.randx, bodyGenes: BodyGenes) {
        this.rand = rand
        kRand = rand.asKotlinRandom()
        for (g in bodyGenes.gripperGenes)
            grippers += Gripper(g)
        for (m in bodyGenes.muscleGenes)
            muscles += Muscle(m, grippers)

        inputs = 0
        outputs = muscles.size + grippers.size

        // add children
        grippers.forEach {
            addChild(it)
        }
    }

    override fun preUpdate(dt: Float) {
        muscles.forEach { it.update(dt) }
    }

    override fun customRender(batch: Batch, cam: Camera) {
        muscles.forEach { it.render() }
    }

    override fun setControllableValue(index: Int, value: Float) {
        if (index < grippers.size) grippers[index].setControllableValue(index, value)
        else muscles[index - grippers.size].setControllableValue(index - grippers.size, value)
    }

    override fun getSensorCount(): Int {
        return inputs
    }

    override fun getSensorValue(index: Int): Float {
        return 0f
    }

    fun makeGenes() : BodyGenes {
        val genes = BodyGenes()
        grippers.forEach { genes.gripperGenes += it.makeGene() }
        muscles.forEach { genes.muscleGenes += it.makeGene(grippers) }
        return genes
    }

    fun connect(connectivity: Float, minLength: Float, maxExtension: Float) {
        val maxConnections = (grippers.size * (grippers.size - 1))/2
        var toAddOrRemove = (maxConnections * connectivity - muscles.size).roundToInt()

        while (toAddOrRemove > 0) {
            if (addRandomMuscle(minLength, maxExtension)) toAddOrRemove--
        }
        while (toAddOrRemove < 0) {
            if (removeRandomMuscle()) toAddOrRemove++
        }
    }

    private fun removeRandomMuscle(): Boolean {
        return if (muscles.isNotEmpty()) {
            muscles.removeAt(kRand.nextInt(muscles.size))
            outputs--
            true
        } else {
            false
        }
    }

    private fun addRandomMuscle(minLength: Float, maxExtension: Float): Boolean {
        // pick two grippers at random
        val gripper1 = grippers.randomOrNull() ?: return false
        val gripper2 = (grippers - gripper1).randomOrNull() ?: return false

        return if (!muscleExists(gripper1, gripper2)) {
            muscles += Muscle(gripper1, gripper2, minLength, minLength + kRand.nextFloat() * maxExtension)
            outputs++
            true
        } else {
            false
        }
    }

    private fun muscleExists(gripper1: Gripper, gripper2: Gripper): Boolean {
        for (m in muscles) {
            if (m.isSameMuscle(gripper1, gripper2)) return true
        }
        return false
    }

    fun computeCenter() : Vector2 {
        center.setZero()
        if (grippers.isNotEmpty()) {
            for (g in grippers) {
                center += g.localPosition
            }
            center.scl(1f/grippers.size)
        }

        return center
    }

    private fun removeRandomGripper() : Boolean = if (grippers.isNotEmpty()) {
        val toRemove = grippers.random(kRand)
        val before = muscles.size
        muscles.removeIf { it.isConnectedToGripper(toRemove) }
        outputs -= before - muscles.size
        grippers.remove(toRemove)
        outputs--
        true
    } else { false }

    open fun mutate(amount: Float) {
        val maxConnections = (grippers.size * (grippers.size - 1))/2
        var addRemoveGripperCount = (rand.nextGaussian() * params.addRemoveGripperSd * amount).roundToInt()
        var addRemoveMuscleCount = (rand.nextGaussian() * params.addRemoveMuscleSd * amount).roundToInt()

        addRemoveGripperCount = addRemoveGripperCount.coerceAtLeast(-grippers.size + 2)
        addRemoveMuscleCount = addRemoveMuscleCount.coerceIn(-muscles.size + 1, maxConnections - muscles.size)

        while (addRemoveGripperCount < 0) {
            removeRandomGripper()
            addRemoveGripperCount++
        }

        while (addRemoveGripperCount > 0) {
            grippers += generateRandomGripper()
            addRemoveGripperCount--
        }

        while (addRemoveMuscleCount < 0) {
            removeRandomMuscle()
            addRemoveMuscleCount++
        }

        var fails = 0
        while (addRemoveMuscleCount > 0) {
            if (addRandomMuscle(params.minMuscleLength, params.maxMuscleExtention)) addRemoveMuscleCount--
            else {
                fails++
                if (fails > 100) {
                    println("failed a lot")
                    addRemoveMuscleCount = 0
                }
            }
        }

//        grippers.forEach {
//            it.mutate(amount)
//        }
//
//        muscles.forEach {
//            it.mutate(amount)
//        }
    }

    private fun generateRandomGripper(): Gripper {
        outputs++
        return Gripper(rand, params.gripperInitPositionMaxRadius)
    }

}