package com.jonahshader.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.utils.viewport.FitViewport
import ktx.app.KtxScreen
import ktx.app.clearScreen

class Box2DTestScreen : KtxScreen {
    val world = World(Vector2(0f, -10f), true)
    val debugRenderer = Box2DDebugRenderer()

    val camera = OrthographicCamera()
    val viewport = FitViewport(128f, 80f, camera)




    init {
        val bodyDef = BodyDef()
        // todo: document
        // https://github.com/libgdx/libgdx/wiki/Box2d
        bodyDef.type = BodyDef.BodyType.DynamicBody
        bodyDef.position.set(5f, 10f)
        val body = world.createBody(bodyDef)
        val circle = CircleShape()
        circle.radius = 6f
        val fixtureDef = FixtureDef()
        fixtureDef.shape = circle
        fixtureDef.density = .5f
        fixtureDef.friction = .4f
        fixtureDef.restitution = .6f // bounciness
        val fixture = body.createFixture(fixtureDef)
        circle.dispose()
    }

    override fun show() {
        viewport.update(Gdx.graphics.width, Gdx.graphics.height)
    }

    override fun render(delta: Float) {
        clearScreen(0f, 0f, 0f)

        viewport.apply()

        world.step(1/60f, 6, 2)



        debugRenderer.render(world, camera.combined)
    }

}