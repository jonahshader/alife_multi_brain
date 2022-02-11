package com.jonahshader.systems.neuralnet.neurons

import com.badlogic.gdx.math.Vector2
import com.jonahshader.MultiBrain
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class WashboardNeuron : Neuron() {
    companion object {
        // metric units
        const val TERA = 10e12
        const val GIGA = 10e9
        const val FEMTO = 10e-15

        // exchange frequency(THz) * 2pi
        const val w_ex = (27.5 * 2 * PI).toFloat()
        // effective damping
        const val a = 0.1f // 0.0001 or 0.1
        // easy axis anisotropy(GHz) * 2pi
        const val w_e = (1.75 * 2 * PI).toFloat()
        // spin-torque efficiency (THz/A)
        const val lSigma = 2.16f
        // spin pumping efficiency (fVs)
        const val B = 0.11f

        const val USE_D = false
    }

    private var angle = 0f
    private var angularVel = 0f
    private var angularAccel = 0f

    private var angleD = 0.0
    private var angularVelD = 0.0
    private var angularAccelD = 0.0

    init {
        neuronName = NeuronName.Washboard
        color.set(.0f, .8f, 1f, 1f)
    }

    override fun update(dt: Float) {
        if (USE_D) {
            updateDp()
        } else {
            updateFp()
        }

    }

    private fun updateFp() {
        val dt = 1/1000f
        val inputCurrent = inputSum + bias

        // compute angular acceleration
        angularAccel = (lSigma * inputCurrent - a*angularVel - (w_e/2)*sin(2 * angle)) * w_ex

        // integrate angular acceleration
        angularVel += angularAccel * dt

        // integrate angular vel
        angle += angularVel * dt

        angle = angle.mod(2* PI.toFloat())

        outputBuffer = angularVel * B
    }

    private fun updateDp() {
        val dt = 10e-15 // 1 picoseconds
        val inputCurrent = inputSum + bias * 10e-6 // TODO: multiply by B here instead of at the output
//        val inputCurrent = (inputSum + bias) * B * FEMTO
//        val inputCurrent = inputSum

        // compute angular acceleration
        angularAccelD = (lSigma * TERA * inputCurrent - a*angularVelD - (w_e * GIGA/2)*sin(2*angleD)) * w_ex * TERA

        // integrate angular acceleration
        angularVelD += angularAccelD * dt

        // integrate angular vel
        angleD += angularVelD * dt

        outputBuffer = (angularVelD * B * FEMTO).toFloat()
//        outputBuffer = angularVelD.toFloat()
    }

    override fun render(pos: Vector2) {
        super.render(pos)

        val ang = if (USE_D) angleD.toFloat() else angle
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