package com.jonahshader.systems.neuralnet.neurons

import com.badlogic.gdx.math.Vector2
import com.jonahshader.MultiBrain
import com.jonahshader.systems.math.Metric.FEMTO
import com.jonahshader.systems.math.Metric.GIGA
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

    var thetaD = 0.0
    private var angularVelD = 0.0
    private var angularAccelD = 0.0

    var theta = 0.0f
    private var angularVel = 0.0f

    var pNeuron: WashboardNeuron? = null
    var pWeight = 0.0

    var k1pos = 0.0
    var k1vel = 0.0

    var k2pos = 0.0
    var k2vel = 0.0

    var k3pos = 0.0
    var k3vel = 0.0

    var k4pos = 0.0
    var k4vel = 0.0


    enum class IntegrationMode {
        EULER,
        TRAP_EULER,
        RK4
    }
    var integrationMode = IntegrationMode.EULER

    init {
        neuronName = NeuronName.Washboard
        color.set(.0f, .8f, 1f, 1f)
    }

    override fun update(dt: Float) {
//        testFp(dt)
        when (integrationMode) {
            IntegrationMode.EULER -> updateDp(dt)
            IntegrationMode.TRAP_EULER -> updateDpTrap(dt)
            IntegrationMode.RK4 -> updateDpRK4(dt)
        }
    }

    private fun accel(inputCurrent: Double, theta: Double, angularVel: Double) = (lSigma * inputCurrent - a*angularVel - (w_e/2)*sin(2*theta)) * w_ex

//    private fun testFp(dt: Float) {
//        val inputCurrent = inputSum + bias
//        val angularAccel = (lSigma.toFloat() * inputCurrent - a*angularVel - (w_e.toFloat()/2)*sin(2*theta)) * w_ex.toFloat()
//        angularVel += angularAccel * dt
//        theta += angularVel * dt
//        outputBuffer = (angularVel * B.toFloat() * FEMTO.toFloat())
//    }

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
        angularAccelD = accel(inputCurrent.toDouble(), thetaD, angularVelD)

        // integrate angular acceleration
        angularVelD += angularAccelD * dt

        // integrate angular vel
        thetaD += angularVelD * dt

        outputBuffer = (angularVelD * B * FEMTO).toFloat()
//        outputBuffer = angularVelD.toFloat()
    }

    private fun updateDpTrap(dt: Float) {
        val inputCurrent = inputSum + bias
        val newAngularAccelD = (lSigma * inputCurrent - a*angularVelD - (w_e/2)*sin(2*thetaD)) * w_ex
        val newAngularVelD = (angularAccelD + newAngularAccelD) * .5 * dt

        thetaD += (angularVelD + newAngularVelD) * .5 * dt

        angularAccelD = newAngularAccelD
        angularVelD = newAngularVelD

        outputBuffer = (angularVelD * B * FEMTO).toFloat()

    }


    // https://en.wikipedia.org/wiki/Runge%E2%80%93Kutta_methods
    // https://scicomp.stackexchange.com/questions/26766/4th-order-runge-kutta-method-for-driven-damped-pendulum
    // https://stackoverflow.com/questions/52985027/runge-kutta-4-and-pendulum-simulation-in-python
    private fun updateDpRK4(dt: Float) {
        val inputCurrent = (inputSum + bias).toDouble()

        if (pNeuron == null) {
            k1pos = dt * angularVelD
            k1vel = dt * accel(inputCurrent, thetaD, angularVelD)

            k2pos = dt * (angularVelD + .5 * k1vel)
            k2vel = dt * accel(inputCurrent, thetaD + .5 * k1pos, angularVelD + .5 * k1vel)

            k3pos = dt * (angularVelD + .5 * k2vel)
            k3vel = dt * accel(inputCurrent, thetaD + .5 * k2pos, angularVelD + .5 * k2vel)

            k4pos = dt * (angularVelD + k3vel)
            k4vel = dt * accel(inputCurrent, thetaD + k3pos, angularVelD + k3vel)

            thetaD += (k1pos + 2 * k2pos + 2 * k3pos + k4pos) / 6.0
            angularVelD += (k1vel + 2 * k2vel + 2 * k3vel + k4vel) / 6.0
        } else {
            // TODO: i think this is correct, but it appears to perform worse than when inputCurrent is treated like a constant
            val pn = pNeuron!!
            k1pos = dt * angularVelD
            k1vel = dt * accel(inputCurrent, thetaD, angularVelD)

            k2pos = dt * (angularVelD + .5 * k1vel)
            k2vel = dt * accel(inputCurrent + pn.k1vel * B * FEMTO * pWeight, thetaD + .5 * k1pos, angularVelD + .5 * k1vel)

            k3pos = dt * (angularVelD + .5 * k2vel)
            k3vel = dt * accel(inputCurrent + .5 * pn.k2vel * B * FEMTO * pWeight, thetaD + .5 * k2pos, angularVelD + .5 * k2vel)

            k4pos = dt * (angularVelD + k3vel)
            k4vel = dt * accel(inputCurrent + pn.k3vel * B * FEMTO * pWeight, thetaD + k3pos, angularVelD + k3vel)

            thetaD += (k1pos + 2 * k2pos + 2 * k3pos + k4pos) / 6.0
            angularVelD += (k1vel + 2 * k2vel + 2 * k3vel + k4vel) / 6.0
        }


        outputBuffer = (angularVelD * B * FEMTO).toFloat()
    }


    override fun render(pos: Vector2) {
        super.render(pos)

        val ang = thetaD.toFloat()
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