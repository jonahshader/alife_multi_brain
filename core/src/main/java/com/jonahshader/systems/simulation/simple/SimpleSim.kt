package com.jonahshader.systems.simulation.simple

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2

class SimpleSim {
    private val creatures = mutableListOf<SimpleCreature>()

    private val parentPos = Vector2()

    init {
        creatures.add(SimpleCreature())
    }

    fun update(dt: Float) {
        creatures.forEach {
            it.update(dt)
        }
    }

    fun render(batch: Batch) {
        creatures.forEach {
//            it.render(batch)
        }
    }
}