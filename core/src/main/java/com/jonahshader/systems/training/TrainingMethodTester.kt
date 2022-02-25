package com.jonahshader.systems.training

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.jonahshader.systems.neuralnet.densecyclic.DenseCyclicNetwork
import com.jonahshader.systems.simulation.foodgrid.FoodCreature
import kotlin.system.measureTimeMillis

fun main() {
    val hiddenSize = 35
    val popSize = 100
    val samples = 10
    val steps = 500
    val dt = 1/20f
    val sim1 = EvolutionStrategies(DenseCyclicNetwork.makeBuilder(hiddenSize), FoodCreature.builder, popSize, samples, steps, dt, algo = EvolutionStrategies.Algo.EsPickBest, logging = true)
    val sim2 = EvolutionStrategies(DenseCyclicNetwork.makeBuilder(hiddenSize), FoodCreature.builder, popSize, samples, steps, dt, algo = EvolutionStrategies.Algo.EsGD, logging = true)
    val sim3 = EvolutionStrategies(DenseCyclicNetwork.makeBuilder(hiddenSize), FoodCreature.builder, popSize, samples, steps, dt, algo = EvolutionStrategies.Algo.EsGDM, logging = true)

    val iterations = 30
    val sim1time = measureTimeMillis { sim1.runIterations(iterations) }
    println("EsPickBest time: $sim1time")
    val sim2time = measureTimeMillis { sim2.runIterations(iterations) }
    println("EsGD time: $sim2time")
    val sim3time = measureTimeMillis { sim3.runIterations(iterations) }
    println("EsGDM time: $sim3time")


//    csvWriter().writeAll
    csvWriter().writeAll(listOf(sim1.getLog(), sim2.getLog(), sim3.getLog()), "data.csv")

    csvWriter().open("data.csv") {
        writeRow("hiddenSize $hiddenSize, popSize $popSize, samples $samples, steps $steps, dt $dt")
        writeRow(sim1time, sim2time, sim3time)
        writeRow("EsPickBest", "EsGD", "EsGDM")
        for (i in sim1.getLog().indices) {
            writeRow(sim1.getLog()[i], sim2.getLog()[i], sim3.getLog()[i])
        }
    }

}