package com.jonahshader.systems.neuralnet

class NetworkSerializer {
    var fullNetworkBuilder: (Unit) -> Network? = { null }
    var networkParams = mutableListOf<Float>()
}