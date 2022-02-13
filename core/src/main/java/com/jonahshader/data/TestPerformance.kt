package com.jonahshader.data

import com.jonahshader.systems.neuralnet.makeDenseNetworkBuilder
import com.jonahshader.systems.simulation.foodgrid.FoodSim
import com.jonahshader.systems.utils.Rand
import kotlin.system.measureTimeMillis

object TestPerformance {
    fun testPerformance() {
        Rand.randx.setSeed(0)
        val sim = FoodSim(makeDenseNetworkBuilder(60), 100, 100, 600, 1/20f, algo = FoodSim.Algo.EsGDM, printFitness = false, rand = Rand.randx)
        val time = measureTimeMillis {
            sim.runIterations(10)
        }
        println("Performance (lower is better): $time")
    }
}