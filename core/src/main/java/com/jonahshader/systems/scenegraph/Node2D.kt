package com.jonahshader.systems.scenegraph

import com.badlogic.gdx.graphics.Camera
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
        // custom update yields an updated localPosition,
        // so we call it before updating globalPosition
        preUpdate(dt)
        globalPosition.set(localPosition)
        globalPosition.add(parentPos)
        globalRotation = parentRot + localRotation
        children.forEach { it.update(globalPosition, globalRotation, dt) }
        children.removeIf { it.remove }
        postUpdate(dt)
    }

    fun render(batch: Batch, cam: Camera) {
        if (isVisible(cam))
            customRender(batch, cam)
        children.forEach { it.render(batch, cam) }
    }

    fun addChild(child: Node2D) {
        children += child
    }

    open fun isVisible(cam: Camera) : Boolean = true

    open fun preUpdate(dt: Float) {}
    open fun postUpdate(dt: Float) {}
    protected abstract fun customRender(batch: Batch, cam: Camera)
}