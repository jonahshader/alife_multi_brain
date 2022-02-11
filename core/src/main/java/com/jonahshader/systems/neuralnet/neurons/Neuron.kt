package com.jonahshader.systems.neuralnet.neurons

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.jonahshader.MultiBrain
import com.jonahshader.systems.ga.NeuronGene
import java.util.*

abstract class Neuron {
    enum class NeuronCategory {
        INPUT,
        OUTPUT,
        HIDDEN
    }
    protected var outputBuffer = 0.0f
    var out = 0.0f
        private set
    var bias = 0.0f
    protected var inputSum = 0f

    // indicates if this can be removed by random mutations
    // this should be false for IO related neurons
    var neuronCategory = NeuronCategory.HIDDEN
        protected set
    var neuronName = NeuronName.Input
        protected set

    // graphical stuff
    protected val color = Color(.125f, 1f, 1f, 1f)

    /**
     * computes output, stores it in outputBuffer
     */
    open fun update(dt: Float){}

    /**
     * resets accumulative values (besides out, outputBuffer)
     */
    protected open fun resetStateInternals(){}

    /**
     * makes the output the most recent computed value
     * also resets accumulation
     */
    fun updateOutput() {
        out = outputBuffer
        inputSum = 0f
    }

    /**
     * add weighted output from another neuron
     */
    fun addWeightedOutput(weightedOutput: Float) {
        inputSum += weightedOutput
    }

    /**
     * mutates internal variables (i can only think of bias)
     */
    open fun mutateScalars(rand: Random, amount: Float) {
        bias += rand.nextGaussian().toFloat() * amount
    }

    /**
     * make genetic representation
     */
    fun makeGenetics() = NeuronGene(neuronName, getParameters().toFloatArray())

    /**
     * set trainable parameters
     */
    open fun setParameters(params: List<Float>) {
        bias = params[0]
    }

    /**
     * get trainable parameters
     */
    open fun getParameters() : List<Float> = listOf(bias)

    /**
     * resetState() reset any integrator variables or anything representing state
     * from a previous time step. when duplicating
     */
    fun resetState() {
        outputBuffer = 0.0f
        out = 0.0f
        resetStateInternals()
    }

    open fun render(pos: Vector2) {
        val DEFAULT_RADIUS = 4.0f
        // activation color
        val brightness = (out / 2f).coerceIn(0f, 1f)
        MultiBrain.shapeDrawer.setColor(brightness, brightness, brightness, 1f)
        MultiBrain.shapeDrawer.filledCircle(pos, DEFAULT_RADIUS)

        // shell color
        MultiBrain.shapeDrawer.setColor(color)
        MultiBrain.shapeDrawer.circle(pos.x, pos.y, DEFAULT_RADIUS)
    }
}