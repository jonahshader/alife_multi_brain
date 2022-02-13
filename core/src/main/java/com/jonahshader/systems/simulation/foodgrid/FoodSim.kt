package com.jonahshader.systems.simulation.foodgrid

import com.jonahshader.systems.neuralnet.NetworkBuilder
import com.jonahshader.systems.training.computeGradientsFromParamEvals
import com.jonahshader.systems.training.esGradientDescent
import com.jonahshader.systems.training.gradientDescentUpdateMomentum
import com.jonahshader.systems.utils.Rand
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import kotlin.math.pow

class Eval(var creature: FoodCreature, var fitness: Float = 0f)

class FoodSim(networkBuilder: NetworkBuilder, populationSize: Int,
              private val samples: Int, val steps: Int, val dt: Float,
              private val rand: Random = Rand.randx, private val algo: Algo = Algo.EsPickBest,
              private val logging: Boolean = false, private val printFitness: Boolean = false) {
    enum class Algo {
        EsPickBest,
        EsGD,
        EsGDM
    }


    private val population = mutableListOf<Eval>()

    private var bestLock = ReentrantLock()
    private var bestCreature: FoodCreature? = null

    private var gdCreatureCurrent: Eval? = null
    private var pUpdate: List<Float>

    private var running = false
    private val fitnessLog = mutableListOf<Float>()

    init {
        for (i in 0 until populationSize) {
            population += Eval(FoodCreature(networkBuilder))
        }

        // initialize pUpdate for gd with momentum
        pUpdate = List(population[0].creature.network.getParameters().size) { 0f }
    }

    private fun runAlgo() {
        when (algo) {
            Algo.EsPickBest -> esPickBestAlgo()
            Algo.EsGD -> esGdAlgo()
            Algo.EsGDM -> esGdMovementAlgo()
        }
    }

    private fun logFitness(fitness: Float) {
        if (logging)
            fitnessLog += fitness
        if (printFitness)
            println("current fitness: $fitness")
    }

    fun getLog() : List<Float> = fitnessLog

    fun runIterations(iterations: Int) {
        for (i in 0 until iterations) {
            runAlgo()
        }
    }

    fun start() {
        if (!running) {
            running = true
            thread {
                while (running) {
                    runAlgo()
                }
            }
        }
    }

    private fun esGdMovementAlgo() {
        population.parallelStream().forEach { evaluateAverage(it) }
        population.sortBy { it.fitness }
        if (gdCreatureCurrent == null) {
//            gdCreatureCurrent = population[(population.size*.80f).toInt()]
            gdCreatureCurrent = population.last()
        }

        logFitness(gdCreatureCurrent!!.fitness)
        bestLock.withLock {
            bestCreature = gdCreatureCurrent!!.creature.cloneAndReset()
        }

        population.forEachIndexed {index, it ->
            // reassign fitness to a value proportional to its rank
            it.fitness = (index.toFloat() / (population.size - 1)) - .5f
        }

        // build lists for algo function
        val paramsList = mutableListOf<List<Float>>()
        population.forEach {
            paramsList += it.creature.network.getParameters()
        }

        val evals = mutableListOf<Float>()
        population.forEach {
            evals += it.fitness
        }

        val grads = computeGradientsFromParamEvals(paramsList, evals)
        val update = gradientDescentUpdateMomentum(grads, pUpdate, 0.05f, 0.9f)
//        val update = gradientDescentUpdateMomentum(grads, pUpdate, 0.00f, 0.9f)
        pUpdate = update
        val medianParams = gdCreatureCurrent!!.creature.network.getParameters()
//        val medianParams = population[population.size/2].creature.network.getParameters()
//        gdCreatureCurrent = population[population.size/2]
        val newParams = medianParams.zip(update) { base, u -> base + u }

        population.forEachIndexed { index, it ->
            it.fitness = 0f
            it.creature.reset()
            it.creature.network.setParameters(newParams)
            if (it != gdCreatureCurrent!!) {
                it.creature.network.mutateParameters((index.toFloat() / (population.size-1)).pow(2) * .25f)
//                it.creature.network.mutateParameters(.1f)
            }
        }
    }

    private fun esGdAlgo() {
        population.parallelStream().forEach { evaluateAverage(it) }
        population.sortBy { it.fitness }
        if (gdCreatureCurrent == null) {
            gdCreatureCurrent = population[population.size/2]
        }

        logFitness(gdCreatureCurrent!!.fitness)
        bestLock.withLock {
            bestCreature = gdCreatureCurrent!!.creature.cloneAndReset()
        }

        population.forEachIndexed {index, it ->
            // reassign fitness to a value proportional to its rank
            it.fitness = (index.toFloat() / (population.size - 1)) - .5f
        }

        // build lists for algo function
        val paramsList = mutableListOf<List<Float>>()
        population.forEach {
            paramsList += it.creature.network.getParameters()
        }

        val evals = mutableListOf<Float>()
        population.forEach {
            evals += it.fitness
        }

        val newParams = esGradientDescent(gdCreatureCurrent!!.creature.network.getParameters(), paramsList, evals, 0.1f)
        population.forEachIndexed { index, it ->
            it.fitness = 0f
            it.creature.reset()
            it.creature.network.setParameters(newParams)
            if (it != gdCreatureCurrent!!) {
                it.creature.network.mutateParameters((index.toFloat() / (population.size-1)).pow(2) * .25f)
            }
        }
    }

    private fun esPickBestAlgo() {
        population.parallelStream().forEach { evaluateAverage(it) }
        population.sortBy { it.fitness }
        val best = population.last()
        logFitness(best.fitness)
        bestLock.withLock {
            bestCreature = best.creature.cloneAndReset()
        }
        population.forEachIndexed { index, it ->
            it.fitness = 0f
            it.creature.reset()
            if (it != best) {
                it.creature.network.setParameters(best.creature.network.getParameters())
                it.creature.network.mutateParameters((index.toFloat() / (population.size-1)).pow(2) * .25f)
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

    private fun evaluateAverage(eval: Eval) {
        var totalFitness = 0f
        val foodGrid = FoodGrid(rand)
        for (i in 0 until samples) {
            eval.creature.reset()
            foodGrid.reset()
//            foodGrid.setSeed(i)
            for (j in 0 until steps) {
                eval.creature.update(foodGrid, dt)
            }
            totalFitness += eval.creature.totalFood
        }

        eval.fitness = totalFitness / samples
    }

    private fun evaluateMedian(eval: Eval) {
        val fitness = mutableListOf<Float>()
        val foodGrid = FoodGrid(rand)
        for (i in 0 until samples) {
            eval.creature.reset()
            foodGrid.reset()
//            foodGrid.setSeed(i)
            for (j in 0 until steps) {
                eval.creature.update(foodGrid, dt)
            }
            fitness += eval.creature.totalFood
        }
        fitness.sort()
        eval.fitness = fitness[fitness.size/2]
    }
}