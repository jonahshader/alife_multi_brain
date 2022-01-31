package com.jonahshader.systems.simulation.foodgrid

import com.jonahshader.systems.brain.NetworkBuilder
import com.jonahshader.systems.utils.Rand
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import kotlin.math.pow

class Eval(var creature: FoodCreature, var fitness: Float = 0f)

class FoodSim(networkBuilder: NetworkBuilder, populationSize: Int,
              private val samples: Int, val steps: Int, val dt: Float,
              private val rand: Random = Rand.randx) {
    private val population = mutableListOf<Eval>()

    private var bestLock = ReentrantLock()
    private var bestCreature: FoodCreature? = null

    private var running = false

    init {
        for (i in 0 until populationSize) {
            population += Eval(FoodCreature(networkBuilder))
        }
    }

    fun start() {
        running = true
        thread {
            while (running) {
                population.parallelStream().forEach { evaluate(it) }
                population.sortBy { it.fitness }
                val best = population.last()
                println("best fitness: ${best.fitness}")
                bestLock.withLock {
                    bestCreature = best.creature.cloneAndReset()
                }
                population.forEachIndexed { index, it ->
                    it.fitness = 0f
                    it.creature.reset()
                    if (it != best) {
                        it.creature.network.setParameters(best.creature.network.getParameters())
                        it.creature.network.mutateParameters((index.toFloat() / population.size).pow(2) * .25f)
                    }
                }


            }
        }
    }

    fun getBestCopy() : FoodCreature? {
        bestLock.withLock {
            return if (bestCreature != null) {
                bestCreature!!.cloneAndReset()
            } else {
                null
            }
        }
    }

    fun stop() {
        running = false
    }

    private fun evaluate(eval: Eval) {
        var totalFitness = 0f
        val foodGrid = FoodGrid(rand)
        for (i in 0 until samples) {
            eval.creature.reset()
            foodGrid.reset()
            for (j in 0 until steps) {
                eval.creature.update(foodGrid, dt)
            }
            totalFitness += eval.creature.totalFood
        }

        eval.fitness = totalFitness / samples
    }
}