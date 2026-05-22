package com.kynzai.petmates.ui.main

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MainScreenTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun guestProfile_shows_auth_cta_and_calls_callback() {
        var authRequested = false
        var notificationsRequested = false
        rule.setContent {
            MainScreen(
                isAuthorized = false,
                onAuthRequested = { authRequested = true },
                onNotificationsClick = { notificationsRequested = true },
                onCreateProjectClick = {},
            )
        }

        // Switch to Profile tab.
        rule.onNodeWithText("Профиль").performClick()

        rule.onNodeWithText("Авторизоваться").assertExists()
        rule.onNodeWithText("Авторизоваться").performClick()

        rule.runOnIdle {
            assertTrue(authRequested)
            // Notification icon is present regardless of tab; click should call callback.
            // We don't click it here, but ensure callback can be triggered later in a separate test if needed.
            assertTrue(!notificationsRequested)
        }
    }

    @Test
    fun topBar_notifications_button_calls_callback() {
        var called = false
        rule.setContent {
            MainScreen(
                isAuthorized = true,
                onAuthRequested = {},
                onNotificationsClick = { called = true },
                onCreateProjectClick = {},
            )
        }

        // Content description is stable for IconButton.
        rule.onNodeWithText("Заявки").assertExists()
        rule.onNodeWithContentDescription("Уведомления").performClick()

        rule.runOnIdle { assertTrue(called) }
    }
}
