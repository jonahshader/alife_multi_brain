package com.jonahshader

import com.badlogic.gdx.Game
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.jonahshader.screens.MenuScreen
import com.jonahshader.systems.assets.Assets
import com.jonahshader.systems.screen.ScreenManager
import space.earlygrey.shapedrawer.ShapeDrawer


/** [com.badlogic.gdx.ApplicationListener] implementation shared by all platforms.  */
class MultiBrain : Game() {
    companion object {
        lateinit var batch: SpriteBatch
            private set
        lateinit var shapeRenderer: ShapeRenderer
            private set
        lateinit var shapeDrawer: ShapeDrawer
            private set
    }

    override fun create() {
        Assets.startLoading()
        Assets.finishLoading()
        batch = SpriteBatch()
        shapeRenderer = ShapeRenderer()
        shapeDrawer = ShapeDrawer(batch, Assets.getSprites().findRegion("white_pixel"))
        ScreenManager.game = this
        ScreenManager.push(MenuScreen())
    }
}