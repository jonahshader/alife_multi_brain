package com.jonahshader.systems.ecs

import com.artemis.Component
import com.badlogic.gdx.math.Vector2

class Position(val pos: Vector2) : Component()
class Velocity(val vel: Vector2) : Component()
class Force(val force: Vector2) : Component()
class Mass(var mass: Float) : Component()

class Circle(var radius: Float) : Component()