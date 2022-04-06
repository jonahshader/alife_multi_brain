package com.jonahshader.systems.math

import org.nd4j.linalg.api.ndarray.INDArray

operator fun INDArray.times(float: Float): INDArray = this.mul(float)

operator fun Float.times(indarray: INDArray): INDArray = indarray.mul(this)

operator fun INDArray.times(indarray: INDArray): INDArray = indarray.mulColumnVector(this)

operator fun INDArray.div(float: Float): INDArray = this.div(float)

operator fun Float.div(indarray: INDArray): INDArray = indarray.div(this)

operator fun INDArray.div(indarray: INDArray): INDArray = indarray.divColumnVector(this)

operator fun INDArray.plus(float: Float): INDArray = this.add(float)

operator fun Float.plus(indarray: INDArray): INDArray = indarray.add(this)

operator fun INDArray.plus(indarray: INDArray): INDArray = indarray.add(this)

operator fun INDArray.plusAssign(indarray: INDArray) {
    this.addi(indarray)
}

operator fun INDArray.minus(float: Float): INDArray = this.sub(float)

operator fun Float.minus(indarray: INDArray): INDArray = indarray.sub(this)

operator fun INDArray.minus(indarray: INDArray): INDArray = indarray.sub(this)

