package com.jonahshader.systems.math

import com.badlogic.gdx.utils.Disposable
import jcuda.Pointer
import jcuda.jcublas.JCublas

open class CudaArray(val array: FloatArray) : Disposable {
    internal val pointer = Pointer()
    val indices = array.indices
    val size: Int get() = array.size
    init {
        CublasSystem.cublasInit()
        JCublas.cublasAlloc(array.size, Float.SIZE_BYTES, pointer)
    }

    fun upload() {
        JCublas.cublasSetVector(array.size, Float.SIZE_BYTES, Pointer.to(array), 1, pointer, 1)
    }

    fun download() {
        JCublas.cublasGetVector(array.size, Float.SIZE_BYTES, pointer, 1, Pointer.to(array), 1)
    }

    override fun dispose() {
        JCublas.cublasFree(pointer)
    }

    operator fun set(index: Int, value: Float) {
        array[index] = value
    }

    operator fun get(index: Int): Float {
        return array[index]
    }

    fun toList() = array.toList()
}