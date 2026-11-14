package org.arabicchemistry.lab

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Color(0xFF0B5E8E), onPrimary = Color.White,
    primaryContainer = Color(0xFFD3EAF9), onPrimaryContainer = Color(0xFF062E47),
    secondary = Color(0xFF8A5F00), onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3EDF4), onSecondaryContainer = Color(0xFF233746),
    tertiary = Color(0xFF0F766E), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE7B0), onTertiaryContainer = Color(0xFF4A3300),
    background = Color(0xFFF3F7FA), onBackground = Color(0xFF12293A),
    surface = Color.White, onSurface = Color(0xFF12293A),
    surfaceVariant = Color(0xFFE3ECF2), onSurfaceVariant = Color(0xFF465A69),
    outline = Color(0xFF7C8F9D)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8BCDFF), onPrimary = Color(0xFF00344F),
    primaryContainer = Color(0xFF16405C), onPrimaryContainer = Color(0xFFD3EAF9),
    secondary = Color(0xFFE8C36C), onSecondary = Color(0xFF3A2A00),
    secondaryContainer = Color(0xFF263846), onSecondaryContainer = Color(0xFFDCE8F0),
    tertiary = Color(0xFF5FD0C5), onTertiary = Color(0xFF00201D),
    tertiaryContainer = Color(0xFF4E3B0C), onTertiaryContainer = Color(0xFFFFE7B0),
    background = Color(0xFF0E1620), onBackground = Color(0xFFE6EDF3),
    surface = Color(0xFF16212D), onSurface = Color(0xFFE6EDF3),
    surfaceVariant = Color(0xFF223142), onSurfaceVariant = Color(0xFFA9B8C6),
    outline = Color(0xFF6F8294)
)

private val BaseType = Typography()

internal val ChemTypography = Typography(
    headlineSmall = BaseType.headlineSmall.copy(fontSize = 24.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold),
    titleLarge = BaseType.titleLarge.copy(fontSize = 20.sp, lineHeight = 30.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = BaseType.titleMedium.copy(fontSize = 16.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = BaseType.bodyLarge.copy(fontSize = 16.sp, lineHeight = 28.sp),
    bodyMedium = BaseType.bodyMedium.copy(fontSize = 14.sp, lineHeight = 24.sp),
    bodySmall = BaseType.bodySmall.copy(fontSize = 12.sp, lineHeight = 19.sp),
    labelLarge = BaseType.labelLarge.copy(fontSize = 14.sp, lineHeight = 22.sp),
    labelMedium = BaseType.labelMedium.copy(fontSize = 12.sp, lineHeight = 18.sp),
    labelSmall = BaseType.labelSmall.copy(fontSize = 11.sp, lineHeight = 17.sp)
)

private val ChemShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
internal fun ChemTheme(dark: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = ChemTypography,
        shapes = ChemShapes,
        content = content
    )
}

/** Decorative banner: gradient with an atom drawing. Contains no text so it never duplicates screen labels. */
@Composable
internal fun HeroArt(modifier: Modifier = Modifier) {
    val a = MaterialTheme.colorScheme.primary
    val b = MaterialTheme.colorScheme.tertiary
    Canvas(modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(28.dp))) {
        drawRect(Brush.linearGradient(colors = listOf(a, b), start = Offset.Zero, end = Offset(size.width, size.height)))
        val white = Color.White
        val cx = size.width * 0.78f
        val cy = size.height * 0.5f
        val r = size.height * 0.40f
        drawCircle(color = white.copy(alpha = 0.92f), radius = r * 0.17f, center = Offset(cx, cy))
        for (i in 0 until 3) {
            rotate(degrees = i * 60f, pivot = Offset(cx, cy)) {
                drawOval(
                    color = white.copy(alpha = 0.55f),
                    topLeft = Offset(cx - r, cy - r * 0.38f),
                    size = Size(r * 2f, r * 0.76f),
                    style = Stroke(width = 3f)
                )
            }
        }
        drawCircle(color = white.copy(alpha = 0.18f), radius = size.height * 0.55f, center = Offset(size.width * 0.12f, size.height * 0.95f))
        drawCircle(color = white.copy(alpha = 0.12f), radius = size.height * 0.30f, center = Offset(size.width * 0.34f, size.height * 0.15f))
    }
}
