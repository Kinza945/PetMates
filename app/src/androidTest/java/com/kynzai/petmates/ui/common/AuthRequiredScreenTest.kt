package com.kynzai.petmates.ui.common

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AuthRequiredScreenTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun auth_required_screen_shows_cta_and_handles_click() {
        var clicked = false

        rule.setContent {
            AuthRequiredScreen(onAuthClick = { clicked = true })
        }

        rule.onNodeWithText("Требуется авторизация").assertIsDisplayed()
        rule.onNodeWithText("Войти / Зарегистрироваться").performClick()

        assertTrue(clicked)
    }
}
