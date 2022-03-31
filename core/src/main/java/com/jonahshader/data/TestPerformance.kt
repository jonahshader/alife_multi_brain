package com.jonahshader.data

import com.jonahshader.systems.neuralnet.densecyclic.DenseCyclicNetwork
import com.jonahshader.systems.training.EvolutionStrategies
import com.jonahshader.systems.simulation.foodgrid.FoodCreature
import com.jonahshader.systems.utils.Rand
import kotlin.system.measureTimeMillis

object TestPerformance {
    fun testPerformance() {
        Rand.randx.setSeed(2513)
        val sim = EvolutionStrategies(DenseCyclicNetwork.makeBuilder(40), FoodCreature.builder,
            150, 100, 1/30f,
            algo = EvolutionStrategies.Algo.EsGDM, printFitness = false, rand = Rand.randx)
        val time = measureTimeMillis {
            sim.runIterations(4)
        }
        println("Performance (lower is better): $time")
    }
}