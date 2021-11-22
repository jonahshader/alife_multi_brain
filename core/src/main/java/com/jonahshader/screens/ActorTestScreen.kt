package com.jonahshader.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.FitViewport
import com.jonahshader.MultiBrain
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.graphics.use

class ActorTestScreen : KtxScreen {
    private val viewport = FitViewport(1280f, 720f)
    private val stage = Stage(viewport, MultiBrain.batch)

    val t = TestActor()
    val s = TestActor()

    class TestActor : Group() {
        override fun draw(batch: Batch, parentAlpha: Float) {
            if (hasParent()) {
                MultiBrain.shapeDrawer.setColor(1f, 1f, 1f, 1f)
                MultiBrain.shapeDrawer.filledCircle(x + parent.x, y + parent.y, 32f)
            } else {
                MultiBrain.shapeDrawer.setColor(1f, 1f, 1f, 1f)
                MultiBrain.shapeDrawer.filledCircle(x, y, 32f)
            }

        }
    }

    init {

        s.x = 64f
        t.addActor(s)
        stage.addActor(s)
        stage.addActor(t)
    }

    override fun show() {
        viewport.update(Gdx.graphics.width, Gdx.graphics.height)

    }

    override fun render(delta: Float) {
        clearScreen(.25f, .25f, .25f)

        viewport.apply()

        stage.act(delta)
        stage.draw()
//        MultiBrain.batch.use(camera) {
//            MultiBrain.shapeDrawer.setColor(1f, 1f, 1f, 1f)
//            MultiBrain.shapeDrawer.filledCircle(0f, 0f, 32f)
//        }

        t.x += delta * 32
    }
}