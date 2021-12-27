package com.jonahshader.systems.scenegraph

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2

abstract class Node2D {
    val localPosition = Vector2()
    val globalPosition = Vector2()
    var localRotation = 0f
    var globalRotation = 0f
    val children = mutableListOf<Node2D>()
    var remove = false

    open fun update(parentPos: Vector2, parentRot: Float, dt: Float) {
        globalPosition.set(localPosition)
        globalPosition.add(parentPos)
        globalRotation = parentRot + localRotation
        customUpdate(dt)
        children.forEach { it.update(globalPosition, globalRotation, dt) }
        children.removeIf { it.remove }
    }

    fun render(batch: Batch) {
        customRender(batch)
        children.forEach { it.render(batch) }
    }

    fun addChild(child: Node2D) {
        children += child
    }

    protected abstract fun customUpdate(dt: Float)
    protected abstract fun customRender(batch: Batch)
}