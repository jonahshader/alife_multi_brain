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
    var parent: Node2D? = null

    open fun update(dt: Float) {
        // custom update yields an updated localPosition,
        // so we call it before updating globalPosition
        preUpdate(dt)
        globalPosition.set(localPosition)
        if (parent != null)
            globalPosition.add(parent!!.globalPosition)
        globalRotation = (parent?.globalRotation ?: 0f) + localRotation
        children.forEach { it.update(dt) }
        children.removeIf { it.remove }
        postUpdate(dt)
    }

    fun render(batch: Batch, cam: Camera) {
        if (isVisible(cam))
            customRender(batch, cam)
        children.forEach { it.render(batch, cam) }
    }

    fun addChild(child: Node2D) {
        child.parent = this
        children += child
    }

    open fun isVisible(cam: Camera) : Boolean = true

    open fun preUpdate(dt: Float) {}
    open fun postUpdate(dt: Float) {}
    open fun customRender(batch: Batch, cam: Camera) {}
}