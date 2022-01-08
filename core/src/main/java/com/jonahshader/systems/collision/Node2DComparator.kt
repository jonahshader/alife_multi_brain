package com.jonahshader.systems.collision

import com.jonahshader.systems.scenegraph.Node2D

class Node2DComparator : Comparator<Node2D> {
    override fun compare(o1: Node2D, o2: Node2D) =
        if (o1.globalPosition.x < o2.globalPosition.x) { -1 }
    else if (o1.globalPosition.x > o2.globalPosition.x) { 1 } else { 0 }
}