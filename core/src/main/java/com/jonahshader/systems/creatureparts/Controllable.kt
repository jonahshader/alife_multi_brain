package com.jonahshader.systems.creatureparts

interface Controllable {
    fun getControllableCount() : Int = 1
    fun setControllableValue(index: Int, value: Float)
}