package com.jonahshader.systems.creatureparts.softbody

import com.jonahshader.systems.brain.CyclicNetwork
import com.jonahshader.systems.brain.NetworkParams
import com.jonahshader.systems.ga.CombinedGenes
import com.jonahshader.systems.utils.Rand
import java.util.*

class BrainSoftBody : SoftBody {
    companion object {
        private const val CUSTOM_INPUTS = 2
    }
    val network: CyclicNetwork
    private var age = 0f

    constructor(rand: Random = Rand.randx, combinedGenes: CombinedGenes) : super(rand, combinedGenes.sbGenes) {
        network = CyclicNetwork(combinedGenes.nnGenes,
            combinedGenes.sbGenes.getInputs() + CUSTOM_INPUTS, combinedGenes.sbGenes.getOutputs(), rand)
    }
    constructor(rand: Random = Rand.randx, bodyParams: SoftBodyParams, nnParams: NetworkParams) : super(rand, bodyParams) {
        network = CyclicNetwork(CUSTOM_INPUTS, muscles.size + grippers.size, nnParams, rand)
    }


    override fun preUpdate(dt: Float) {
//        network.setInput(0, cos(age))
//        network.setInput(1, sin(age))
        network.update(1/100f)
        for (i in network.outputNeurons.indices) {
            setControllableValue(i, network.getOutput(i))
        }
        age += dt
        super.preUpdate(dt)
    }

    fun getCombinedGenes() = CombinedGenes(network.makeGenes(), makeGenes())

    override fun mutate(amount: Float) {
        network.mutate(amount)
        super.mutate(amount)
        // check output size matches
        if (outputs > network.outputNeurons.size) {
            network.resizeOutputs(outputs)
            // connect up new outputs
            network.connectNewOutputs(network.networkParams.connectivityInit)
        } else if (outputs < network.outputNeurons.size) {
            // don't bother outputs because we only removed outputs instead of adding
            network.resizeOutputs(outputs)
        }

    }
}