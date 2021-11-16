package com.jonahshader.systems.creatureparts

interface Controllable {
    fun getControllableCount() : Int
    fun setControllableValue(index: Int, value: Float)
}