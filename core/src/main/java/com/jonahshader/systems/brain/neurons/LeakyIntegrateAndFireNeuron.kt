package com.jonahshader.systems.brain.neurons

import com.jonahshader.systems.ga.NeuronGene
import java.util.*

class LeakyIntegrateAndFireNeuron : Neuron() {
    // TODO: units???
    // TODO: keep track of init membrane voltage, or always init at zero?
    var initMembraneVoltage = 0f
    var recessMembraneVoltage = -.1f
    private val spikeThreshold = 1f // same units as membrane voltage
    var membraneVoltage = 0f
    var capacitance = 1f
    var resistance = 10f

    companion object {
        private const val INIT_MEMBRANE_VOLTAGE_MUTATE = .5f
        private const val CAPACITANCE_MUTATE = .25f
        private const val RESISTANCE_MUTATE = .25f
    }

    init {
        neuronType = NeuronType.LeakyIntegrateAndFire
    }

    override fun makeGenetics() = NeuronGene(neuronType, floatArrayOf(bias, initMembraneVoltage, capacitance, resistance))

    override fun setState(state: FloatArray) {
        bias = state[0]
        initMembraneVoltage = state[1]
        capacitance = state[2]
        resistance = state[3]
    }

    override fun update(dt: Float) {
        val inputCurrent = inputSum + bias
        // compute delta membrane voltage
        val deltaV = (inputCurrent - (membraneVoltage / resistance)) / capacitance

        // integrate delta membrane voltage
        membraneVoltage += deltaV * dt

        // spike
        if (membraneVoltage > spikeThreshold) {
            membraneVoltage = recessMembraneVoltage
            outputBuffer = 1/dt
        } else {
            outputBuffer = 0f
        }
    }

    override fun mutateScalars(rand: Random, amount: Float) {
        super.mutateScalars(rand, amount)
        initMembraneVoltage += rand.nextGaussian().toFloat() * amount * INIT_MEMBRANE_VOLTAGE_MUTATE

        // mutate capacitance, don't mutate if result is negative
        val oldC = capacitance
        capacitance += rand.nextGaussian().toFloat() * amount * CAPACITANCE_MUTATE
        if (capacitance <= 0) capacitance = oldC

        // mutate resistance, don't mutate if result is negative
        val oldR = resistance
        resistance += rand.nextGaussian().toFloat() * amount * RESISTANCE_MUTATE
        if (resistance <= 0) resistance = oldR
    }

    override fun resetStateInternals() {
        super.resetStateInternals()
        membraneVoltage = initMembraneVoltage
    }



}