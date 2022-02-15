package com.jonahshader.systems.utils

fun map(x: Float, inMin: Float, inMax: Float, outMin: Float, outMax: Float) =
    (x - inMin) * (outMax - outMin) / (inMax - inMin) + outMin
