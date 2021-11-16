package com.jonahshader.systems.creatureparts

interface Sensor {
    fun getSensorCount() : Int
    fun getSensorValue(index: Int) : Float
}