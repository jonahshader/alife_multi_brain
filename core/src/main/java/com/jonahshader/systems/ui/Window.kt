package com.jonahshader.systems.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.ScalingViewport
import com.jonahshader.MultiBrain
import ktx.math.minus
import ktx.math.plusAssign
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

open class Window {
    val size = Vector2()
    val localPosition = Vector2()
    val globalPosition = Vector2()
    val minSize = Vector2(15f, 15f)
    var draggingEnabled = true
    var resizingEnabled = true
    val edgeColor = Color(1f, 1f, 1f, 1f)
    val bodyColor = Color(.1f, .1f, .1f, 1f)
    protected val childWindows = mutableListOf<Window>()
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

    constructor(viewport: ScalingViewport) {
        this.localPosition.set(-viewport.worldWidth/2f, -viewport.worldHeight/2f)
        this.size.set(viewport.worldWidth, viewport.worldHeight)
        draggingEnabled = false
        resizingEnabled = false
        bodyColor.a = 0f
        edgeColor.a = 0f
    }

    constructor(pos: Vector2, size: Vector2, minSize: Vector2) {
        this.localPosition.set(pos)
        this.size.set(size)
        this.minSize.set(minSize)
    }

    fun update(dt: Float) {
        updateGlobalPosition()
        childWindows.forEach { it.update(dt) }
        childWindows.removeIf { it.remove }
    }

    open fun render(cam: OrthographicCamera, viewport: ScalingViewport) {
        drawContainer()
        childWindows.forEach { it.render(cam, viewport) }
    }

    protected fun drawContainer() {
        MultiBrain.shapeDrawer.setColor(edgeColor)
        if (edgeColor.a > 0f)
            MultiBrain.shapeDrawer.filledRectangle(globalPosition.x, globalPosition.y, size.x, size.y, Color(.8f, .8f, .8f, 1f))
        if (bodyColor.a > 0f)
            MultiBrain.shapeDrawer.filledRectangle(globalPosition.x + 2, globalPosition.y + 2, size.x - 4, size.y - 4, bodyColor)
    }

    fun addChildWindow(childWindow: Window) {
        childWindow.parent = this
        childWindows += childWindow
    }

    fun handleMouseInput(mouseStateChange: Boolean, mouseDown: Boolean, mousePos: Vector2) : Boolean {
        // first check all children to find the lowest window in the tree that could be dragged by this input
        var childToReorder: Window? = null
        for (w in childWindows.reversed()) {
            if (w.handleMouseInput(mouseStateChange, mouseDown, mousePos)) {
                childToReorder = w
                break
            }
        }
        if (childToReorder != null) {
            childWindows.remove(childToReorder)
            childWindows += childToReorder
            return true
        }
        // none of the children handled that, so see if that input is applicable to this instance

        if (mouseStateChange && mouseDown) {
            // check resize edges
            if (resizingEnabled) {
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
            }

            if (!resizing) {
                if (draggingEnabled &&
                    (mousePos.x in (globalPosition.x..(globalPosition.x + size.x))) &&
                    (mousePos.y in (globalPosition.y..(globalPosition.y + size.y)))
                ) {
                    dragging = true
                    pDragPos.set(mousePos)
                    return true
                }
                if (customMouseHandle(mousePos)) return true
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
                    keepWithinBounds(true, false)
                    keepWithinParent(true, false)
                } else {
                    localPosition.x += delta
                    size.x -= delta
                    moveChildren(-delta, 0f)
                    updateGlobalPosition()
                    keepWithinBounds(true, true)
                    keepWithinParent(true, true)
                }
            }
            if (yResizing) {
                val delta = mousePos.y - pDragPos.y
                if (yMaxResizing) {
                    size.y += delta
                    keepWithinBounds(false, false)
                    keepWithinParent(false, false)
                } else {
                    localPosition.y += delta
                    size.y -= delta
                    moveChildren(0f, -delta)
                    updateGlobalPosition()
                    keepWithinBounds(false, true)
                    keepWithinParent(false, true)
                }
            }
            pDragPos.set(mousePos)
        }

        return false
    }

    open fun customMouseHandle(mousePos: Vector2): Boolean = false

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

    /*TODO: need another function to not squash children. need to determine minimum bounding box of children
    (as they currently are, without rearranging them)
    TODO: also move this stuff back. maybe make the method take in two bools so it can determine which
    case is being fixed. right now it might break idk, and its less efficient.
    TODO: add minimum size to window
     */


    /**
     * xSide: true = x, false = y
     * minSide: true = min, false = max
     */
    private fun keepWithinParent(xSide: Boolean, minSide: Boolean) {
        updateGlobalPosition()
        if (parent != null) {
            if (xSide) {
                if (minSide) {
                    if (globalPosition.x < parent!!.globalPosition.x) {
                        val fixDelta = globalPosition.x - parent!!.globalPosition.x
                        localPosition.x -= fixDelta
                        size.x += fixDelta
//                        moveChildren(fixDelta, 0f)
                        updateGlobalPosition()
                    }
                } else {
                    if (globalPosition.x + size.x > parent!!.globalPosition.x + parent!!.size.x) {
                        size.x = parent!!.globalPosition.x + parent!!.size.x - globalPosition.x
                    }
                }
            } else {
                if (minSide) {
                    if (globalPosition.y < parent!!.globalPosition.y) {
                        val fixDelta = globalPosition.y - parent!!.globalPosition.y
                        localPosition.y -= fixDelta
                        size.y += fixDelta
//                        moveChildren(0f, fixDelta)
                        updateGlobalPosition()
                    }
                } else {
                    if (globalPosition.y + size.y > parent!!.globalPosition.y + parent!!.size.y) {
                        size.y = parent!!.globalPosition.y + parent!!.size.y - globalPosition.y
                    }
                }
            }
        }


        childWindows.forEach {
            it.keepWithinParent(xSide, minSide)
        }
    }

    private fun keepWithinBounds(xSide: Boolean, minSide: Boolean) {
        val bounds = getBounds()
        if (xSide) {
            if (minSide) {
                if (size.x < bounds.width) {
                    val fixDelta = size.x - bounds.width
                    localPosition.x += fixDelta
                    size.x -= fixDelta
                    moveChildren(-fixDelta, 0f)
                    updateGlobalPosition()
                }
            } else {
                if (globalPosition.x + size.x < bounds.x + bounds.width) {
                    size.x = bounds.x + bounds.width - globalPosition.x
                }
            }
        } else {
            if (minSide) {
                if (size.y < bounds.height) {
                    val fixDelta = size.y - bounds.height
                    localPosition.y += fixDelta
                    size.y -= fixDelta
                    moveChildren(0f, -fixDelta)
                    updateGlobalPosition()
                }
            } else {
                if (globalPosition.y + size.y < bounds.y + bounds.height) {
                    size.y = bounds.y + bounds.height - globalPosition.y
                }
            }
        }
    }

    private fun getBounds() : Rectangle {
        updateGlobalPosition()
        var xMin = globalPosition.x
        var xMax = globalPosition.x + max(size.x, minSize.x)
        var yMin = globalPosition.y
        var yMax = globalPosition.y + max(size.y, minSize.y)

        childWindows.forEach {
            val childBounds = it.getBounds()
            xMin = min(xMin, childBounds.x)
            xMax = max(xMax, childBounds.x + childBounds.width)
            yMin = min(yMin, childBounds.y)
            yMax = max(yMax, childBounds.y + childBounds.height)
        }
        return Rectangle(xMin, yMin, xMax - xMin, yMax - yMin)
    }

    private fun moveChildren(xm: Float, ym: Float) {
        childWindows.forEach {
            it.localPosition.x += xm
            it.localPosition.y += ym
            it.updateGlobalPosition()
        }
    }

}