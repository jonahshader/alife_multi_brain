package com.jonahshader.systems.creatureparts.softbody

import com.jonahshader.systems.neuralnet.neurons.NeuronName

class SoftBodyParams {
    var addRemoveGripperSd = .8f
    var addRemoveMuscleSd = .8f
    var minMuscleLength = 15f
    var maxMuscleExtention = 30f
    var gripperCountInit = 8
    var connectivityInit = .5f
    var gripperInitPositionMaxRadius = 24f
}