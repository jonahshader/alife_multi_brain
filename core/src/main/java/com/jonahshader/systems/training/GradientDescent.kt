package com.jonahshader.systems.training

import org.jetbrains.kotlinx.multik.api.math.cumSum
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.api.zeros
import org.jetbrains.kotlinx.multik.ndarray.data.*
import org.jetbrains.kotlinx.multik.ndarray.operations.*
import kotlin.math.max
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

// shape of params is param_count x pop_size, shape of evals is pop_size
// TODO: verify
fun computeGradientsFromParamEvals(params: NDArray<Float, D2>, evals: NDArray<Float, D1>) : NDArray<Float, D1> {
    val yMean = evals.average().toFloat()
    val gradients = mk.zeros<Float>(params.shape[0])
    (0 until params.shape[0]).forEach {
        val xVals = params[0, 0..params.shape[1]]
        val xMean = xVals.average().toFloat()
        val xn = xVals - xMean
        val yn = evals - yMean
        val gradient = (xn * yn).sum().toFloat() / ((xn * xn).sum().toFloat())
        gradients[it] = gradient
    }
    return gradients
}

// returns weights update
//fun gradientDescentUpdate(gradients: List<Float>, learningRate: Float) : List<Float> = gradients.map { it * learningRate }
//
//// returns weights update
//fun gradientDescentUpdateMomentum(gradients: List<Float>, pUpdate: List<Float>, learningRate: Float, momentum: Float) : List<Float> =
//    gradients.mapIndexed { index, it -> it * learningRate + momentum * pUpdate[index] }

// moment1 and moment2 will be changed, so expect that when calling this
fun sgdAdamUpdate(gradients: List<Float>, moment: MutableList<Float>, variance: MutableList<Float>, timestep: Int,
                  a: Float = 0.001f, b1: Float = 0.9f, b2: Float = 0.999f, e: Float = 1e-8f) : List<Float> {
    val gt = mk.ndarray(gradients)
    val mt = mk.ndarray(moment) * b1 + (1-b1) * gt
    val vt = mk.ndarray(variance) * b2 + (1-b2) * gt.map { it * it }
    // store back into moment1 and moment2
    mt.forEachIndexed{ index, fl -> moment[index] = fl }
    vt.forEachIndexed{ index, fl -> variance[index] = fl }
    // compute bias corrected first and second moment estimates
    val mth = mt/(1-b1.pow(timestep+1))
    val vth = vt/(1-b2.pow(timestep+1))
    // compute weight update
    return ((mth * a)/(vth.map{sqrt(it) + e})).toList()
}

fun sgdAdamUpdate(gradients: NDArray<Float, D1>, moment: Array<NDArray<Float, D1>>, variance: Array<NDArray<Float, D1>>, timestep: Int,
    a: Float = 0.001f, b1: Float = 0.9f, b2: Float = 0.999f, e: Float = 1e-8f) : NDArray<Float, D1> {
    val gt = gradients
    val mt = (moment[0] * b1) + (gt * (1-b1))
    val vt = (variance[0] * b2) + (gt * gt * (1-b2))
    // store back into moment and variance
    moment[0] = mt
    variance[0] = vt
    // compute bias corrected first and second moment estimates
    val mth: NDArray<Float, D1> = mt / (1f - b1.pow(timestep + 1))
    val vth: NDArray<Float, D1> = vt / (1f - b2.pow(timestep + 1))
    val vthsqrt = vth.map { sqrt(it) }
    return ((mth * a) / (vthsqrt + e))
}

//fun sgdAdaMaxUpdate(gradients: List<Float>,  moment: MutableList<Float>, infNorm: MutableList<Float>, timestep: Int,
//                    a: Float = 0.001f, b1: Float = 0.9f, b2: Float = 0.999f, e: Float = 1e-8f) : List<Float> {
//    val gt = mk.ndarray(gradients)
//    val mt = b1 * mk.ndarray(moment) + (1-b1)*gt
//    val utp = mk.ndarray(infNorm)
//    val ut = b2*utp.mapIndexed { index, fl ->  max(fl, gt[index]) }
//    mt.forEachIndexed{ index, fl -> moment[index] = fl }
//    ut.forEachIndexed{ index, fl -> infNorm[index] = fl }
//    val update = (a/(1-b1.pow(timestep + 1)))*(mt / ut)
//    return update.toList()
//}

// for now, input should be sorted and evals should be mapped to rank
//fun esGradientDescent(baseParams: List<Float>, paramsList: List<List<Float>>, evals: List<Float>, learningRate: Float) : List<Float> {
//    val grads = computeGradientsFromParamEvals(paramsList, evals)
//
//    val update = gradientDescentUpdate(grads, learningRate)
//    return baseParams.zip(update) { base, u -> base + u }
//}

