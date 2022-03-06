package com.jonahshader.systems.training

import org.jetbrains.kotlinx.multik.api.linalg.dot
import org.jetbrains.kotlinx.multik.api.math.exp
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.operations.*
import kotlin.math.pow
import kotlin.math.sqrt


//TODO: maybe make an alternative version of this that multiplies each partial derivative by the confidence of the linear regression that produced it
// this could make gradient descent only act on parameters that have a strong partial derivative and strong confidence, instead of
// treating noisy samples the same as the confident ones. might improve training performance
fun computeGradientsFromParamEvals(paramsList: List<List<Float>>, evals: List<Float>) : MutableList<Float> {
    // compute linear regression on every parameter with respect to fitness

    // think of an individual param as x, and the fitness as y

    // compute mean params

    val xMeans = paramsList[0].indices.map { singleParamIndex ->
        paramsList.fold(0f) { acc, params -> acc + params[singleParamIndex] / paramsList.size }
    }

    val yMean = evals.average().toFloat()

    val gradients = paramsList[0].indices.map { singleParamIndex ->
        paramsList.foldIndexed(0f) { pListIndex, acc, params -> acc + (params[singleParamIndex]-xMeans[singleParamIndex]) * (evals[pListIndex]-yMean) } /
                paramsList.fold(0f) { acc, params -> acc + (params[singleParamIndex]-xMeans[singleParamIndex]).pow(2)}
    }
    return gradients.toMutableList()
}

// returns weights update
fun gradientDescentUpdate(gradients: List<Float>, learningRate: Float) : List<Float> = gradients.map { it * learningRate }

// returns weights update
fun gradientDescentUpdateMomentum(gradients: List<Float>, pUpdate: List<Float>, learningRate: Float, momentum: Float) : List<Float> =
    gradients.mapIndexed { index, it -> it * learningRate + momentum * pUpdate[index] }

// moment1 and moment2 will be changed, so expect that when calling this
fun sgdAdamUpdate(gradients: List<Float>, moment1: MutableList<Float>, moment2: MutableList<Float>, timestep: Int,
                  a: Float = 0.001f, b1: Float = 0.9f, b2: Float = 0.999f, e: Float = 1e-8f) : List<Float> {
    val gt = mk.ndarray(gradients)
    val mt = mk.ndarray(moment1) * b1 + gt * (1-b1) * gt
    val vt = mk.ndarray(moment2) * b2 + (1-b2) * gt.map { it * it }
    // store back into moment1 and moment2
    mt.forEachIndexed{ index, fl -> moment1[index] = fl }
    vt.forEachIndexed{ index, fl -> moment2[index] = fl }
    // compute bias corrected first and second moment estimates
    val mth = mt/(1-b1.pow(timestep+1))
    val vth = vt/(1-b2.pow(timestep+1))
    // compute weight update
    return ((mth * -a)/(vth.map{sqrt(it) + e})).toList()

}

//fun gradientDescentUpdateAdam(gradients: List<Float>, firstMoment: MutableList<Float>, secondMoment: MutableList<Float>, timestep: Int) : List<Float> {
//
//}

// for now, input should be sorted and evals should be mapped to rank
fun esGradientDescent(baseParams: List<Float>, paramsList: List<List<Float>>, evals: List<Float>, learningRate: Float) : List<Float> {
    val grads = computeGradientsFromParamEvals(paramsList, evals)

    val update = gradientDescentUpdate(grads, learningRate)
    return baseParams.zip(update) { base, u -> base + u }
}

