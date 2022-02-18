package com.jonahshader.systems.math

import jcuda.jcublas.JCublas

class CudaMatrix : CudaArray {
//    m      number of rows of matrix op(A) and rows of matrix C
//    n      number of columns of matrix op(B) and number of columns of C
//    k      number of columns of matrix op(A) and number of rows of op(B)
    // no idea if this works
    val rows: Int
    val cols: Int

    constructor(rows: Int, cols: Int, init: () -> Float = { 0f }) : super(FloatArray(rows * cols) { init() }) {
        this.rows = rows
        this.cols = cols
    }

    constructor(toCopy: CudaMatrix) : super(toCopy.array.copyOf()) {
        rows = toCopy.rows
        cols = toCopy.cols
    }

    /**
     * matrixStore = this x matrixB
     */
    fun multiply(matrixB: CudaMatrix, matrixStore: CudaMatrix) {
        JCublas.cublasSgemm('n', 'n',
            rows, matrixB.cols, cols, 1f,
            pointer, rows, matrixB.pointer, matrixB.rows, 0f, matrixStore.pointer, matrixStore.rows)
    }

    /**
     * vectorStore = this x vectorB
     */
    fun multiply(vectorB: CudaVector, vectorStore: CudaVector, beta: Float) {
        JCublas.cublasSgemv('n', rows, cols, 1f,
            pointer, rows, vectorB.pointer, 1, beta, vectorStore.pointer, 1)
    }

    fun deepCopy() = CudaMatrix(this)
}