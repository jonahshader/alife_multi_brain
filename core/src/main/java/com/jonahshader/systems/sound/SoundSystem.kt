package com.jonahshader.systems.sound

import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.math.Vector2
import com.jonahshader.systems.assets.Assets
import com.jonahshader.systems.settings.Settings
import kotlin.math.pow

object SoundSystem {
    var overallVolume = (Settings.settings["overall-volume"] as String).toFloat()
        set(value) {
            field = value
            updateVolume()
        }
    var soundVolume = (Settings.settings["sound-volume"] as String).toFloat()
    var musicVolume = (Settings.settings["music-volume"] as String).toFloat()
        set(value) {
            field = value
            updateVolume()
        }
    lateinit var camera: Camera

    lateinit var menuMusic: Music
    lateinit var gameMusic: Music
    /**
     * play sound with calculated panning related to sound and camera position
     * if sound position is left of camera position the panning is negative
     * if sound position is right of camera position the panning is positive
     *
     * @param s              the sound play
     * @param soundPosition  the position of the sound in game world
     * @param volume         the range (0,1) to play the sound in the game
     */
    fun playSoundInWorld(s: Sound, soundPosition: Vector2, volume: Float, pitch: Float) : Long {
        val currentWidth = camera.viewportWidth
        var pan: Float = (soundPosition.x - camera.position.x) / (currentWidth * .8f)
        var volScale: Float =
            1 - Vector2.len(soundPosition.x - camera.position.x, soundPosition.y - camera.position.y) / currentWidth
        volScale = 0f.coerceAtLeast(volScale).toDouble().pow(1.5).toFloat()
        pan = (-1f).coerceAtLeast(pan)
        pan = 1f.coerceAtMost(pan)
//        if (volScale < 0.01) return -1  // early return if the sound is barely audible. don't want entire world to emit sounds all the time
        val realVolume = volume * overallVolume * soundVolume * volScale
        return s.play(realVolume, pitch, pan)
    }

    fun playSoundStandalone(s: Sound, volume: Float, pan: Float) {
        val realVolume = volume * overallVolume * soundVolume
        s.play(realVolume, 1f, pan)
    }

    fun loadMusic() {
        menuMusic = Assets.manager.get(Assets.MENU_MUSIC)
        gameMusic = Assets.manager.get(Assets.GAME_MUSIC)
    }

    fun playMenuMusic() {
        gameMusic.stop()
        if (!menuMusic.isPlaying) {
            menuMusic.isLooping = true
            updateVolume()
            menuMusic.play()
        }
    }

    fun playGameMusic() {
        stopMusic()
        gameMusic.isLooping = true
        updateVolume()
        gameMusic.play()
    }

    fun stopMusic() {
        gameMusic.stop()
        menuMusic.stop()
    }

    private fun updateVolume() {
        gameMusic.volume = overallVolume * musicVolume
        menuMusic.volume = overallVolume * musicVolume
    }
}