package com.jonahshader.systems.simulation

interface Environment {
    fun resetAndRandomize()
    fun render()
    fun update(dt: Float) {}
}