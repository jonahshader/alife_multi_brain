package com.jonahshader.systems.scenegraph

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2

abstract class Node2D {
    val localPosition = Vector2()
    val globalPosition = Vector2()
    val children = mutableListOf<Node2D>()
    var remove = false

    fun update(parentPos: Vector2, dt: Float) {
        globalPosition.set(localPosition)
        globalPosition.add(parentPos)
        customUpdate(dt)
        children.forEach { it.update(parentPos, dt) }
        children.removeIf { it.remove }
    }

    fun render(batch: Batch) {
        customRender(batch)
        children.forEach { it.render(batch) }
    }

    protected abstract fun customUpdate(dt: Float)
    protected abstract fun customRender(batch: Batch)
}