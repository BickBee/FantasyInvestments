package com.example.fantasystocks.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import java.text.DecimalFormat
import kotlin.math.ceil
import kotlin.math.floor

private const val Y_STEP = 10.0

private val RangeProvider = object : CartesianLayerRangeProvider {
    override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore) =
        Y_STEP * floor(minY / Y_STEP)
    override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore) =
        Y_STEP * ceil(maxY / Y_STEP)
}

private val StartAxisValueFormatter = CartesianValueFormatter.decimal(DecimalFormat("$#,###"))
private val StartAxisItemPlacer = VerticalAxis.ItemPlacer.step({ Y_STEP })

@Composable
fun StockGraph(
    stockData: List<Double>,
    modifier: Modifier = Modifier,
    height: Int = 220
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(stockData) {
        modelProducer.runTransaction {
            lineSeries { series(stockData) }
        }
    }

    CartesianChartHost(
        rememberCartesianChart(
            rememberLineCartesianLayer(
                rangeProvider = RangeProvider
            ),
            startAxis = VerticalAxis.rememberStart(
                valueFormatter = StartAxisValueFormatter,
                itemPlacer = StartAxisItemPlacer
            )
        ),
        modelProducer,
        modifier.height(height.dp)
    )
}