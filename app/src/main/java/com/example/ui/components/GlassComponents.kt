package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ThemeCardBorder
import com.example.ui.theme.ThemeCardFill

/**
 * Reusable layout card expressing high-fidelity glassmorphism.
 * Includes translucent frosted-glass content overlays and thin glowing border trims.
 */
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderWidth: Dp = 1.dp,
    borderColor: Color = ThemeCardBorder,
    fillColor: Color = ThemeCardFill,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(fillColor)
            .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * High-performance, Canvas-drawn circular telemetry progress indicator
 * featuring primary and secondary track sweeps, rounded cap metrics, 
 * and ambient numeric center readouts.
 */
@Composable
fun CircularMetricRing(
    percentage: Float,
    title: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 14.dp,
    iconSection: @Composable (ColumnScope.() -> Unit)? = null
) {
    val animatedProgress = animateFloatAsState(
        targetValue = percentage.coerceIn(0f, 100f) / 100f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "CircularProgress"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val sizeMin = size.minDimension
                val strokePx = strokeWidth.toPx()
                val radius = (sizeMin - strokePx) / 2
                
                // Track 1: Dark translucent background ring (low-opacity container)
                drawCircle(
                    color = accentColor.copy(alpha = 0.1f),
                    radius = radius,
                    style = Stroke(width = strokePx)
                )

                // Track 2: Bright glowing foreground indicator
                drawArc(
                    color = accentColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress.value,
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (iconSection != null) {
                    iconSection()
                } else {
                    Text(
                        text = "${(animatedProgress.value * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * Custom line chart drawn using an optimized Bezier Curve path on Canvas.
 * Ideal to display metric timeline history dynamically within widgets.
 */
@Composable
fun MetricMiniSparkline(
    dataHistory: List<Int>,
    lineColor: Color,
    modifier: Modifier = Modifier,
    minVal: Float = 0f,
    maxVal: Float = 100f
) {
    if (dataHistory.size < 2) return

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val valRange = (maxVal - minVal).coerceAtLeast(1f)

        val points = dataHistory.mapIndexed { idx, value ->
            val x = (idx.toFloat() / (dataHistory.size - 1)) * width
            val normalizedY = (value.toFloat() - minVal) / valRange
            val y = height - (normalizedY * height * 0.85f) - (height * 0.07f)
            Offset(x, y)
        }

        val strokePath = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }
        }

        // Fill path under sparkline
        val fillPath = Path().apply {
            addPath(strokePath)
            if (points.isNotEmpty()) {
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
        }

        // Draw translucent gradient fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.22f), Color.Transparent),
                startY = 0f,
                endY = height
            )
        )

        // Draw line trace
        drawPath(
            path = strokePath,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}
