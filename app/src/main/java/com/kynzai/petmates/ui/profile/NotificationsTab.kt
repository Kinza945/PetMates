package com.kynzai.petmates.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kynzai.petmates.ui.theme.PetMatesBackground
import com.kynzai.petmates.ui.theme.PetMatesPrimary
import com.kynzai.petmates.ui.theme.PetMatesSurface
import com.kynzai.petmates.ui.theme.PetMatesTextPrimary
import com.kynzai.petmates.ui.theme.PetMatesTextSecondary

private val Success = Color(0xFF3AC83D)
private val Danger = Color(0xFFE53935)
private val Info = PetMatesPrimary

enum class NotificationType { Info, Success, Danger }

data class NotificationUiModel(
    val id: String,
    val type: NotificationType,
    val text: String,
    val date: String,
    val isUnread: Boolean,
)

/**
 * Демо-вариант без DI, сервера и mock-репозиториев.
 * Нужен для preview и UI-тестов, которые не поднимают Hilt.
 * В реальном приложении использовать [NotificationsRoute].
 */
@Composable
fun NotificationsTab(
    modifier: Modifier = Modifier,
    showHeader: Boolean = true,
) {
    var items by rememberSaveable { mutableStateOf(demoNotifications()) }
    NotificationsTabContent(
        modifier = modifier,
        showHeader = showHeader,
        items = items,
        onReadAllClick = { items = items.map { it.copy(isUnread = false) } },
    )
}

@Composable
fun NotificationsTabContent(
    modifier: Modifier = Modifier,
    showHeader: Boolean = true,
    items: List<NotificationUiModel>,
    onReadAllClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PetMatesBackground)
            .padding(16.dp)
            .testTag("notifications_tab")
    ) {
        if (showHeader) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Уведомления",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = PetMatesTextPrimary
                )

                ReadAllButton(onClick = onReadAllClick)
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                ReadAllButton(onClick = onReadAllClick)
            }
        }

        LazyColumn(
            modifier = Modifier
                .padding(top = if (showHeader) 16.dp else 0.dp)
                .fillMaxWidth()
                .weight(1f, fill = true),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items, key = { it.id }) { n ->
                NotificationItem(
                    type = n.type,
                    text = n.text,
                    date = n.date,
                    isUnread = n.isUnread
                )
            }
        }
    }
}

@Composable
private fun ReadAllButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.DoneAll,
            contentDescription = null,
            tint = PetMatesPrimary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = "Прочитать все",
            color = PetMatesPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun NotificationItem(
    type: NotificationType,
    text: String,
    date: String,
    isUnread: Boolean,
) {
    val unreadBg = PetMatesPrimary.copy(alpha = 0.1f)
    val cardColor = if (isUnread) unreadBg else PetMatesSurface

    val (accent, icon) = when (type) {
        NotificationType.Info -> Info to Icons.Default.Info
        NotificationType.Success -> Success to Icons.Default.Check
        NotificationType.Danger -> Danger to Icons.Default.Close
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(accent.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f)
            ) {
                Text(text = text, fontSize = 14.sp, color = PetMatesTextPrimary)
                Text(
                    text = date,
                    fontSize = 12.sp,
                    color = PetMatesTextSecondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

private fun demoNotifications(): List<NotificationUiModel> =
    listOf(
        NotificationUiModel(
            id = "1",
            type = NotificationType.Danger,
            text = "Ваш отклик к проекту Приложение Contacts был отклонен",
            date = "01.03.2026, 18:54",
            isUnread = true
        ),
        NotificationUiModel(
            id = "2",
            type = NotificationType.Success,
            text = "Вас приняли в проект PetMates. Добро пожаловать в команду!",
            date = "04.03.2026, 11:02",
            isUnread = false
        ),
        NotificationUiModel(
            id = "3",
            type = NotificationType.Info,
            text = "Новый проект \"Task Tracker\" добавлен в ленту",
            date = "10.03.2026, 09:40",
            isUnread = true
        ),
        NotificationUiModel(
            id = "4",
            type = NotificationType.Info,
            text = "Приглашение в проект Mobile Design System ожидает ответа",
            date = "12.03.2026, 21:17",
            isUnread = false
        ),
    )
