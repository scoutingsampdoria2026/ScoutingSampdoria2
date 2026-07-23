package com.scoutingsampdoria.persone2.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object SampColors {
    val Blu = Color(0xFF003D7A)
    val BluScuro = Color(0xFF00285A)
    val BluChiaro = Color(0xFF4A90D9)
    val BluNebbia = Color(0xFFEAF2FC)
    val Rosso = Color(0xFFD8232A)
    val RossoSoft = Color(0xFFFCE7E8)
    val Success = Color(0xFF2E7D5B)
    val Warning = Color(0xFFE8A317)
    val ErrorColor = Color(0xFFC0392B)
    val Info = Color(0xFF3D8FB5)
    val Nero = Color(0xFF1A2332)
    val TestoSecondario = Color(0xFF5C6672)
    val TestoMuto = Color(0xFF8A94A0)
    val Divisore = Color(0xFFE1E5EB)
    val SfondoChiaro = Color(0xFFF7F8FA)
    val Superficie = Color(0xFFFFFFFF)
    val SuperficieAlt = Color(0xFFF0F2F5)
    val Bianco = Color(0xFFFFFFFF)
    val CardChiaro = Superficie
    val GrigioTesto = TestoSecondario
}

private val LightColors = lightColorScheme(
    primary = SampColors.Blu,
    onPrimary = SampColors.Bianco,
    primaryContainer = SampColors.BluNebbia,
    onPrimaryContainer = SampColors.Blu,
    secondary = SampColors.Rosso,
    onSecondary = SampColors.Bianco,
    secondaryContainer = SampColors.RossoSoft,
    onSecondaryContainer = SampColors.Rosso,
    tertiary = SampColors.Info,
    onTertiary = SampColors.Bianco,
    background = SampColors.SfondoChiaro,
    onBackground = SampColors.Nero,
    surface = SampColors.Superficie,
    onSurface = SampColors.Nero,
    surfaceVariant = SampColors.SuperficieAlt,
    onSurfaceVariant = SampColors.TestoSecondario,
    outline = SampColors.Divisore,
    outlineVariant = SampColors.Divisore,
    error = SampColors.ErrorColor,
    onError = SampColors.Bianco,
)

private val DarkColors = darkColorScheme(
    primary = SampColors.BluChiaro,
    onPrimary = SampColors.Bianco,
    primaryContainer = SampColors.BluScuro,
    onPrimaryContainer = SampColors.Bianco,
    secondary = SampColors.Rosso,
    onSecondary = SampColors.Bianco,
    background = Color(0xFF0E1621),
    onBackground = SampColors.Bianco,
    surface = Color(0xFF16222F),
    onSurface = SampColors.Bianco,
    surfaceVariant = Color(0xFF223140),
    onSurfaceVariant = Color(0xFFB8C4D0),
    error = SampColors.ErrorColor,
)

private val Typography = Typography(
    displaySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 28.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 18.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    titleSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 0.5.sp),
)

@Composable
fun SampdoriaTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
