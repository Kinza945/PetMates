package com.kynzai.petmates.ui.profile

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class NotificationsTabTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun notificationsTab_renders_header_and_items_and_read_all() {
        rule.setContent { NotificationsTab() }

        rule.onNodeWithTag("notifications_tab").assertExists()
        rule.onNodeWithText("Уведомления").assertExists()
        rule.onNodeWithText("Прочитать все").assertExists()

        // One of stub notifications.
        rule.onNodeWithText("Ваш отклик к проекту Приложение Contacts был отклонен").assertExists()

        // Tap "Read all" - should not crash (state update).
        rule.onNodeWithText("Прочитать все").performClick()
    }
}

