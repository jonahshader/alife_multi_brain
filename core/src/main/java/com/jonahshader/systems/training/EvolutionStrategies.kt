package com.jonahshader.systems.training

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Disposable
import com.jonahshader.systems.creatureparts.ReinforcementTask
import com.jonahshader.systems.creatureparts.CreatureBuilder
import com.jonahshader.systems.neuralnet.NetworkBuilder
import com.jonahshader.systems.utils.Rand
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import kotlin.math.pow

class Eval(var creature: ReinforcementTask, var fitness: Float = 0f)

class EvolutionStrategies(networkBuilder: NetworkBuilder, creatureBuilder: CreatureBuilder, populationSize: Int,
                          private val samples: Int, val steps: Int, val dt: Float,
                          private val rand: Random = Rand.randx, private val algo: Algo = Algo.EsPickBest,
                          private val logging: Boolean = false, private val printFitness: Boolean = false) : Disposable {
    enum class Algo {
        EsPickBest,
        EsGD,
        EsGDM
    }


    private val population = mutableListOf<Eval>()

    private var bestLock = ReentrantLock()
    private var bestCreature: ReinforcementTask? = null

    private var gdCreatureCurrent: Eval? = null
    private var pUpdate: List<Float>
    private var currentIteration = 0

    private var running = false
    private val fitnessLog = mutableListOf<Float>()
    private val fitnessCallbacks = mutableListOf<(Vector2) -> Unit>()

    private var disposeQueued = false
    private var computeThreadRunning = false

    init {
        for (i in 0 until populationSize) {
            population += Eval(creatureBuilder(networkBuilder))
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

        currentIteration++
    }

    private fun logFitness(fitness: Float) {
        if (logging)
            fitnessLog += fitness
        if (printFitness)
            println("current fitness: $fitness")
        fitnessCallbacks.forEach { it(Vector2(currentIteration.toFloat(), fitness)) }
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
                computeThreadRunning = true
                while (running) {
                    runAlgo()
                }
                if (disposeQueued) {
                    population.forEach {
                        it.creature.network.dispose()
                    }
                }
                computeThreadRunning = false
            }
        }
    }

    private fun esGdMovementAlgo() {
        if (population[0].creature.network.multithreadable)
            population.parallelStream().forEach { evaluateAverage(it) }
        else
            population.forEach { evaluateAverage(it) }
        population.sortBy { it.fitness }
        println(population.map { it.fitness })
        if (gdCreatureCurrent == null) {
//            gdCreatureCurrent = population[(population.size*.80f).toInt()]
            gdCreatureCurrent = population.last()
            var checkingNotNanIndex = population.size - 1
            while (gdCreatureCurrent!!.fitness.isNaN()) {
                gdCreatureCurrent = population[--checkingNotNanIndex]
            }
        }

        logFitness(gdCreatureCurrent!!.fitness)
        bestLock.withLock {
            bestCreature?.network?.dispose()
            bestCreature = gdCreatureCurrent!!.creature.cloneAndReset()
        }

        population.forEachIndexed {index, it ->
            // reassign fitness to a value proportional to its rank
            it.fitness = (index.toFloat() / (population.size - 1)) - .5f
        }

        // build lists for algo function
//        val paramsList = mutableListOf<List<Float>>()
//        population.forEach {
//            paramsList += it.creature.network.getParameters()
//        }
        // build list for algo function
        val paramsList = population.map { it.creature.network.getParameters() }

//        val evals = mutableListOf<Float>()
//        population.forEach {
//            evals += it.fitness
//        }
        val evals = population.map { it.fitness }

        val grads = computeGradientsFromParamEvals(paramsList, evals)
        val mutationRate = 0.01f
        val update = gradientDescentUpdateMomentum(grads, pUpdate, 0.01f * mutationRate, 0.92f)
//        val update = gradientDescentUpdateMomentum(grads, pUpdate, 0.00f, 0.9f)
        pUpdate = update
        val medianParams = gdCreatureCurrent!!.creature.network.getParameters()
//        val medianParams = population[population.size/2].creature.network.getParameters()
//        gdCreatureCurrent = population[population.size/2]
        val newParams = medianParams.zip(update) { base, u -> base + u }

        population.parallelStream().forEach {
            it.fitness = 0f
            it.creature.network.setParameters(newParams)
            if (it != gdCreatureCurrent!!) {
//                it.creature.network.mutateParameters((index.toFloat() / (population.size-1)).pow(2) * .25f)
                // TODO: scale mutation based on update per parameter
                it.creature.network.mutateParameters(mutationRate)
//                it.creature.network.mutateParameters()
//                it.creature.network.mutateParameters(.1f)
            }
        }
    }

    private fun esGdAlgo() {
        if (population[0].creature.network.multithreadable)
            population.parallelStream().forEach { evaluateAverage(it) }
        else
            population.forEach { evaluateAverage(it) }
        population.sortBy { it.fitness }
        if (gdCreatureCurrent == null) {
            gdCreatureCurrent = population[population.size/2]
        }

        logFitness(gdCreatureCurrent!!.fitness)
        bestLock.withLock {
            bestCreature?.network?.dispose()
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
            it.creature.network.setParameters(newParams)
            if (it != gdCreatureCurrent!!) {
                it.creature.network.mutateParameters((index.toFloat() / (population.size-1)).pow(2) * .25f)
            }
        }
    }

    private fun esPickBestAlgo() {
        if (population[0].creature.network.multithreadable)
            population.parallelStream().forEach { evaluateAverage(it) }
        else
            population.forEach { evaluateAverage(it) }
        population.sortBy { it.fitness }
        val best = population.last()
        logFitness(best.fitness)
        bestLock.withLock {
            bestCreature?.network?.dispose()
            bestCreature = best.creature.cloneAndReset()
        }
        population.forEachIndexed { index, it ->
            it.fitness = 0f
            if (it != best) {
                it.creature.network.setParameters(best.creature.network.getParameters())
                it.creature.network.mutateParameters((index.toFloat() / (population.size-1)).pow(2) * .25f)
            }
        }
    }

    /**
     * whoever gets this must dispose it after using it (right now thats just the network)
     */
    fun getBestCopy() : ReinforcementTask? {
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
        for (i in 0 until samples) {
            eval.creature.restartAndRandomize()
            for (j in 0 until steps) {
                eval.creature.update(dt)
            }
            totalFitness += eval.creature.getFitness()
        }

        eval.fitness = totalFitness / samples
    }

    private fun evaluateMedian(eval: Eval) {
        val fitness = mutableListOf<Float>()
        for (i in 0 until samples) {
            eval.creature.restartAndRandomize()
            for (j in 0 until steps) {
                eval.creature.update(dt)
            }
            fitness += eval.creature.getFitness()
        }
        fitness.sort()
        eval.fitness = fitness[fitness.size/2]
    }

    fun addFitnessCallback(fitnessCallback: (Vector2) -> Unit) {
        fitnessCallbacks += fitnessCallback
    }

    override fun dispose() {
        if (!computeThreadRunning) {
            population.forEach {
                it.creature.network.dispose()
            }
        } else {
            disposeQueued = true
        }
    }
}