package com.jonahshader.systems.creatureparts.softbody

import com.jonahshader.systems.neuralnet.cyclic.CyclicNetwork
import com.jonahshader.systems.neuralnet.cyclic.CyclicNetworkParams
import com.jonahshader.systems.ga.CombinedGenes
import com.jonahshader.systems.utils.Rand
import java.util.*

class BrainSoftBody : SoftBody {
    companion object {
        private const val CUSTOM_INPUTS = 2
    }
    val network: CyclicNetwork
    private var age = 0f

    constructor(rand: Random = Rand.randx, genes: CombinedGenes) : super(rand, genes.sbGenes) {
                network = CyclicNetwork(genes.nnGenes,
                    genes.sbGenes.getInputs() + CUSTOM_INPUTS, genes.sbGenes.getOutputs(), rand)
    }

//    constructor(rand: Random = Rand.randx, bodyGenes: BodyGenes, network: Network) : super(rand, bodyGenes) {
//        this.network = network
//    }
    constructor(rand: Random = Rand.randx, bodyParams: SoftBodyParams, nnParams: CyclicNetworkParams) : super(rand, bodyParams) {
        network = CyclicNetwork(CUSTOM_INPUTS, muscles.size + grippers.size, nnParams, rand)
    }

//    constructor(rand: Random = Rand.randx, bodyParams: SoftBodyParams) : super(rand, bodyParams) {
//        network = DenseCyclicNetwork(CUSTOM_INPUTS, 32, muscles.size + grippers.size, rand)
//    }


    override fun preUpdate(dt: Float) {
//        network.setInput(0, cos(age))
//        network.setInput(1, sin(age))
        network.update(1/1000f)
        for (i in 0 until network.getOutputSize()) {
            setControllableValue(i, network.getOutput(i))
        }
        age += dt
        super.preUpdate(dt)
    }

    fun getCombinedGenes() = CombinedGenes(network.makeGenes(), makeGenes())

    override fun mutateBody(amount: Float) {
//        super.mutateBody(amount)
//        // check output size matches
//        if (outputs > network.getOutputSize()) {
//            network.resizeOutputs(outputs)
//            // connect up new outputs
//            network.connectNewOutputs(network.networkParams.connectivityInit)
//        } else if (outputs < network.outputNeurons.size) {
//            // don't bother outputs because we only removed outputs instead of adding
//            network.resizeOutputs(outputs)
//        }
    }
}