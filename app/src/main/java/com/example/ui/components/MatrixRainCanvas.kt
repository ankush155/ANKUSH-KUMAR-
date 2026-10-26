package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun MatrixRainCanvas(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF00FF66),
    alpha: Float = 0.25f
) {
    var tick by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(50)
            tick = (tick + 1) % 10000
        }
    }

    val characters = remember {
        "0123456789ABCDEFｦｱｳｴｵｶｷｹｺｻｼｽｾｿﾀﾂﾃﾅﾆﾇﾈﾊﾋﾎﾏﾐﾑﾒﾓﾔﾕﾗﾘﾜ#*+=-~<>λπ".toCharArray()
    }

    // Keep track of drop positions
    val drops = remember {
        IntArray(50) { Random.nextInt(-40, 0) }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val colWidth = 24f
        val numCols = (size.width / colWidth).toInt().coerceAtLeast(1)

        val paint = android.graphics.Paint().apply {
            this.color = android.graphics.Color.argb(
                (alpha * 255).toInt(),
                (color.red * 255).toInt(),
                (color.green * 255).toInt(),
                (color.blue * 255).toInt()
            )
            this.textSize = 20f
            this.typeface = android.graphics.Typeface.MONOSPACE
        }

        val leadPaint = android.graphics.Paint().apply {
            this.color = android.graphics.Color.argb(
                (alpha * 1.8f).coerceAtMost(1f * 255).toInt(),
                255, 255, 255
            )
            this.textSize = 22f
            this.typeface = android.graphics.Typeface.MONOSPACE
            this.isFakeBoldText = true
        }

        drawIntoCanvas { canvas ->
            for (i in 0 until numCols.coerceAtMost(drops.size)) {
                val x = i * colWidth
                val y = drops[i] * 24f

                // Draw lead bright char
                val leadChar = characters[Random.nextInt(characters.size)].toString()
                canvas.nativeCanvas.drawText(leadChar, x, y, leadPaint)

                // Draw trail chars
                for (trail in 1..8) {
                    val trailY = y - (trail * 24f)
                    if (trailY > 0 && trailY < size.height) {
                        val trailChar = characters[(tick + i + trail) % characters.size].toString()
                        canvas.nativeCanvas.drawText(trailChar, x, trailY, paint)
                    }
                }

                drops[i] = drops[i] + 1
                if (drops[i] * 24f > size.height && Random.nextFloat() > 0.95f) {
                    drops[i] = 0
                }
            }
        }
    }
}
