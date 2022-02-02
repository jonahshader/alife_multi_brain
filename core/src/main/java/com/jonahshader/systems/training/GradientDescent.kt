package com.jonahshader.systems.training

import kotlin.math.pow


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

// for now, input should be sorted and evals should be mapped to rank
fun esGradientDescent(baseParams: List<Float>, paramsList: List<List<Float>>, evals: List<Float>, learningRate: Float) : List<Float> {
    val grads = computeGradientsFromParamEvals(paramsList, evals)

    val update = gradientDescentUpdate(grads, learningRate)
    return baseParams.zip(update) { base, u -> base + u }
}
