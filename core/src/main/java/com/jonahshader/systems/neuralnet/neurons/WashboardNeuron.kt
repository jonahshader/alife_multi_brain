package com.jonahshader.systems.neuralnet.neurons

import com.badlogic.gdx.math.Vector2
import com.jonahshader.MultiBrain
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class WashboardNeuron(
    // effective dampening
    private val a: Float = 0.01f) : Neuron() {
    companion object {
        // metric units
        const val TERA = 1e12
        const val GIGA = 1e9
        const val FEMTO = 1e-15

        // exchange frequency(THz) * 2pi
        const val w_ex = (27.5 * 2 * PI).toFloat()
        // easy axis anisotropy(GHz) * 2pi
        const val w_e = (1.75 * 2 * PI).toFloat()
        // spin-torque efficiency (THz/A)
        const val lSigma = 2.16f
        // spin pumping efficiency (fVs)
        const val B = 0.11f

        const val USE_D = true
    }

    var angle = 0f
    private var angularVel = 0f

    var angleD = 0.0
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
//        updateFpClean()

    }

    private fun updateFp() {
        val dt = 1/1000f
        val inputCurrent = inputSum + bias

        // compute angular acceleration
        val angularAccel = (lSigma * inputCurrent - a*angularVel - (w_e/2)*sin(2 * angle)) * w_ex

        // integrate angular acceleration
        angularVel += angularAccel * dt

        // integrate angular vel
        angle += angularVel * dt

        angle = angle.mod(2* PI.toFloat())

        outputBuffer = angularVel * B
    }

    private fun updateFpClean() {
        val dt = 2e-14.toFloat() // DON'T CHANGE
        val inputCurrent = inputSum + bias

        // compute angular acceleration (pre multiplied by dt)
        val angularAccel: Float = 7.464424E12F * inputCurrent - 3.455752f * a * angularVel - 1.89989888E10f * sin(2*angle)

        // integrate angular acceleration
        angularVel += angularAccel

        // integrate angular vel
        angle += angularVel * dt

        outputBuffer = angularVel * B * 10e-15f
    }

    private fun updateDp() {
        val dt = 2e-14 // 10e-15
//        val dt = 10e-16 // 1 picoseconds?? idk was 10e-15
//        val inputCurrent = inputSum + bias * 10e-6 // TODO: multiply by B here instead of at the output
        val inputCurrent = inputSum + bias
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