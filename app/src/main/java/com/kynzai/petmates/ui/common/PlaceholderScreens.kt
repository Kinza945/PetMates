package com.kynzai.petmates.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun InDevelopmentScreen(
    modifier: Modifier = Modifier,
    title: String = "В разработке",
    message: String = "Раздел скоро появится в приложении.",
) {
    EmptyStateScreen(
        modifier = modifier,
        title = title,
        message = message,
        icon = Icons.Default.HourglassEmpty,
    )
}

@Composable
fun VacanciesPlaceholderScreen(
    modifier: Modifier = Modifier,
) {
    EmptyStateScreen(
        modifier = modifier,
        title = "Заявки появятся позже",
        message = "Лента pet-проектов и вакансий подключается к серверу. " +
            "Сейчас проектов на API ещё нет — раздел обновится, когда они появятся.",
        icon = Icons.Default.Event,
    )
}
