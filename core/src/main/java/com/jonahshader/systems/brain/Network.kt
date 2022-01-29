package com.jonahshader.systems.brain

interface Network {
    fun setInput(index: Int, value: Float)
    fun getOutput(index: Int) : Float

    fun getInputSize() : Int
    fun getOutputSize() : Int
    fun update(dt: Float)
}