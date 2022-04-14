package com.jonahshader.systems.assets

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.Disposable
import com.jonahshader.systems.sound.SoundSystem

object Assets : Disposable {
    // Fonts
    private const val LIGHT_FONT = "graphics/fonts/light_font"
    const val LIGHT_FONT_SPREAD = 3f
    const val LIGHT_FONT_SIZE = 64f
    private lateinit var lightFontTexture: Texture
    lateinit var lightFont: BitmapFont
    private const val NORMAL_FONT = "graphics/fonts/normal_font"
    const val NORMAL_FONT_SPREAD = 4f
    const val NORMAL_FONT_SIZE = 64f
    private lateinit var normalFontTexture: Texture
    lateinit var normalFont: BitmapFont
    private const val HEAVY_FONT = "graphics/fonts/heavy_font"
    const val HEAVY_FONT_SPREAD = 6f
    const val HEAVY_FONT_SIZE = 64f
    private lateinit var heavyFontTexture: Texture
    lateinit var heavyFont: BitmapFont

    // Sounds
    const val MENU_OPEN_SOUND = "audio/sounds/open.ogg"
    const val MENU_CLOSE_SOUND = "audio/sounds/close.ogg"
    const val MENU_MOUSE_OVER_SOUND = "audio/sounds/menu_mouseover.ogg"
    const val DONE_GROWING_SOUND = "audio/sounds/done_growing.ogg"
    const val LIMB_GROW_SOUND = "audio/sounds/limb_grow.ogg"

    // Music
    const val GAME_MUSIC = "audio/music/ingame.ogg"
    const val MENU_MUSIC = "audio/music/menu.ogg"

    // Sprites
    private const val SPRITES = "graphics/spritesheets/sprites.atlas"

    // Shaders
    private const val DFF_SHADER = "graphics/shaders/dff"
    lateinit var dffShader: ShaderProgram
        private set

    val manager = AssetManager()

    fun getSprites() : TextureAtlas {
        return manager.get(SPRITES)
    }

    fun startLoading() {
        loadFonts()
        loadTextures()
        loadShaders()
        loadSounds()
    }

    fun getProgress() : Float = manager.progress

    fun finishLoading() {
        manager.finishLoading()
        SoundSystem.loadMusic()
    }

    private fun loadShaders() {
        dffShader = ShaderProgram(Gdx.files.internal("$DFF_SHADER.vert"), Gdx.files.internal("$DFF_SHADER.frag"))
        if (!dffShader.isCompiled) {
            Gdx.app.error("dffShader", "compilation failed:\n" + dffShader.log)
        }
    }

    private fun loadFonts() {
        lightFontTexture = Texture(Gdx.files.internal("$LIGHT_FONT.png"), true)
        heavyFontTexture = Texture(Gdx.files.internal("$HEAVY_FONT.png"), true)
        normalFontTexture = Texture(Gdx.files.internal("$NORMAL_FONT.png"), true)

        lightFontTexture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear)
        heavyFontTexture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear)
        normalFontTexture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear)

        lightFont = BitmapFont(Gdx.files.internal("$LIGHT_FONT.fnt"), TextureRegion(lightFontTexture), false)
        heavyFont = BitmapFont(Gdx.files.internal("$HEAVY_FONT.fnt"), TextureRegion(heavyFontTexture), false)
        normalFont = BitmapFont(Gdx.files.internal("$NORMAL_FONT.fnt"), TextureRegion(normalFontTexture), false)
    }

    private fun loadTextures() {
        manager.load(SPRITES, TextureAtlas::class.java)
    }

    private fun loadSounds() {
        manager.load(MENU_OPEN_SOUND, Sound::class.java)
        manager.load(MENU_CLOSE_SOUND, Sound::class.java)
        manager.load(MENU_MOUSE_OVER_SOUND, Sound::class.java)
        manager.load(DONE_GROWING_SOUND, Sound::class.java)
        manager.load(LIMB_GROW_SOUND, Sound::class.java)

        manager.load(GAME_MUSIC, Music::class.java)
        manager.load(MENU_MUSIC, Music::class.java)
    }

    override fun dispose() {
        manager.dispose()
        lightFontTexture.dispose()
        lightFont.dispose()
        heavyFontTexture.dispose()
        heavyFont.dispose()
    }
}