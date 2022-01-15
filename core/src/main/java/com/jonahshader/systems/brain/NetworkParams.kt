package com.jonahshader.systems.brain

class NetworkParams {
    var weightInitSd = .8f
    var addRemoveNeuronSd = 0.8f
    var addRemoveWeightSd = 0.4f

    var hiddenNeuronCountInit = 45
    var connectivityInit = .1f // .1f

    val mutateWeightSd = .1f
}