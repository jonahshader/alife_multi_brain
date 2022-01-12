package com.jonahshader.systems.brain.neurons

import java.util.*

enum class HiddenType {
    LeakyReLU,
    Tanh;

    companion object {
        fun getRandom(rand: Random) = values()[rand.nextInt(values().size)]
    }
}