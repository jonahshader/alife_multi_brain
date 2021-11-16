package com.jonahshader.systems.ui

import com.badlogic.gdx.math.MathUtils
import space.earlygrey.shapedrawer.ShapeDrawer

object CustomShapes {
    fun filledRoundedRect(drawer: ShapeDrawer, x: Float, y: Float, width: Float, height: Float, roundness: Float) {
        drawer.filledRectangle(x, y + roundness, width, height - 2*roundness)
        drawer.filledRectangle(x + roundness, y, width - 2*roundness, roundness)
        drawer.filledRectangle(x + roundness, y + height - roundness, width - 2*roundness, roundness)
        drawer.sector(x + roundness, y + roundness, roundness, MathUtils.PI, MathUtils.PI /2, 80)
        drawer.sector(x + width - roundness, y + roundness, roundness, 3* MathUtils.PI /2, MathUtils.PI /2, 80)
        drawer.sector(x + width - roundness, y + height - roundness, roundness, 0f, MathUtils.PI /2, 80)
        drawer.sector(x + roundness, y + height - roundness, roundness, MathUtils.PI /2, MathUtils.PI /2, 80)
    }
}