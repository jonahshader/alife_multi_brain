package com.jonahshader.systems.ui

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.jonahshader.MultiBrain
import ktx.math.minus
import ktx.math.plusAssign
import kotlin.math.absoluteValue

class Window {
    val size = Vector2()
    val localPosition = Vector2()
    val globalPosition = Vector2()
    private val childWindows = mutableListOf<Window>()
    private var parent: Window? = null
    private var remove = false

    private var dragging = false
    private var resizing = false
    private var xMaxResizing = false
    private var yMaxResizing = false
    private var xResizing = false
    private var yResizing = false
    private val pDragPos = Vector2()

    companion object {
        private const val RESIZE_RADIUS = 4f
    }


    constructor(pos: Vector2, size: Vector2) {
        this.localPosition.set(pos)
        this.size.set(size)
    }

    fun update(dt: Float) {
        updateGlobalPosition()
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
        for (w in childWindows.reversed()) {
            if (w.handleMouseInput(mouseStateChange, mouseDown, mousePos)) return true
        }
        // none of the children handled that, so see if that input is applicable to this instance

        if (mouseStateChange && mouseDown) {
            // check resize edges
            if ((mousePos.y > (globalPosition.y - RESIZE_RADIUS)) && mousePos.y < (globalPosition.y + size.y + RESIZE_RADIUS)) {
                if ((mousePos.x - globalPosition.x).absoluteValue < RESIZE_RADIUS) {
                    resizing = true
                    xResizing = true
                    xMaxResizing = false
                } else if ((mousePos.x - (globalPosition.x + size.x)).absoluteValue < RESIZE_RADIUS) {
                    resizing = true
                    xResizing = true
                    xMaxResizing = true
                }
            }
            if ((mousePos.x > (globalPosition.x - RESIZE_RADIUS)) && mousePos.x < (globalPosition.x + size.x + RESIZE_RADIUS)) {
                if ((mousePos.y - globalPosition.y).absoluteValue < RESIZE_RADIUS) {
                    resizing = true
                    yResizing = true
                    yMaxResizing = false
                } else if ((mousePos.y - (globalPosition.y + size.y)).absoluteValue < RESIZE_RADIUS) {
                    resizing = true
                    yResizing = true
                    yMaxResizing = true
                }
            }

            // TODO: keep resize within parent size. stop resizing if it goes out of bounds of parent

            if (!resizing) {
                if ((mousePos.x in (globalPosition.x..(globalPosition.x + size.x))) &&
                    (mousePos.y in (globalPosition.y..(globalPosition.y + size.y)))
                ) {
                    dragging = true
                    pDragPos.set(mousePos)
                    return true
                }
            } else {
                pDragPos.set(mousePos)
                return true
            }
        }

        if (!mouseDown) {
            dragging = false
            resizing = false
            xResizing = false
            yResizing = false
        }

        if (dragging) {
            localPosition += mousePos - pDragPos
            updateGlobalPosition()
            pDragPos.set(mousePos)
            if (parent != null) {
                if (globalPosition.x < parent!!.globalPosition.x) {
                    val xDelta = globalPosition.x - parent!!.globalPosition.x
                    localPosition.x -= xDelta
                } else if (globalPosition.x + size.x > parent!!.globalPosition.x + parent!!.size.x) {
                    val xDelta = globalPosition.x + size.x - (parent!!.globalPosition.x + parent!!.size.x)
                    localPosition.x -= xDelta
                }
                if (globalPosition.y < parent!!.globalPosition.y) {
                    val yDelta = globalPosition.y - parent!!.globalPosition.y
                    localPosition.y -= yDelta
                } else if (globalPosition.y + size.y > parent!!.globalPosition.y + parent!!.size.y) {
                    val yDelta = globalPosition.y + size.y - (parent!!.globalPosition.y + parent!!.size.y)
                    localPosition.y -= yDelta
                }
            }
            return true
        } else if (resizing) {
            if (xResizing) {
                val delta = mousePos.x - pDragPos.x
                if (xMaxResizing) {
                    size.x += delta
                    keepWithinParent()
                } else {
                    localPosition.x += delta
                    size.x -= delta
                    keepWithinParent()
                }

            }
            if (yResizing) {
                val delta = mousePos.y - pDragPos.y
                if (yMaxResizing) {
                    size.y += delta
                    keepWithinParent()
                } else {
                    localPosition.y += delta
                    size.y -= delta
                    keepWithinParent()

                }
            }
            pDragPos.set(mousePos)
        }

        return false
    }

    private fun updateGlobalPosition() {
        globalPosition.set(localPosition)
        if (parent != null)
            globalPosition.add(parent!!.globalPosition)
    }

    private fun notifyResize() {
        childWindows.forEach {
            it.notifyResize()
        }
    }

    private fun keepWithinParent() {
        updateGlobalPosition()

        // keep within parent while resizing
        if (parent != null) {
            if (globalPosition.x + size.x > parent!!.globalPosition.x + parent!!.size.x) {
                size.x = parent!!.globalPosition.x + parent!!.size.x - globalPosition.x
            }
        }
        // keep within parent while resizing
        if (parent != null) {
            if (globalPosition.x < parent!!.globalPosition.x) {
                val fixDelta = globalPosition.x - parent!!.globalPosition.x
                localPosition.x -= fixDelta
                size.x += fixDelta
                updateGlobalPosition()
            }
        }

        // keep within parent while resizing
        if (parent != null) {
            if (globalPosition.y + size.y > parent!!.globalPosition.y + parent!!.size.y) {
                size.y = parent!!.globalPosition.y + parent!!.size.y - globalPosition.y
            }
        }

        // keep within parent while resizing
        if (parent != null) {
            if (globalPosition.y < parent!!.globalPosition.y) {
                val fixDelta = globalPosition.y - parent!!.globalPosition.y
                localPosition.y -= fixDelta
                size.y += fixDelta
                updateGlobalPosition()
            }
        }
        childWindows.forEach {
            it.keepWithinParent()
        }
    }

//    private fun restrictToParent() {
//        if (parent != null) {
//            if (size.x > parent!!.size.x) {
//                size.x
//            }
//        }
//    }

}