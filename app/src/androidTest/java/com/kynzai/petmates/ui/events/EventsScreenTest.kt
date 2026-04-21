package com.kynzai.petmates.ui.events

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class EventsScreenTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun eventsScreen_renders_search_and_list_and_can_filter() {
        rule.setContent { EventsScreen() }

        rule.onNodeWithTag("events_screen").assertExists()
        rule.onNodeWithTag("events_search").assertExists()
        rule.onNodeWithTag("events_list").assertExists()

        // One of stub projects.
        rule.onNodeWithText("Приложение Contacts").assertExists()

        // Filter by name.
        rule.onNodeWithTag("events_search").performTextInput("PetMates")
        rule.onNodeWithText("PetMates Mobile").assertExists()
    }
}
