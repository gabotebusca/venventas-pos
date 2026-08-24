package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.model.BcvRate
import com.example.model.BiblicalVerse
import com.example.ui.splash.BiblicalSplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun splash_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        BiblicalSplashScreen(
          verse = BiblicalVerse(
            quote = "Pon en manos del Señor todas tus obras, y tus proyectos se cumplirán.",
            reference = "Proverbios 16:3",
            topic = "Prosperidad"
          ),
          bcvRate = BcvRate.DEFAULT,
          countdownSeconds = 5,
          onSkip = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
