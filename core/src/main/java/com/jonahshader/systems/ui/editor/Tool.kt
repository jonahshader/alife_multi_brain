package com.jonahshader.systems.ui.editor

import com.badlogic.gdx.math.Vector2

interface Tool {
    enum class MouseButton {
        LEFT,
        MIDDLE,
        RIGHT
    }
    fun mousePressed(pos: Vector2, button: MouseButton) {}
    fun mouseMoved(pos: Vector2) {}
    fun mouseReleased(pos: Vector2) {}
}