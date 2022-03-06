package com.jonahshader.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.FitViewport
import com.jonahshader.MultiBrain
import com.jonahshader.systems.neuralnet.densecyclic.DenseCyclicNetwork
import com.jonahshader.systems.screen.ScreenManager
import com.jonahshader.systems.settings.Settings
import com.jonahshader.systems.simulation.foodgrid.FoodCreature
import com.jonahshader.systems.simulation.selectmove.SelectMove
import com.jonahshader.systems.training.EvolutionStrategies
import com.jonahshader.systems.ui.TextRenderer
import com.jonahshader.systems.ui.menu.Menu
import com.jonahshader.systems.utils.Rand
import ktx.app.KtxScreen
import ktx.graphics.use

class SimulationMenuScreen : KtxScreen {
    private val camera = OrthographicCamera()
    private val viewport = FitViewport(800f, 1500f, camera)
    private val menu = Menu(TextRenderer.Font.HEAVY, camera, Vector2(0f, 180f), Vector2(500f, 90f))

    init {
//        menu.addMenuItem("Box2D Test") { ScreenManager.push(Box2DTestScreen()) }
//        menu.addMenuItem("Food Task") { ScreenManager.push(FoodCreatureTestScreen()) }
        menu.addMenuItem("Food Task") {
            val sim = EvolutionStrategies(DenseCyclicNetwork.makeBuilder(55), FoodCreature.builder,
                200, 50, 700, 1/20f,
                algo = EvolutionStrategies.Algo.EsGDM, printFitness = false, rand = Rand.randx)
            ScreenManager.push(SimViewerScreen(sim, 1))
        }
        menu.addMenuItem("SB Task") { ScreenManager.push(SBCreatureTestScreen()) }
        menu.addMenuItem("Ball Push Task") { ScreenManager.push(SimViewerScreen(
            EvolutionStrategies(DenseCyclicNetwork.makeBuilder(60),
                SelectMove.defaultBuilder, 400, 100, 50, 1/30f,
                algo = EvolutionStrategies.Algo.EsGDM), 20)) }
        menu.addMenuItem("Back") { ScreenManager.pop() }

//        if((Settings.settings["fullscreen"] as String).toBoolean()) Gdx.graphics.setFullscreenMode(Gdx.graphics.displayMode)
    }

    override fun show() {
        viewport.update(Gdx.graphics.width, Gdx.graphics.height)
    }

    override fun render(delta: Float) {
        ScreenUtils.clear(.25f, .25f, .25f, 1f)


        viewport.apply()

        MultiBrain.batch.use(camera) {
//            TextRenderer.begin(MultiBrain.batch, viewport, TextRenderer.Font.HEAVY, 125f, 0.05f)
//            TextRenderer.color = Color.WHITE
//            TextRenderer.drawTextCentered(0f, viewport.worldHeight*.5f - 130f, "PVP:", 10f, 0.75f)
//            TextRenderer.end()
//            TextRenderer.begin(MultiBrain.batch, viewport, TextRenderer.Font.HEAVY, 75f, 0.05f)
//            TextRenderer.color = Color.WHITE
//            TextRenderer.drawTextCentered(0f, viewport.worldHeight*.5f-225f, "Plant Vs Plant", 4f, 0.75f)
//            TextRenderer.end()
            menu.run(delta, viewport)
            menu.draw(MultiBrain.batch, MultiBrain.shapeDrawer, viewport)

        }

    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }
}