package com.jonahshader.systems.ui

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.ScalingViewport
import com.jonahshader.MultiBrain
import com.jonahshader.systems.utils.map
import kotlin.math.*

class Plot(private val xAxisLabel: String, private val yAxisLabel: String, private val title: String,
           pos: Vector2, size: Vector2 = Vector2(150f, 130f)) : Window(pos, size, Vector2(150f, 130f)) {
    companion object {
        private const val BOX_LEFT_PADDING = 50f
        private const val BOX_RIGHT_PADDING = 5f
        private const val BOX_TOP_PADDING = 20f
        private const val BOX_BOTTOM_PADDING = 50f
        private const val TREND_PADDING = 10f
        private const val LABEL_FONT_SIZE = 18f
        private const val TITLE_FONT_SIZE = 20f
        private const val Y_MIN_INTERVAL_SIZE = 12f
        private const val X_MIN_INTERVAL_SIZE = 24f
    }
    enum class Mode {
        POINT,
        LINE
    }
    class Trend(val label: String, private val color: Color, private val sorted: Boolean, private val mode: Mode = Mode.POINT) {
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

        fun render(cam: Camera, minVals: Vector2, maxVals: Vector2, bottomLeft: Vector2, topRight: Vector2) {
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
        MultiBrain.shapeDrawer.rectangle(globalPosition.x + BOX_LEFT_PADDING, globalPosition.y + BOX_BOTTOM_PADDING,
        size.x - BOX_RIGHT_PADDING - BOX_LEFT_PADDING, size.y - BOX_TOP_PADDING - BOX_BOTTOM_PADDING)

        // draw labels
        TextRenderer.begin(MultiBrain.batch, viewport, TextRenderer.Font.NORMAL, TITLE_FONT_SIZE, 0.1f, cam.zoom)
        TextRenderer.drawTextCentered(globalPosition.x + size.x/2, globalPosition.y + size.y - BOX_TOP_PADDING/2, title)
        TextRenderer.end()
        TextRenderer.begin(MultiBrain.batch, viewport, TextRenderer.Font.NORMAL, LABEL_FONT_SIZE, 0f, cam.zoom)
        TextRenderer.drawTextCentered(globalPosition.x + size.x/2, globalPosition.y + LABEL_FONT_SIZE/2, xAxisLabel)
        TextRenderer.end()
        TextRenderer.begin(MultiBrain.batch, viewport, TextRenderer.Font.NORMAL, LABEL_FONT_SIZE, 0f, cam.zoom, rotation = 90f)
        TextRenderer.drawTextCentered(globalPosition.x + LABEL_FONT_SIZE/2f, globalPosition.y + size.y/2, yAxisLabel)
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

        val dataMin = Vector2(xMinData, yMinData)
        val dataMax = Vector2(xMaxData, yMaxData)

        val bottomLeft = Vector2(globalPosition.x + BOX_LEFT_PADDING + TREND_PADDING, globalPosition.y + BOX_BOTTOM_PADDING + TREND_PADDING)
        val topRight = Vector2(globalPosition.x + size.x - BOX_RIGHT_PADDING - TREND_PADDING,
            globalPosition.y + size.y - BOX_TOP_PADDING - TREND_PADDING)

        // draw grid
        drawYValueMarkers(cam, viewport, dataMin, dataMax, bottomLeft, topRight)
        drawXValueMarkers(cam, viewport, dataMin, dataMax, bottomLeft, topRight)

        // draw charts
        trends.forEach { it.value.render(cam,
            dataMin, dataMax,
            bottomLeft, topRight) }
    }

    private fun drawYValueMarkers(cam: OrthographicCamera, viewport: ScalingViewport, dataMin: Vector2, dataMax: Vector2,
                                  renderMin: Vector2, renderMax: Vector2) {

        val minIntervals = (renderMax.y - renderMin.y) / Y_MIN_INTERVAL_SIZE
        val minIntervalSize = (dataMax.y - dataMin.y) / minIntervals
        val intervalSize = 2f.pow(ceil(log2(minIntervalSize)))
        val intervalRenderSize = (intervalSize / (dataMax.y - dataMin.y)) * (renderMax.y - renderMin.y)
        val intervals = ceil((renderMax.y - renderMin.y) / intervalRenderSize).roundToInt() + 2

        val firstDataInterval = floor(dataMin.y / intervalSize) * intervalSize
        MultiBrain.shapeDrawer.setColor(Color.GRAY)
        val fontSize = Y_MIN_INTERVAL_SIZE
        TextRenderer.begin(MultiBrain.batch, viewport, TextRenderer.Font.NORMAL, fontSize, 0f, cam.zoom)
        for (i in 0 until intervals) {
            val interval = firstDataInterval + intervalSize * i
            val intervalRender = map(interval, dataMin.y, dataMax.y, renderMin.y, renderMax.y)
            if (intervalRender in (globalPosition.y + BOX_BOTTOM_PADDING..globalPosition.y + size.y - BOX_TOP_PADDING)) {
                TextRenderer.drawText(
                    globalPosition.x + BOX_LEFT_PADDING * (3f / 6),
                    intervalRender - fontSize / 2,
                    interval.toString()
                ) //.take(3)
            }
        }
        TextRenderer.end()

        for (i in 0 until intervals) {
            val interval = firstDataInterval + intervalSize * i
            val intervalRender = map(interval, dataMin.y, dataMax.y, renderMin.y, renderMax.y)
            if (intervalRender in (globalPosition.y + BOX_BOTTOM_PADDING..globalPosition.y + size.y - BOX_TOP_PADDING)) {
                MultiBrain.shapeDrawer.line(
                    globalPosition.x + BOX_LEFT_PADDING,
                    intervalRender,
                    globalPosition.x + size.x - BOX_RIGHT_PADDING,
                    intervalRender,
                    1f
                )
            }
        }
    }

    private fun drawXValueMarkers(cam: OrthographicCamera, viewport: ScalingViewport, dataMin: Vector2, dataMax: Vector2,
                                  renderMin: Vector2, renderMax: Vector2) {
        val minIntervals = (renderMax.x - renderMin.x) / X_MIN_INTERVAL_SIZE
        val minIntervalSize = (dataMax.x - dataMin.x) / minIntervals
        val intervalSize = 2f.pow(ceil(log2(minIntervalSize)))
        val intervalRenderSize = (intervalSize / (dataMax.x - dataMin.x)) * (renderMax.x - renderMin.x)
        val intervals = ceil((renderMax.x - renderMin.x) / intervalRenderSize).roundToInt() + 2

        val firstDataInterval = floor(dataMin.x / intervalSize) * intervalSize
        MultiBrain.shapeDrawer.setColor(Color.GRAY)
        val fontSize = Y_MIN_INTERVAL_SIZE
        for (i in 0 until intervals) {
            val interval = firstDataInterval + intervalSize * i
            val intervalRender = map(interval, dataMin.x, dataMax.x, renderMin.x, renderMax.x)
            if (intervalRender in (globalPosition.x + BOX_LEFT_PADDING..globalPosition.x + size.x - BOX_RIGHT_PADDING)) {
                TextRenderer.begin(MultiBrain.batch, viewport, TextRenderer.Font.NORMAL, fontSize, 0f, cam.zoom, rotation = 45f)
                TextRenderer.drawTextCentered(intervalRender, globalPosition.y + BOX_BOTTOM_PADDING * (2f/3), interval.toString())
                TextRenderer.end()
            }
        }

        for (i in 0 until intervals) {
            val interval = firstDataInterval + intervalSize * i
            val intervalRender = map(interval, dataMin.x, dataMax.x, renderMin.x, renderMax.x)
            if (intervalRender in (globalPosition.x + BOX_LEFT_PADDING..globalPosition.x + size.x - BOX_RIGHT_PADDING)) {
                MultiBrain.shapeDrawer.line(
                    intervalRender,
                    globalPosition.y + BOX_BOTTOM_PADDING,
                    intervalRender,
                    globalPosition.y + size.y - BOX_TOP_PADDING,
                    1f
                )
            }
        }
    }
}