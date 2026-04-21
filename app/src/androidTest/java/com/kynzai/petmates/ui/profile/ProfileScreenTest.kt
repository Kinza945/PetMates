package com.kynzai.petmates.ui.profile

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class ProfileScreenTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun profileScreen_shows_tabs_and_can_open_settings() {
        rule.setContent { ProfileScreen() }

        rule.onNodeWithText("Информация").assertExists()
        rule.onNodeWithText("Уведомления").assertExists()
        rule.onNodeWithText("Настройки").assertExists()

        rule.onNodeWithText("Настройки").performClick()
        rule.onNodeWithText("Безопасность").assertExists()
        rule.onNodeWithText("Удаление аккаунта").assertExists()

        // Switching to notifications should not crash (previously could due to nested scrolls).
        rule.onNodeWithText("Уведомления").performClick()
        rule.onNodeWithText("Прочитать все").assertExists()
    }
}
