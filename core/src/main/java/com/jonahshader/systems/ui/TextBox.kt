package com.jonahshader.systems.ui

import com.badlogic.gdx.math.Vector2

class TextBox(pos: Vector2, size: Vector2, minSize: Vector2) : Window(pos, size, minSize) {
    //TODO: text box may or many not be manually resizable. that functionality should
    //  probably be put in Window and have it configurable. it will be movable though
    //  it should auto calculate the size based on font size, line distance, and padding
}