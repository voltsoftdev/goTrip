package coom.moosik.mooo.composable

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import coom.moosik.mooo.R

private val DarkColorPalette = darkColors(
    primary = Purple200,
    primaryVariant = Purple700,
    secondary = Teal200
)

private val LightColorPalette = lightColors(
    primary = Purple500,
    primaryVariant = Purple700,
    secondary = Teal200
)

val notoSansFonts = FontFamily(
    Font(R.font.notosans_kr_regular),
    Font(R.font.notosans_kr_regular, weight = FontWeight.Normal),
    Font(R.font.notosans_kr_bold, weight = FontWeight.Bold),
    Font(R.font.notosans_kr_light, weight = FontWeight.Light),
    Font(R.font.notosans_kr_thin, weight = FontWeight.Thin))

val poppinsFonts = FontFamily(
    Font(R.font.poppins_regular),
    Font(R.font.poppins_regular, weight = FontWeight.Normal),
    Font(R.font.poppins_bold, weight = FontWeight.Bold),
    Font(R.font.poppins_semibold, weight = FontWeight.SemiBold))

@Composable
fun ViewModelDemoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        shapes = Shapes,
        content = content
    )
}