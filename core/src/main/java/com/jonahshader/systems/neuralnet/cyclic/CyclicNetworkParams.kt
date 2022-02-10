package com.jonahshader.systems.neuralnet.cyclic

class CyclicNetworkParams {
    var weightInitSd = 1f
    var addRemoveNeuronSd = 1f
    var addRemoveWeightSd = 0.4f

    var hiddenNeuronCountInit = 50
    var connectivityInit = .1f // .1f

    val mutateWeightSd = .1f
}