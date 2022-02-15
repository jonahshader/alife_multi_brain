package com.jonahshader.systems.ui

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.ScalingViewport
import com.jonahshader.MultiBrain
import com.jonahshader.systems.utils.map
import kotlin.math.max
import kotlin.math.min

class Plot(private val xAxisLabel: String, private val yAxisLabel: String, private val title: String, private val mode: Mode = Mode.POINT,
           pos: Vector2, size: Vector2 = Vector2(150f, 100f)) : Window(pos, size, Vector2(100f, 70f)) {
    companion object {
        private const val BOX_PADDING = 20f
        private const val TREND_PADDING = 8f
        private const val LABEL_FONT_SIZE = 18f
        private const val TITLE_FONT_SIZE = 20f
    }
    enum class Mode {
        POINT,
        LINE
    }
    class Trend(val label: String, private val color: Color, private val sorted: Boolean) {
        companion object {
            private const val LINE_THICKNESS = 1f
            private const val POINT_RADIUS = 1f

        }

        val dataMin = Vector2(Float.MAX_VALUE, Float.MAX_VALUE)
        val dataMax = Vector2(-Float.MAX_VALUE, -Float.MAX_VALUE)
        private val data = mutableListOf<Vector2>()

        fun addDatum(datum: Vector2) {
            data += datum
            if (sorted) data.sortBy { it.x }

            dataMin.x = min(datum.x, dataMin.x)
            dataMin.y = min(datum.y, dataMin.y)
            dataMax.x = max(datum.x, dataMax.x)
            dataMax.y = max(datum.y, dataMax.y)
        }

        fun render(cam: Camera, mode: Mode, minVals: Vector2, maxVals: Vector2, bottomLeft: Vector2, topRight: Vector2) {
            MultiBrain.shapeDrawer.setColor(color)
            when (mode) {
                Mode.POINT -> renderPointMode(cam, minVals, maxVals, bottomLeft, topRight)
                Mode.LINE -> renderLineMode(cam, minVals, maxVals, bottomLeft, topRight)
            }
        }

        private fun renderPointMode(cam: Camera, minVals: Vector2, maxVals: Vector2, minRender: Vector2, maxRender: Vector2) {
            for (d in data) {
                val xMapped = map(d.x, minVals.x, maxVals.x, minRender.x, maxRender.x)
                val yMapped = map(d.y, minVals.y, maxVals.y, minRender.y, maxRender.y)
                MultiBrain.shapeDrawer.filledCircle(xMapped, yMapped, POINT_RADIUS)
            }
        }

        private fun renderLineMode(cam: Camera, minVals: Vector2, maxVals: Vector2, minRender: Vector2, maxRender: Vector2) {
            for (i in 0 until data.size - 1) {
                val xMapped = map(data[i].x, minVals.x, maxVals.x, minRender.x, maxRender.x)
                val yMapped = map(data[i].y, minVals.y, maxVals.y, minRender.y, maxRender.y)

                val xMapped2 = map(data[i + 1].x, minVals.x, maxVals.x, minRender.x, maxRender.x)
                val yMapped2 = map(data[i + 1].y, minVals.y, maxVals.y, minRender.y, maxRender.y)
                MultiBrain.shapeDrawer.line(xMapped, yMapped, xMapped2, yMapped2, LINE_THICKNESS)
            }
        }
    }

    private val trends = HashMap<String, Trend>()

    fun addDatum(trendName: String, datum: Vector2) {
        trends[trendName]?.addDatum(datum)
    }

    fun addTrend(trend: Trend) {
        trends[trend.label] = trend
    }

    override fun render(cam: OrthographicCamera, viewport: ScalingViewport) {
        drawContainer()

        // draw chart background
        MultiBrain.shapeDrawer.setColor(Color.BLACK)
        MultiBrain.shapeDrawer.rectangle(globalPosition.x + BOX_PADDING, globalPosition.y + BOX_PADDING,
        size.x - BOX_PADDING * 2, size.y - BOX_PADDING * 2)

        // draw labels
        TextRenderer.begin(MultiBrain.batch, viewport, TextRenderer.Font.NORMAL, LABEL_FONT_SIZE, 0f, cam.zoom)
        TextRenderer.drawTextCentered(globalPosition.x + size.x/2, globalPosition.y + LABEL_FONT_SIZE/2, xAxisLabel)
        TextRenderer.end()
        TextRenderer.begin(MultiBrain.batch, viewport, TextRenderer.Font.NORMAL, TITLE_FONT_SIZE, 0.1f, cam.zoom)
        TextRenderer.drawTextCentered(globalPosition.x + size.x/2, globalPosition.y + size.y - TITLE_FONT_SIZE/2, title)
        TextRenderer.end()

        // determine minRender and maxRender
        var xMinData = trends.values.minOf { it.dataMin.x }
        var yMinData = trends.values.minOf { it.dataMin.y }
        var xMaxData = trends.values.maxOf { it.dataMax.x }
        var yMaxData = trends.values.maxOf { it.dataMax.y }

        if (xMinData == xMaxData) {
            xMinData -= .5f
            xMaxData += .5f
        }
        if (yMinData == yMaxData) {
            yMinData -= .5f
            yMaxData += .5f
        }


        // draw charts
        trends.forEach { it.value.render(cam, mode,
            Vector2(xMinData, yMinData), Vector2(xMaxData, yMaxData),
            Vector2(globalPosition.x + BOX_PADDING + TREND_PADDING, globalPosition.y + BOX_PADDING + TREND_PADDING),
            Vector2(globalPosition.x + size.x - BOX_PADDING - TREND_PADDING, globalPosition.y + size.y - BOX_PADDING - TREND_PADDING)) }
    }


}