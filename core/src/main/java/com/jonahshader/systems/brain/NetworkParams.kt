package com.jonahshader.systems.brain

class NetworkParams {
    var weightInitSd = 1f
    var addRemoveNeuronSd = 1f
    var addRemoveWeightSd = 0.4f

    var hiddenNeuronCountInit = 50
    var connectivityInit = .1f // .1f

    val mutateWeightSd = .1f
}