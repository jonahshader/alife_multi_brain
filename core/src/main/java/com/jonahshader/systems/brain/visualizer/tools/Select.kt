package com.jonahshader.systems.brain.visualizer.tools

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.jonahshader.systems.ui.editor.Selectable
import com.jonahshader.systems.ui.editor.Tool

class Select : Tool {
    private val selectionRect = Rectangle()
    private val selected = mutableListOf<Selectable>()

    override fun mousePressed(pos: Vector2, button: Tool.MouseButton) {
        selectionRect.x = pos.x
        selectionRect.y = pos.y
    }

    override fun mouseMoved(pos: Vector2) {

    }

    override fun mouseReleased(pos: Vector2) {

    }
}