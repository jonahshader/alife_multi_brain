package com.jonahshader.systems.brain.visualizer

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.jonahshader.MultiBrain
import com.jonahshader.systems.scenegraph.Node2D

abstract class NeuronGraphic : Node2D() {
    val weights = mutableListOf<WeightGraphic>()
    val color = Color(1f, 1f, 1f, 1f)
    var radius = 32f

    override fun customUpdate(dt: Float) {
        TODO("Update weight spring physics. also slow down ")
    }

    override fun customRender(batch: Batch) {
        MultiBrain.shapeDrawer.setColor(color)
        MultiBrain.shapeDrawer.filledCircle(globalPosition, radius)
    }
}