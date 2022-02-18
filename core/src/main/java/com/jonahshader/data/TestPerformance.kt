package com.jonahshader.data

import com.jonahshader.systems.neuralnet.densecyclic.DenseCyclicNetwork
import com.jonahshader.systems.simulation.EvolutionStrategies
import com.jonahshader.systems.simulation.foodgrid.FoodCreature
import com.jonahshader.systems.utils.Rand
import kotlin.system.measureTimeMillis

object TestPerformance {
    fun testPerformance() {
        Rand.randx.setSeed(0)
        val sim = EvolutionStrategies(DenseCyclicNetwork.makeBuilder(60), FoodCreature.builder,
            100, 100, 600, 1/20f,
            algo = EvolutionStrategies.Algo.EsGDM, printFitness = false, rand = Rand.randx)
        val time = measureTimeMillis {
            sim.runIterations(10)
        }
        println("Performance (lower is better): $time")
    }
}