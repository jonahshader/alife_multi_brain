package com.jonahshader.systems.ga

import java.util.*

class GripperGene(var xInit: Float, var yInit: Float) {
    fun mutateScalars(rand: Random, amount: Float) {
        xInit += rand.nextGaussian().toFloat() * amount
        yInit += rand.nextGaussian().toFloat() * amount
    }
}

class MuscleGene(var bodyPartA: Int, var bodyPartB: Int,
                 var minLength: Float, var maxLength: Float) {
    fun mutateScalars(rand: Random, amount: Float) {
        minLength += rand.nextGaussian().toFloat() * amount
        maxLength += rand.nextGaussian().toFloat() * amount
    }

    fun mutateIndices(rand: Random, mutateProbability: Float, size: Int) {
        if (rand.nextFloat() < mutateProbability)
            bodyPartA = rand.nextInt(size)
        if (rand.nextFloat() < mutateProbability)
            bodyPartB = rand.nextInt(size)
    }
}

class BodyGenes(var gripperGenes: MutableList<GripperGene> = mutableListOf(),
                var muscleGenes: MutableList<MuscleGene> = mutableListOf()) {
    fun mutateScalars(rand: Random, amount: Float) {
        gripperGenes.forEach { it.mutateScalars(rand, amount) }
        muscleGenes.forEach { it.mutateScalars(rand, amount) }
    }

    fun mutateIndices(rand: Random, probability: Float) {
        muscleGenes.forEach { it.mutateIndices(rand, probability, gripperGenes.size) }
    }

    fun getInputs() = 0
    fun getOutputs() = gripperGenes.size + muscleGenes.size

//    fun makeBody() : Sof
}

