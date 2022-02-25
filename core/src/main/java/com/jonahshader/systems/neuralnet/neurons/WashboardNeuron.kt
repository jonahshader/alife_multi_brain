package com.jonahshader.systems.neuralnet.neurons

import com.badlogic.gdx.math.Vector2
import com.jonahshader.MultiBrain
import com.jonahshader.systems.math.Metric.FEMTO
import com.jonahshader.systems.math.Metric.GIGA
import com.jonahshader.systems.math.Metric.PICO
import com.jonahshader.systems.math.Metric.TERA
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class WashboardNeuron(
    // effective dampening
    private val a: Float = 0.0000f) : Neuron() {
    companion object {
        // exchange frequency(THz) * 2pi
        const val w_ex = (27.5 * 2 * PI) * TERA
        // easy axis anisotropy(GHz) * 2pi
        const val w_e = (1.75 * 2 * PI) * GIGA
        // spin-torque efficiency (THz/A)
        const val lSigma = 2.16 * TERA
        // spin pumping efficiency (fVs)
        const val B = 0.11
    }

    var angleD = 0.0
    private var angularVelD = 0.0
    private var angularAccelD = 0.0

    var angle = 0.0f
    private var angularVel = 0.0f

    init {
        neuronName = NeuronName.Washboard
        color.set(.0f, .8f, 1f, 1f)
    }

    override fun update(dt: Float) {
//        testFp(dt)
        updateDp(dt)
    }

    private fun testFp(dt: Float) {
        val inputCurrent = inputSum + bias
        val angularAccel = (lSigma.toFloat() * inputCurrent - a*angularVel - (w_e.toFloat()/2)*sin(2*angle)) * w_ex.toFloat()
        angularVel += angularAccel * dt
        angle += angularVel * dt
        outputBuffer = (angularVel * B.toFloat() * FEMTO.toFloat())
    }

    private fun updateDp(dt: Float) {
        // 1 pico second
//        val dt = 2e-14 // 10e-15
//        val dt = PICO * 1e-1
//        val dt = 10e-16 // 1 picoseconds?? idk was 10e-15
//        val inputCurrent = inputSum + bias * 10e-6 // TODO: multiply by B here instead of at the output
        val inputCurrent = inputSum + bias
//        val inputCurrent = (inputSum + bias) * B * FEMTO
//        val inputCurrent = inputSum

        // compute angular acceleration
        angularAccelD = (lSigma * inputCurrent - a*angularVelD - (w_e/2)*sin(2*angleD)) * w_ex

        // integrate angular acceleration
        angularVelD += angularAccelD * dt

        // integrate angular vel
        angleD += angularVelD * dt

        outputBuffer = (angularVelD * B * FEMTO).toFloat()
//        outputBuffer = angularVelD.toFloat()
    }

    override fun render(pos: Vector2) {
        super.render(pos)

        val ang = angleD.toFloat()
        // red positive, blue negative
        MultiBrain.shapeDrawer.setColor(1f, 1f, 1f, 1f)
        MultiBrain.shapeDrawer.line(pos.x, pos.y, pos.x + cos(ang) * 5f, pos.y + sin(ang) * 5f, 2f)

        if (ang.isNaN()) {
            MultiBrain.shapeDrawer.line(pos.x - 10f, pos.y - 10f, pos.x + 10f, pos.y + 10f, 3f)
        } else if (ang.isInfinite()) {
            MultiBrain.shapeDrawer.line(pos.x + 10f, pos.y - 10f, pos.x - 10f, pos.y + 10f, 3f)
        }
    }
}