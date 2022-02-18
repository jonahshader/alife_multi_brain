package com.jonahshader.systems.math

import jcuda.jcublas.JCublas

class CudaVector : CudaArray {

    private val cols: Int

    constructor(cols: Int, init: () -> Float = { 0f }) : super(FloatArray(cols) { init() }) {
        this.cols = cols
    }

    constructor(toCopy: CudaVector) : super(toCopy.array.copyOf()) {
        cols = toCopy.cols
    }

    fun copyFrom(vector: CudaVector) {
        JCublas.cublasScopy(cols, vector.pointer, 1, pointer, 1)
    }

    fun deepCopy() = CudaVector(this)

}