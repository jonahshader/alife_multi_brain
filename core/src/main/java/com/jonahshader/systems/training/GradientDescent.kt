package com.jonahshader.systems.training

import com.jonahshader.systems.math.plus
import com.jonahshader.systems.math.times
import com.jonahshader.systems.math.div
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.operations.*
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
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
        paramsList.foldIndexed(0f) {
                pListIndex, acc, params -> acc + (params[singleParamIndex]-xMeans[singleParamIndex]) * (evals[pListIndex]-yMean)
        } / paramsList.fold(0f) { acc, params -> acc + (params[singleParamIndex]-xMeans[singleParamIndex]).pow(2)}
    }
    return gradients.toMutableList()
}

// dim of params is param_count x pop_size, dim of evals is 1 x pop_size
fun computeGradientsFromParamEvals(params: INDArray, evals: INDArray) : INDArray {

    val yVals = evals
    val yMean = yVals.mean(0).getFloat(0)
    val gradients = Nd4j.zeros(params.rows())
    (0 until params.rows()).forEach {
        val xVals = params.getRow(it.toLong()) // TODO: was transposed
        val xMean = xVals.mean(0) // should be scalar

        val xn = xVals.sub(xMean.getFloat(0))
        val yn = yVals.sub(yMean)
        println("xn shape: ${xn.shape().toList()}")
        println("yn shape: ${yn.shape().toList()}")
        println("params shape: ${params.shape().toList()}")
        println("params rows: ${params.rows()}")
        val gradient = xn.mulColumnVector(yn).cumsum(0).getFloat(0) / xn.mulColumnVector(xn).cumsum(0).getFloat(0)
        gradients.putScalar(it.toLong(), gradient)
    }
    return gradients
}

// returns weights update
fun gradientDescentUpdate(gradients: List<Float>, learningRate: Float) : List<Float> = gradients.map { it * learningRate }

fun gradientDescentUpdate(gradients: INDArray, learningRate: Float) : INDArray = gradients * learningRate

// returns weights update
fun gradientDescentUpdateMomentum(gradients: List<Float>, pUpdate: List<Float>, learningRate: Float, momentum: Float) : List<Float> =
    gradients.mapIndexed { index, it -> it * learningRate + momentum * pUpdate[index] }

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

fun sgdAdamUpdate(gradients: INDArray, moment: INDArray, variance: INDArray, timestep: Int,
                  a: Float = 0.001f, b1: Float = 0.9f, b2: Float = 0.999f, e: Float = 1e-8f) : INDArray {
//    val gt = gradients
//    val mt = moment * b1 + (1f-b1) * gt
//    val vt = variance * b2 + (1f-b2) * gt.mul(gt) // TODO: mul or mulColumnVector
//    // store back into moment and variance
//    moment.subi(moment) // TODO: this sucks
//    variance.subi(variance)
//    moment.addi(mt)
//    variance.addi(vt)
//    // compute bias corrected first and second moment estimates
//    val mth: INDArray = mt / (1f - b1.pow(timestep + 1))
//    val vth: INDArray = vt / (1-b2.pow(timestep+1))
//    return ((mth * a)/(Nd4j.math.pow(vth, .5) + e))

    val gt = gradients
    val mt = moment.mul(b1).add(gt.mul(1f-b1))
    val vt = variance.mul(b2) + gt.mul(gt).mul(1f-b2) // TODO: mul or mulColumnVector
    // store back into moment and variance
    println("moment shape: ${moment.shape().toList()}")
    println("mt shape: ${mt.shape().toList()}")
    moment.subi(moment) // TODO: this sucks
    variance.subi(variance)
    moment.addi(mt)
    variance.addi(vt)
    // compute bias corrected first and second moment estimates
    val mth: INDArray = mt / (1f - b1.pow(timestep + 1))
    val vth: INDArray = vt / (1-b2.pow(timestep+1))
    return ((mth * a)/(Nd4j.math.pow(vth, .5) + e))

}

fun sgdAdaMaxUpdate(gradients: List<Float>,  moment: MutableList<Float>, infNorm: MutableList<Float>, timestep: Int,
                    a: Float = 0.001f, b1: Float = 0.9f, b2: Float = 0.999f, e: Float = 1e-8f) : List<Float> {
    val gt = mk.ndarray(gradients)
    val mt = b1 * mk.ndarray(moment) + (1-b1)*gt
    val utp = mk.ndarray(infNorm)
    val ut = b2*utp.mapIndexed { index, fl ->  max(fl, gt[index]) }
    mt.forEachIndexed{ index, fl -> moment[index] = fl }
    ut.forEachIndexed{ index, fl -> infNorm[index] = fl }
    val update = (a/(1-b1.pow(timestep + 1)))*(mt / ut)
    return update.toList()
}


// for now, input should be sorted and evals should be mapped to rank
fun esGradientDescent(baseParams: List<Float>, paramsList: List<List<Float>>, evals: List<Float>, learningRate: Float) : List<Float> {
    val grads = computeGradientsFromParamEvals(paramsList, evals)

    val update = gradientDescentUpdate(grads, learningRate)
    return baseParams.zip(update) { base, u -> base + u }
}

//fun esGradientDescent(baseParams: INDArray, params: INDArray, evals: INDArray, learningRate: Float) : INDArray {
//    val grads =
//}

