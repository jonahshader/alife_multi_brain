package com.jonahshader.systems.brain.neurons

import java.util.*

enum class NeuronType {
    Input,
    LeakyReLU,
    Output,
    Tanh,
    Sin;

    companion object {
        // excludes input, output
        fun getRandomHidden(rand: Random) : NeuronType {
            var selection = rand.nextInt(values().size - 2) // TODO: is this correct?
            if (selection >= Input.ordinal) selection++
            if (selection >= Output.ordinal) selection++
            return values()[selection]
        }
        fun getRandom(rand: Random) = values()[rand.nextInt(values().size)]

        fun make(type: NeuronType) : Neuron {
            return when (type) {
                LeakyReLU -> LeakyReLUNeuron()
                Tanh -> TanhNeuron()
                Sin -> SinNeuron()
                Input -> InputNeuron()
                Output -> OutputNeuron()
            }
        }
        fun makeRandomHidden(rand: Random) = make(getRandomHidden(rand))
        fun makeRandom(rand: Random) = make(getRandom(rand))
    }
}