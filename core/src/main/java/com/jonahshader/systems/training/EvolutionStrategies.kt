package com.jonahshader.systems.training

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Disposable
import com.jonahshader.systems.creatureparts.ReinforcementTask
import com.jonahshader.systems.creatureparts.TaskBuilder
import com.jonahshader.systems.neuralnet.NetworkBuilder
import com.jonahshader.systems.utils.Rand
import org.jetbrains.kotlinx.multik.api.*
import org.jetbrains.kotlinx.multik.ndarray.data.D1
import org.jetbrains.kotlinx.multik.ndarray.data.D2
import org.jetbrains.kotlinx.multik.ndarray.data.NDArray
import org.jetbrains.kotlinx.multik.ndarray.operations.append
import org.jetbrains.kotlinx.multik.ndarray.operations.plus
import org.jetbrains.kotlinx.multik.ndarray.operations.toList
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

class Eval(var creature: ReinforcementTask, var fitness: Float = 0f)

class EvolutionStrategies(networkBuilder: NetworkBuilder, creatureBuilder: TaskBuilder, populationSize: Int,
                          private val samples: Int, val dt: Float,
                          private val rand: Random = Rand.randx, private val algo: Algo = Algo.EsAdam,
                          private val logging: Boolean = false, private val printFitness: Boolean = false) : Disposable {
    enum class Algo {
        EsPickBest,
        EsGD,
        EsGDM,
        EsAdam
    }


    private val population = mutableListOf<Eval>()

    private var bestLock = ReentrantLock()
    private var bestCreature: ReinforcementTask? = null

    private var gdCreatureCurrent: Eval? = null
    private var pUpdate: List<Float>
    private val moment1: MutableList<Float>
    private val moment2: MutableList<Float>
    private val moment1v: Array<NDArray<Float, D1>>
    private val moment2v: Array<NDArray<Float, D1>>
    private var currentIteration = 0

    private var running = false
    private val fitnessLog = mutableListOf<Float>()
    private val meanFitnessCallbacks = mutableListOf<(Vector2) -> Unit>()
    private val centerFitnessCallbacks = mutableListOf<(Vector2) -> Unit>()
    private val maxFitnessCallbacks = mutableListOf<(Vector2) -> Unit>()
    private val minFitnessCallbacks = mutableListOf<(Vector2) -> Unit>()

    private var disposeQueued = false
    private var computeThreadRunning = false

    init {
        for (i in 0 until populationSize) {
            population += Eval(creatureBuilder(networkBuilder))
        }

        // initialize pUpdate for gd with momentum
        val size = population[0].creature.network.getParameters().size
        pUpdate = List(size) { 0f }
        moment1 = MutableList(size) { 0f }
        moment2 = MutableList(size) { 0f }
        moment1v = arrayOf(mk.zeros(size))
        moment2v = arrayOf(mk.zeros(size))
    }

    private fun runAlgo() {
        when (algo) {
//            Algo.EsPickBest -> esPickBestAlgo()
//            Algo.EsGD -> esGdAlgo()
//            Algo.EsAdam -> esGdAdamAlgo()
//            Algo.EsAdam
            else -> esGdAdamVecAlgo()
        }

        currentIteration++
    }

    private fun logFitness(centerFitness: Float, population: List<Eval>) {
        if (logging)
            fitnessLog += centerFitness
        if (printFitness)
            println("current fitness: $centerFitness")
        val fitnesses = population.map{it.fitness}
        val meanFitness = fitnesses.average().toFloat()
        val maxFitness = fitnesses.maxOrNull()!!
        val minFitness = fitnesses.minOrNull()!!
        centerFitnessCallbacks.forEach { it(Vector2(currentIteration.toFloat(), centerFitness)) }
        meanFitnessCallbacks.forEach { it(Vector2(currentIteration.toFloat(), meanFitness)) }
        maxFitnessCallbacks.forEach { it(Vector2(currentIteration.toFloat(), maxFitness)) }
        minFitnessCallbacks.forEach { it(Vector2(currentIteration.toFloat(), minFitness)) }
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

    //TODO: pull out optimizer from algo
//    private fun esGdAdamAlgo() {
//        if (population[0].creature.network.multithreadable)
//            population.parallelStream().forEach { evaluateAverage(it) }
//        else
//            population.forEach { evaluateAverage(it) }
//        population.sortBy { it.fitness }
////        println(population.map { it.fitness })
//        if (gdCreatureCurrent == null) {
//            gdCreatureCurrent = population[(population.size*.75f).toInt()]
////            gdCreatureCurrent = population.last()
//            var checkingNotNanIndex = population.size - 1
//            while (gdCreatureCurrent!!.fitness.isNaN()) {
//                gdCreatureCurrent = population[--checkingNotNanIndex]
//            }
//        }
//
////        logFitness(gdCreatureCurrent!!.fitness)
//        logFitness(gdCreatureCurrent!!.fitness, population)
//        bestLock.withLock {
//            bestCreature?.network?.dispose()
//            bestCreature = gdCreatureCurrent!!.creature.cloneAndReset()
//        }
//
//        population.forEachIndexed {index, it ->
//            // reassign fitness to a value proportional to its rank
//            it.fitness = (index.toFloat() / (population.size - 1)) - .5f
//        }
//
//        // build lists for algo function
////        val paramsList = mutableListOf<List<Float>>()
////        population.forEach {
////            paramsList += it.creature.network.getParameters()
////        }
//        // build list for algo function
//        val paramsList = population.map { it.creature.network.getParameters() }
//
////        val evals = mutableListOf<Float>()
////        population.forEach {
////            evals += it.fitness
////        }
//        val evals = population.map { it.fitness }
//
//        val grads = computeGradientsFromParamEvals(paramsList, evals)
//        val mutationRate = 0.01f
////        val update = gradientDescentUpdateMomentum(grads, pUpdate, 0.01f * mutationRate, 0.92f)
////        pUpdate = update
//        val update = sgdAdamUpdate(grads, moment1, moment2, currentIteration,a = mutationRate * 1f)
////        val update = sgdAdaMaxUpdate(grads, moment1, moment2, currentIteration, a = mutationRate * 1f)
//        println(moment1)
//        println(moment2)
//        val medianParams = gdCreatureCurrent!!.creature.network.getParameters()
////        val medianParams = population[population.size/2].creature.network.getParameters()
////        gdCreatureCurrent = population[population.size/2]
//        val newParams = medianParams.zip(update) { base, u -> base + u }
//
//        population.parallelStream().forEach {
//            it.fitness = 0f
//            it.creature.network.setParameters(newParams)
//            if (it != gdCreatureCurrent!!) {
////                it.creature.network.mutateParameters((index.toFloat() / (population.size-1)).pow(2) * .25f)
//                // TODO: scale mutation based on update per parameter
//                it.creature.network.mutateParameters(mutationRate)
////                it.creature.network.mutateParameters()
////                it.creature.network.mutateParameters(.1f)
//            }
//        }
//    }

    private fun esGdAdamVecAlgo() {
        if (population[0].creature.network.multithreadable)
            population.parallelStream().forEach { evaluateAverage(it) }
        else
            population.forEach { evaluateAverage(it) }
        population.sortBy { it.fitness }
//        println(population.map { it.fitness })
        if (gdCreatureCurrent == null) {
            gdCreatureCurrent = population[(population.size*.75f).toInt()]
//            gdCreatureCurrent = population.last()
            var checkingNotNanIndex = population.size - 1
            while (gdCreatureCurrent!!.fitness.isNaN()) {
                gdCreatureCurrent = population[--checkingNotNanIndex]
            }
        }

//        logFitness(gdCreatureCurrent!!.fitness)
        logFitness(gdCreatureCurrent!!.fitness, population)
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
//        val paramsList = population.map { it.creature.network.getParameters() }
//        val paramsList = population.map {
//            val params = it.creature.network.getParameters()
//            mk.ndarray<Float, D2>(params, intArrayOf(params.size, 1))
//        }.reduce {
//            acc, ndArray ->  acc.append(ndArray, 1)
//        }

        var paramCount = 0
        val paramsList = population.map { val p = it.creature.network.getParameters()
            paramCount = p.size
            p
        }.reduce { acc, ndArray -> acc.append(ndArray) }.reshape(paramCount, population.size)
//        val paramsList = paramsListTemp[0]
//        (1 until paramsListTemp.size).forEach {
//            paramsList.app
//        }

//        val evals = mutableListOf<Float>()
//        population.forEach {
//            evals += it.fitness
//        }
//        val evals = population.map { it.fitness }
        val evals = mk.ndarray<Float, D1>(population.map { it.fitness }, intArrayOf(population.size) )

        val grads = computeGradientsFromParamEvals(paramsList, evals)
        val mutationRate = 0.01f
//        val update = gradientDescentUpdateMomentum(grads, pUpdate, 0.01f * mutationRate, 0.92f)
//        pUpdate = update
        val update = sgdAdamUpdate(grads, moment1v, moment2v, currentIteration,a = mutationRate * 1f)
//        val update = sgdAdaMaxUpdate(grads, moment1, moment2, currentIteration, a = mutationRate * 1f)
        println(moment1v)
        println(moment2v)
//        val medianParams = gdCreatureCurrent!!.creature.network.getParameters()
//        val medianParamsFArray = gdCreatureCurrent!!.creature.network.getParameters()
        val medianParams = gdCreatureCurrent!!.creature.network.getParameters()

//        val medianParams = population[population.size/2].creature.network.getParameters()
//        gdCreatureCurrent = population[population.size/2]
//        val newParams = medianParams.zip(update) { base, u -> base + u }
        val newParams = update + medianParams

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

//    private fun esGdAlgo() {
//        if (population[0].creature.network.multithreadable)
//            population.parallelStream().forEach { evaluateAverage(it) }
//        else
//            population.forEach { evaluateAverage(it) }
//        population.sortBy { it.fitness }
//        if (gdCreatureCurrent == null) {
//            gdCreatureCurrent = population[population.size/2]
//        }
//
//        logFitness(gdCreatureCurrent!!.fitness, population)
//        bestLock.withLock {
//            bestCreature?.network?.dispose()
//            bestCreature = gdCreatureCurrent!!.creature.cloneAndReset()
//        }
//
//        population.forEachIndexed {index, it ->
//            // reassign fitness to a value proportional to its rank
//            it.fitness = (index.toFloat() / (population.size - 1)) - .5f
//        }
//
//        // build lists for algo function
//        val paramsList = mutableListOf<List<Float>>()
//        population.forEach {
//            paramsList += it.creature.network.getParameters()
//        }
//
//        val evals = mutableListOf<Float>()
//        population.forEach {
//            evals += it.fitness
//        }
//
//        val newParams = esGradientDescent(gdCreatureCurrent!!.creature.network.getParameters(), paramsList, evals, 0.1f)
//        population.forEachIndexed { index, it ->
//            it.fitness = 0f
//            it.creature.network.setParameters(newParams)
//            if (it != gdCreatureCurrent!!) {
//                it.creature.network.mutateParameters((index.toFloat() / (population.size-1)).pow(2) * .25f)
//            }
//        }
//    }
//
//    private fun esPickBestAlgo() {
//        if (population[0].creature.network.multithreadable)
//            population.parallelStream().forEach { evaluateAverage(it) }
//        else
//            population.forEach { evaluateAverage(it) }
//        population.sortBy { it.fitness }
//        val best = population.last()
//        logFitness(best.fitness, population)
//        bestLock.withLock {
//            bestCreature?.network?.dispose()
//            bestCreature = best.creature.cloneAndReset()
//        }
//        population.forEachIndexed { index, it ->
//            it.fitness = 0f
//            if (it != best) {
//                it.creature.network.setParameters(best.creature.network.getParameters())
//                it.creature.network.mutateParameters((index.toFloat() / (population.size-1)).pow(2) * .25f)
//            }
//        }
//    }

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
            while (!eval.creature.done())
                eval.creature.update(dt)
            totalFitness += eval.creature.getFitness()
        }

        eval.fitness = totalFitness / samples
    }

    private fun evaluateMedian(eval: Eval) {
        val fitness = mutableListOf<Float>()
        for (i in 0 until samples) {
            eval.creature.restartAndRandomize()
            while (!eval.creature.done())
                eval.creature.update(dt)
            fitness += eval.creature.getFitness()
        }
        fitness.sort()
        eval.fitness = fitness[fitness.size/2]
    }

    fun addMeanFitnessCallback(fitnessCallback: (Vector2) -> Unit) {
        meanFitnessCallbacks += fitnessCallback
    }

    fun addCenterFitnessCallback(fitnessCallback: (Vector2) -> Unit) {
        centerFitnessCallbacks += fitnessCallback
    }

    fun addMaxFitnessCallback(fitnessCallback: (Vector2) -> Unit) {
        maxFitnessCallbacks += fitnessCallback
    }

    fun addMinFitnessCallback(fitnessCallback: (Vector2) -> Unit) {
        minFitnessCallbacks += fitnessCallback
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