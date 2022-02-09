package com.jonahshader.systems.simulation.softbodytravel

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.IntSet
import com.jonahshader.systems.brain.cyclic.CyclicNetworkParams
import com.jonahshader.systems.creatureparts.softbody.BrainSoftBody
import com.jonahshader.systems.creatureparts.softbody.SoftBodyParams
import com.jonahshader.systems.ga.CombinedGenes
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.pow

class InstFitnessPair(var sb: BrainSoftBody, var fitness: Float = 0f)

class SoftBodyTravelSim(private val popSize: Int, private val steps: Int, private val dt: Float) {
    private val rand = Random()
    private val population = mutableListOf<InstFitnessPair>()

    val netParams = CyclicNetworkParams()
    val bodyParams = SoftBodyParams()
    private val bestLock = ReentrantLock()
    private var best: BrainSoftBody? = null


    fun setup() {
        netParams.connectivityInit = .11f
        bodyParams.gripperCountInit = 5
        for (i in 0 until popSize) {
            population += InstFitnessPair(BrainSoftBody(rand, bodyParams, netParams))
        }
    }

    fun runGeneration() {
//        var localBest = population[0]
//        var bestFitness = 0f

//        population.forEachIndexed { index, brainSoftBody ->
//            fitnesses[index] = runEpisode(brainSoftBody)
//            if (fitnesses[index] > bestFitness) {
//                bestFitness = fitnesses[index]
//                localBest = brainSoftBody
//            }
//        }

        population.parallelStream().forEach {
            it.fitness = runEpisode(it.sb)
        }
        var localBest = population[0].sb
        var bestFitness = population[0].fitness
        for (i in 1 until population.size) {
            val it = population[i]
            if (it.fitness > bestFitness) {
                bestFitness = it.fitness
                localBest = it.sb
            }
        }

        for (i in population.indices) {
            population[i] = InstFitnessPair(BrainSoftBody(rand, localBest.getCombinedGenes()))
        }
        for (i in 1 until population.size) {
            population[i].sb.network.mutateParameters((i / population.size.toFloat()).pow(2f) * 2)
        }
        println("best fitness: $bestFitness")
        bestLock.lock()
        best = localBest
        bestLock.unlock()
    }

    fun getCopyOfBest() : CombinedGenes? {
        if (best == null) return null
        bestLock.lock()
        val genes = best!!.getCombinedGenes()
        bestLock.unlock()
        return genes
    }

    private fun runEpisode(sb: BrainSoftBody) : Float {
        val positionSet = IntSet()
        for (i in 0 until steps) {
            sb.update(Vector2.Zero, 0f, dt)
            if (i % 10 == 0) {
                sb.grippers.forEach {
                    val hash = (it.globalPosition.x.toInt()/2) + (it.globalPosition.y.toInt()/2) * 80000
                    positionSet.add(hash)
                }
            }
        }
        return sb.computeCenter().len().pow(1/2f) * positionSet.size.toFloat()
    }
}