package com.jonahshader.systems.simulation.simple

import com.badlogic.gdx.graphics.g2d.Batch
import com.jonahshader.MultiBrain
import com.jonahshader.systems.scenegraph.Node2D
import com.jonahshader.systems.scenegraph.Physics2D

class SimpleCreature : Physics2D() {
    companion object {
        const val RADIUS = 8f
    }

    override fun customUpdate(dt: Float) {

    }

    override fun customRender(batch: Batch) {
        MultiBrain.shapeDrawer.setColor(1.0f, 1.0f, 1.0f, 1.0f)
        MultiBrain.shapeDrawer.filledCircle(globalPosition, RADIUS)
    }
}