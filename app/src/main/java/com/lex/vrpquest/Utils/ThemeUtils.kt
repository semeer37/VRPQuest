package com.lex.vrpquest.Utils

import android.content.Context
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color


val ThemeOptions = listOf("dark", "light", "transparent", "new dark")
val DefaultTheme =  "new dark"

fun SetTheme(context:Context) { //Sets the theme based on settings value
    var themeid = SettingGetSting(context, "theme") ?: DefaultTheme

    when(themeid) {
        "dark" -> { CustomColorScheme = DarkTheme }
        "light" -> { CustomColorScheme = LightTheme }
        "transparent" -> { CustomColorScheme = TransparentTheme }
        "new dark" -> { CustomColorScheme = NewDarkTheme }
    }
}

var CustomColorScheme by mutableStateOf(
    darkColorScheme(
        //background = Color(0, 0, 0, 100),
        background = Color(27, 43, 59, 255), //Color(27, 43, 59, 255),
        onBackground = Color(27, 43, 59, 255),
        surface = Color(50, 63, 76, 255),
        error = Color(187, 0, 19, 255),
        onSurface = Color(240, 244, 245, 255),
        onSurfaceVariant = Color(54, 63, 78, 255),
        tertiary = Color(3, 100, 237, 255),
        surfaceVariant = Color(240, 244, 245, 255)
    )
)

var DarkTheme =
    darkColorScheme(
        background = Color(27, 43, 59, 255),
        onBackground = Color(27, 43, 59, 255),
        surface = Color(50, 63, 76, 255),
        onSurface = Color(240, 244, 245, 255),
        tertiary = Color(3, 100, 237, 255),
        error = Color(187, 0, 19, 255),
        surfaceVariant = Color(27, 43, 59, 255)
    )

var LightTheme =
    darkColorScheme(
        background = Color(191, 197, 209, 255),
        onBackground = Color(191, 197, 209, 255),
        surface = Color(182, 189, 199, 255),
        onSurface = Color(9, 20, 24, 255),
        tertiary = Color(0, 102, 231, 255),
        error = Color(187, 0, 19, 255),
        surfaceVariant = Color(191, 197, 209, 255),
    )

var TransparentTheme =
    darkColorScheme(
        background = Color(20, 20, 20, 30),
        onBackground = Color(20, 20, 20, 30),
        surface = Color(50, 63, 76, 255),
        onSurface = Color(240, 244, 245, 255),
        tertiary = Color(3, 100, 237, 255),
        error = Color(187, 0, 19, 255),
        surfaceVariant = Color(20, 20, 20, 30)
    )

var NewDarkTheme =
    darkColorScheme(
        background = Color(0xFF414141),
        onBackground = Color(0xFF272727),
        surface = Color(0x0cffffff),
        surfaceVariant = Color(0x4c000000),
        secondaryContainer = Color(240, 244, 245, 255),
        onSurface = Color(240, 244, 245, 255),
        tertiary = Color(0xff0173ec),
        error = Color(0xFFdd1535),
    )