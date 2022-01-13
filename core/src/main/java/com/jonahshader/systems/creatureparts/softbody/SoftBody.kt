package com.jonahshader.systems.creatureparts.softbody

import com.jonahshader.systems.brain.Network
import com.jonahshader.systems.ga.BodyGenes
import com.jonahshader.systems.ga.NNGenes

class SoftBody {
    private val grippers = mutableListOf<Gripper>()
    private val muscles = mutableListOf<Muscle>()
    private val brain: Network

    // brain inputs and outputs
    private var inputs = 0
    private var outputs = 0

    constructor(bodyGenes: BodyGenes, nnGenes: NNGenes) {
        for (g in bodyGenes.gripperGenes)
            grippers += Gripper(g)
        for (m in bodyGenes.muscleGenes)
            muscles += Muscle(m, grippers)

        inputs = 0
        outputs = muscles.size + grippers.size

        brain = Network(nnGenes, inputs, outputs)
    }
}