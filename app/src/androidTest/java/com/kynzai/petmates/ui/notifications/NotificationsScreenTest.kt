package com.kynzai.petmates.ui.notifications

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class NotificationsScreenTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun guest_sees_auth_button_in_notifications() {
        rule.setContent {
            NotificationsScreen(
                isAuthorized = false,
                onBackClick = {},
                onAuthRequested = {},
            )
        }

        rule.onNodeWithText("Авторизоваться").assertExists()
    }

    @Test
    fun authorized_sees_notifications_list() {
        rule.setContent {
            NotificationsScreen(
                isAuthorized = true,
                onBackClick = {},
                onAuthRequested = {},
            )
        }

        rule.onNodeWithText("Прочитать все").assertExists()
    }
}

