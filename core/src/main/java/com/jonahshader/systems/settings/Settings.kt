package com.jonahshader.systems.settings

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

object Settings {
    val settings = Properties()

    init {
        if(File("settings.properties").exists()) {
            settings.load(FileInputStream("settings.properties"))
        } else{
            settings["fullscreen"] = "false"
            settings["overall-volume"] = "1.0"
            settings["music-volume"] = "0.3"
            settings["sound-volume"] = "1.0"
        }
    }

    fun save(){
        settings.store(FileOutputStream("settings.properties"), "")
    }
}