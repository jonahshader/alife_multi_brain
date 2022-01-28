package com.jonahshader.systems.ui.editor

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.jonahshader.systems.ui.editor.Action

interface Selectable {
    fun trySelectSolo(pos: Vector2, toleranceRadius: Float) : Boolean // probably generate some signed distance field and use the toleranceRadius to offset this
    fun trySelectInclusive(selection: Rectangle) : Boolean // selections that only partially cover selectable are included
    fun trySelectExclusive(selection: Rectangle) : Boolean // selections that only partially cover selectable are excluded

    fun getActions() : List<Action>
}