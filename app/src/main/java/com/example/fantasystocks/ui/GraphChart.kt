package com.example.fantasystocks.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun GraphChart(
    modifier: Modifier = Modifier,
    dataPoints: List<Pair<Float, Float>>, // List of (x, y) points
    lineColor: Color = Color.Blue,
    pointColor: Color = Color.Red,
    strokeWidth: Float = 4f,
    pointRadius: Float = 6f
) {
    if (dataPoints.isEmpty()) return

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Determine min/max for scaling
        val minX = dataPoints.minOf { it.first }
        val maxX = dataPoints.maxOf { it.first }
        val minY = dataPoints.minOf { it.second }
        val maxY = dataPoints.maxOf { it.second }

        // Scale function to map data points to canvas size
        fun scaleX(x: Float) = (x - minX) / (maxX - minX) * width
        fun scaleY(y: Float) = height - ((y - minY) / (maxY - minY) * height) // Invert Y-axis

        // Create the path for the line graph
        val path = Path().apply {
            val firstPoint = dataPoints.first()
            moveTo(scaleX(firstPoint.first), scaleY(firstPoint.second))
            dataPoints.drop(1).forEach { (x, y) ->
                lineTo(scaleX(x), scaleY(y))
            }
        }

        // Draw the line connecting the points
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Draw data points
        dataPoints.forEach { (x, y) ->
            drawCircle(
                color = pointColor,
                radius = pointRadius,
                center = Offset(scaleX(x), scaleY(y))
            )
        }
    }
}

@Preview
@Composable
fun GraphChartPreview() {
    val sampleData = listOf(
        1f to 10f,
        2f to 25f,
        3f to 5f,
        4f to 30f,
        5f to 15f,
        6f to 40f
    )

    Box(modifier = Modifier.size(300.dp, 200.dp)) {
        GraphChart(
            dataPoints = sampleData,
            lineColor = Color.Blue,
            pointColor = Color.Red,
            strokeWidth = 3f,
            pointRadius = 5f
        )
    }
}