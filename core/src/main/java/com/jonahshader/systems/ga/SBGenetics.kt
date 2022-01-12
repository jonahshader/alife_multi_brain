package com.jonahshader.systems.ga

import java.util.*
import kotlin.math.roundToInt

class LimbGene(var xInit: Float, var yInit: Float) {
    fun mutateScalars(rand: Random, amount: Float) {
        xInit += rand.nextGaussian().toFloat() * amount
        yInit += rand.nextGaussian().toFloat() * amount
    }
}

class MuscleGene(var limbA: Int, var limbB: Int, var minLength: Float, var maxLength: Float) {
    fun mutateScalars(rand: Random, amount: Float) {
        minLength += rand.nextGaussian().toFloat() * amount
        maxLength += rand.nextGaussian().toFloat() * amount
    }

    fun mutateIndices(rand: Random, amount: Float, size: Int) {
        if (rand.nextFloat() < .2f * amount)
            limbA = rand.nextInt(size)
        if (rand.nextFloat() < .2f * amount)
            limbB = rand.nextInt(size)
    }
}

class BodyGenes(var limbGenes: MutableList<LimbGene> = mutableListOf(),
                var muscleGenes: MutableList<MuscleGene> = mutableListOf()) {
    fun mutateScalars(rand: Random, amount: Float) {
        limbGenes.forEach { it.mutateScalars(rand, amount) }
        muscleGenes.forEach { it.mutateScalars(rand, amount) }
    }

    fun mutateIndices(rand: Random, amount: Float, size: Int) {
        muscleGenes.forEach { it.mutateIndices(rand, amount, size) }
    }
}

