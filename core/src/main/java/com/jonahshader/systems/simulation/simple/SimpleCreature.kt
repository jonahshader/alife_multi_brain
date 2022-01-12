package com.jonahshader.systems.simulation.simple

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.jonahshader.MultiBrain
import com.jonahshader.systems.collision.Bounded
import com.jonahshader.systems.scenegraph.Node2D
import com.jonahshader.systems.scenegraph.Physics2D

class SimpleCreature : Bounded() {
    companion object {
        const val RADIUS = 4f
    }

    private var age = 0.0
    private var bounds = Rectangle()
    private var colliders = mutableListOf<SimpleCreature>()
    private val temp = Vector2()

    override fun getBounds(): Rectangle {
        bounds.x = globalPosition.x - RADIUS
        bounds.y = globalPosition.y - RADIUS
        bounds.width = RADIUS * 2
        bounds.height = RADIUS * 2
        return bounds
    }

    override fun preUpdate(dt: Float) {


        age += dt
        colliders.clear()
    }

    override fun customRender(batch: Batch) {
        MultiBrain.shapeDrawer.setColor(1.0f, 1.0f, 1.0f, 1.0f)
        MultiBrain.shapeDrawer.filledCircle(globalPosition, RADIUS)
    }

    fun checkCollision(otherCreature: SimpleCreature) {
        if (temp.set(otherCreature.globalPosition).sub(globalPosition).len2() < RADIUS * RADIUS * 4) {
            colliders += otherCreature
            otherCreature.colliders += this
        }
    }
}