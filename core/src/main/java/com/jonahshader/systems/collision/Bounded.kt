package com.jonahshader.systems.collision

import com.badlogic.gdx.math.Rectangle
import com.jonahshader.systems.scenegraph.Node2D

abstract class Bounded : Node2D() {
    abstract fun getBounds() : Rectangle
}