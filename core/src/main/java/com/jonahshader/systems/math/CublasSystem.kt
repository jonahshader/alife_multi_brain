package com.jonahshader.systems.math

import jcuda.jcublas.JCublas

object CublasSystem {
    private var initialized = false

    fun cublasInit() {
        if (!initialized) {
            JCublas.cublasInit()
            initialized = true
        }
    }

    fun cublasShutdown() {
        if (initialized) {
            JCublas.cublasShutdown()
            initialized = false
        }
    }
}