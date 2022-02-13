package com.jonahshader.systems.neuralnet.neurons

import com.jonahshader.systems.ga.NeuronGene
import java.util.*

enum class NeuronName {
    Input,
    LeakyReLU,
    Output,
    Tanh,
    Sin,
    LeakyIntegrateAndFire,
    Washboard;

    companion object {
        // excludes input, output
        fun getRandomHidden(rand: Random) : NeuronName {
            var selection = rand.nextInt(values().size - 2) // TODO: is this correct?
            if (selection >= Input.ordinal) selection++
            if (selection >= Output.ordinal) selection++
            return values()[selection]
        }
        fun getRandom(rand: Random) = values()[rand.nextInt(values().size)]

        fun make(type: NeuronName) : Neuron {
            return when (type) {
                LeakyReLU -> LeakyReLUNeuron()
                Tanh -> TanhNeuron()
                Sin -> SinNeuron()
                Input -> InputNeuron()
                Output -> OutputNeuron()
                LeakyIntegrateAndFire -> LeakyIntegrateAndFireNeuron()
                Washboard -> WashboardNeuron()
            }
        }

        fun make(gene: NeuronGene) : Neuron {
            val neuron = make(gene.neuron)
            neuron.setParameters(gene.state.toList())
            return neuron
        }

        fun makeRandomHidden(rand: Random) = make(getRandomHidden(rand))
        fun makeRandom(rand: Random) = make(getRandom(rand))
    }
}