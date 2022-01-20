package com.jonahshader.systems.creatureparts.softbody

import com.badlogic.gdx.math.Vector2
import com.jonahshader.systems.brain.Network
import com.jonahshader.systems.brain.NetworkParams
import com.jonahshader.systems.ga.BodyGenes
import com.jonahshader.systems.ga.CombinedGenes
import com.jonahshader.systems.ga.NNGenes
import com.jonahshader.systems.utils.Rand
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

class BrainSoftBody : SoftBody {
    companion object {
        private const val CUSTOM_INPUTS = 2
    }
    private val network: Network
    private var age = 0f

    constructor(rand: Random = Rand.randx, combinedGenes: CombinedGenes) : super(rand, combinedGenes.sbGenes) {
        network = Network(combinedGenes.nnGenes,
            combinedGenes.sbGenes.getInputs() + CUSTOM_INPUTS, combinedGenes.sbGenes.getOutputs(), rand)
    }
    constructor(rand: Random = Rand.randx, bodyParams: SoftBodyParams, nnParams: NetworkParams) : super(rand, bodyParams) {
        network = Network(CUSTOM_INPUTS, muscles.size + grippers.size, nnParams, rand)
    }


    override fun preUpdate(dt: Float) {
        network.setInput(0, cos(age))
        network.setInput(1, sin(age))
        network.update(1/100f)
        for (i in network.outputNeurons.indices) {
            setControllableValue(i, network.getOutput(i))
        }
        age += dt
        super.preUpdate(dt)
    }



}