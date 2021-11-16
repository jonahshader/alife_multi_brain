package com.jonahshader.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.RandomXS128
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.FillViewport
import com.badlogic.gdx.utils.viewport.FitViewport
import com.jonahshader.MultiBrain
import com.jonahshader.systems.screen.ScreenManager
import com.jonahshader.systems.settings.Settings
import com.jonahshader.systems.sound.SoundSystem
import com.jonahshader.systems.ui.TextRenderer
import com.jonahshader.systems.ui.menu.Menu
import ktx.app.KtxScreen
import ktx.graphics.begin

class MenuScreen : KtxScreen {
    private val camera = OrthographicCamera()
    private val viewport = FitViewport(640f, 900f, camera)
    private val menu = Menu(TextRenderer.Font.HEAVY, camera, Vector2(), Vector2(500f, 90f))

    init {
        menu.addMenuItem("Tutorial") { }
        menu.addMenuItem("Singleplayer") { }
        menu.addMenuItem("Settings") { ScreenManager.push(SettingsScreen()) }
        menu.addMenuItem("Exit") { Gdx.app.exit()}

        if((Settings.settings["fullscreen"] as String).toBoolean()) Gdx.graphics.setFullscreenMode(Gdx.graphics.displayMode)
    }

    override fun show() {
        viewport.update(Gdx.graphics.width, Gdx.graphics.height)

        SoundSystem.playMenuMusic()
        println(viewport.toString())
    }

    override fun render(delta: Float) {
        ScreenUtils.clear(.25f, .25f, .25f, 1f)


        viewport.apply()

        MultiBrain.batch.begin(viewport.camera)
        TextRenderer.begin(MultiBrain.batch, viewport, TextRenderer.Font.HEAVY, 125f, 0.05f)
        TextRenderer.color = Color.WHITE
        TextRenderer.drawTextCentered(0f, viewport.worldHeight*.5f - 130f, "PVP:", 10f, 0.75f)
        TextRenderer.end()
        TextRenderer.begin(MultiBrain.batch, viewport, TextRenderer.Font.HEAVY, 75f, 0.05f)
        TextRenderer.color = Color.WHITE
        TextRenderer.drawTextCentered(0f, viewport.worldHeight*.5f-225f, "Plant Vs Plant", 4f, 0.75f)
        TextRenderer.end()
        menu.run(delta, viewport)
        menu.draw(MultiBrain.batch, MultiBrain.shapeDrawer, viewport)

        MultiBrain.batch.end()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }
}