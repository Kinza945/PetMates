package com.kynzai.petmates.ui.auth

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class AuthScreenTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun loginButton_disabled_until_email_and_password_filled() {
        rule.setContent {
            AuthScreen(onAuthorizedContinue = { _, _ -> }, onGuestContinue = {})
        }

        rule.onNodeWithText("Войти").assertIsNotEnabled()

        rule.onNodeWithTag("auth_username").performTextInput("DogI1X")
        rule.onNodeWithText("Войти").assertIsNotEnabled()

        rule.onNodeWithTag("auth_password").performTextInput("password123!")
        rule.onNodeWithText("Войти").assertIsEnabled()
    }

    @Test
    fun can_switch_to_register_tab() {
        rule.setContent {
            AuthScreen(onAuthorizedContinue = { _, _ -> }, onGuestContinue = {})
        }

        rule.onNodeWithText("Регистрация аккаунта").performClick()
        rule.onNodeWithText("Зарегистрироваться").assertIsNotEnabled()

        rule.onNodeWithTag("auth_username").performTextInput("DogI1X")
        rule.onNodeWithTag("auth_email").performTextInput("test@example.com")
        rule.onNodeWithTag("auth_password").performTextInput("password123!")
        rule.onNodeWithTag("auth_confirm_password").performTextInput("password123!")
        rule.onNodeWithText("Зарегистрироваться").assertIsEnabled()
    }
}
