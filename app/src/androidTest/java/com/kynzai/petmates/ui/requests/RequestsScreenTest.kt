package com.kynzai.petmates.ui.requests

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class RequestsScreenTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun requestsScreen_shows_tabs_and_can_switch() {
        rule.setContent { RequestsScreen() }

        rule.onNodeWithText("Заявки").assertExists()
        rule.onNodeWithText("Входящие").assertExists()
        rule.onNodeWithText("Исходящие").assertExists()

        // Incoming stub.
        rule.onNodeWithText("Принять").assertExists()
        rule.onNodeWithText("Отклонить").assertExists()

        rule.onNodeWithText("Исходящие").performClick()
        // Outgoing stub.
        rule.onNodeWithText("В ожидании").assertExists()
    }
}
