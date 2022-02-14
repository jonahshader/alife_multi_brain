package com.jonahshader.systems.ui

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.jonahshader.MultiBrain
import ktx.math.minus
import ktx.math.plusAssign
import space.earlygrey.shapedrawer.JoinType

class Window {
    private val size = Vector2()
    private val localPosition = Vector2()
    private val globalPosition = Vector2()
    private val childWindows = mutableListOf<Window>()
    private var parent: Window? = null
    private var remove = false

    private var dragging = false
    private val pDragPos = Vector2()

    constructor(pos: Vector2, size: Vector2) {
        this.localPosition.set(pos)
        this.size.set(size)
    }

    fun update(dt: Float) {
        globalPosition.set(localPosition)
        if (parent != null)
            globalPosition.add(parent!!.globalPosition)
        childWindows.forEach { it.update(dt) }
        childWindows.removeIf { it.remove }
    }

    fun render(cam: Camera) {
        MultiBrain.shapeDrawer.setColor(.8f, .8f, .8f, 1f)
        MultiBrain.shapeDrawer.filledRectangle(globalPosition.x, globalPosition.y, size.x, size.y, Color(.8f, .8f, .8f, 1f))
        MultiBrain.shapeDrawer.filledRectangle(globalPosition.x + 2, globalPosition.y + 2, size.x - 4, size.y - 4, Color(.2f, .2f, .2f, 1f))
//        MultiBrain.shapeDrawer.rectangle(globalPosition.x, globalPosition.y, size.x, size.y, 2f, JoinType.SMOOTH)
        childWindows.forEach { it.render(cam) }
    }

    fun addChildWindow(childWindow: Window) {
        childWindow.parent = this
        childWindows += childWindow
    }

    fun handleMouseInput(mouseStateChange: Boolean, mouseDown: Boolean, mousePos: Vector2) : Boolean {
        // first check all children to find the lowest window in the tree that could be dragged by this input
        for (w in childWindows) {
            if (w.handleMouseInput(mouseStateChange, mouseDown, mousePos)) return true
        }
        // none of the children handled that, so see if that input is applicable to this instance
        if (mouseStateChange && mouseDown) {
            if (mousePos.x in (globalPosition.x .. globalPosition.x + size.x) &&
                mousePos.y in (globalPosition.y .. globalPosition.y + size.y)) {
                dragging = true
                pDragPos.set(mousePos)
                return true
            }
        }
        if (!mouseDown) dragging = false

        if (dragging) {
            localPosition += mousePos - pDragPos
            pDragPos.set(mousePos)
            return true
        }

        return false
    }

}