package com.jonahshader.systems.neuralnet.neurons

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
        neuronName = NeuronName.LeakyIntegrateAndFire
        color.set(.2f, .8f, .2f, 1f)
    }

    override fun setParameters(params: List<Float>) {
        bias = params[0]
        initMembraneVoltage = params[1]
        capacitance = params[2]
        resistance = params[3]
    }

    override fun getParameters(): List<Float> = listOf(bias, initMembraneVoltage, capacitance, resistance)

    override fun update(dt: Float) {
        val inputCurrent = inputSum + bias
        // compute delta membrane voltage
        val deltaV = (inputCurrent - (membraneVoltage / resistance)) / capacitance

        // integrate delta membrane voltage
        membraneVoltage += deltaV * dt

        // spike
        outputBuffer =  if (membraneVoltage > spikeThreshold) {
            membraneVoltage = recessMembraneVoltage
            1/dt
        } else {
            0f
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