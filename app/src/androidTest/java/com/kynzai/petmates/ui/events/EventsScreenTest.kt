package com.kynzai.petmates.ui.events

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test
import java.util.UUID

class EventsScreenTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun eventsScreen_renders_search_and_list_and_can_filter() {
        val items = listOf(
            FeedProjectUi(
                projectId = UUID.fromString("11111111-1111-1111-1111-111111111111"),
                vacancyId = null,
                openVacancies = emptyList(),
                role = "Frontend-разработчик",
                ratingCount = 4,
                averageRating = 4.5,
                name = "Приложение Contacts",
                description = "React/верстка",
                tags = listOf("#web", "#react", "#hooks"),
                authorName = "Clown[???]",
                isOnline = true,
                hasPendingResponse = false,
            ),
            FeedProjectUi(
                projectId = UUID.fromString("22222222-2222-2222-2222-222222222222"),
                vacancyId = null,
                openVacancies = emptyList(),
                role = "Android-разработчик",
                ratingCount = 12,
                averageRating = 4.2,
                name = "PetMates Mobile",
                description = "Compose/Ktor/Supabase",
                tags = listOf("#kotlin", "#compose", "#ktor"),
                authorName = "kynzai",
                isOnline = false,
                hasPendingResponse = false,
            ),
        )

        rule.setContent {
            var query by remember { mutableStateOf("") }
            EventsScreen(
                projects = items,
                isLoading = false,
                query = query,
                onQueryChange = { query = it },
            )
        }

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
