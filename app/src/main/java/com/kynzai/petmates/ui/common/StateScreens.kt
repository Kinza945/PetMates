package com.kynzai.petmates.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kynzai.domain.common.DomainError
import com.kynzai.petmates.ui.theme.PetMatesBackground
import com.kynzai.petmates.ui.theme.PetMatesPrimary
import com.kynzai.petmates.ui.theme.PetMatesSurface
import com.kynzai.petmates.ui.theme.PetMatesTextPrimary
import com.kynzai.petmates.ui.theme.PetMatesTextSecondary

@Composable
fun LoadingStateScreen(
    modifier: Modifier = Modifier,
    message: String = "Загружаем данные...",
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("loading_state"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = PetMatesPrimary)
        Text(
            text = message,
            color = PetMatesTextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Composable
fun EmptyStateScreen(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Inbox,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    StateCard(
        modifier = modifier.testTag("empty_state"),
        icon = icon,
        iconTint = PetMatesPrimary,
        title = title,
        message = message,
        actionText = actionText,
        onActionClick = onActionClick,
    )
}

@Composable
fun ErrorStateScreen(
    title: String = "Не удалось загрузить данные",
    message: String,
    modifier: Modifier = Modifier,
    onRetryClick: (() -> Unit)? = null,
) {
    StateCard(
        modifier = modifier.testTag("error_state"),
        icon = Icons.Default.ErrorOutline,
        iconTint = Color(0xFFE53935),
        title = title,
        message = message,
        actionText = onRetryClick?.let { "Повторить" },
        onActionClick = onRetryClick,
    )
}

@Composable
fun ServerUnavailableScreen(
    modifier: Modifier = Modifier,
    onRetryClick: (() -> Unit)? = null,
) {
    StateCard(
        modifier = modifier.testTag("server_unavailable_state"),
        icon = Icons.Default.CloudOff,
        iconTint = PetMatesPrimary,
        title = "Сервер пока не подключён",
        message = "Данные появятся после подключения API. Сейчас можно продолжать работу в демо-режиме.",
        actionText = onRetryClick?.let { "Повторить" },
        onActionClick = onRetryClick,
    )
}

@Composable
fun DemoModeBanner(
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("demo_mode_banner"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PetMatesPrimary.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = PetMatesPrimary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.size(8.dp))
            Text(
                text = "Демо-режим: данные тестовые, API сервера пока не подключён.",
                color = PetMatesTextPrimary,
                fontSize = 13.sp,
                lineHeight = 17.sp,
            )
        }
    }
}

@Composable
private fun StateCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PetMatesBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(iconTint.copy(alpha = 0.12f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(34.dp),
                )
            }
            Text(
                text = title,
                color = PetMatesTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 18.dp),
            )
            Text(
                text = message,
                color = PetMatesTextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
            if (actionText != null && onActionClick != null) {
                Button(
                    onClick = onActionClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PetMatesPrimary, contentColor = Color.White),
                ) {
                    Text(actionText, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CompactErrorState(
    message: String,
    onRetryClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            color = PetMatesTextSecondary,
            textAlign = TextAlign.Center,
        )
        if (onRetryClick != null) {
            OutlinedButton(onClick = onRetryClick, modifier = Modifier.padding(top = 12.dp)) {
                Text("Повторить", color = PetMatesPrimary)
            }
        }
    }
}

fun DomainError.toUiMessage(): String =
    when (this) {
        DomainError.Unauthorized -> "Нужно войти в аккаунт."
        DomainError.Forbidden -> "Недостаточно прав для этого действия."
        DomainError.NotFound -> "Данные не найдены."
        is DomainError.Validation -> message ?: "Проверьте введённые данные."
        is DomainError.Network -> message ?: "Не удалось связаться с сервером."
        is DomainError.Unknown -> message ?: "Произошла неизвестная ошибка."
    }

fun DomainError.isServerUnavailable(): Boolean =
    toUiMessage().isServerUnavailableMessage()

fun String.isServerUnavailableMessage(): Boolean =
    contains("SUPABASE_URL is empty", ignoreCase = true) ||
        contains("server", ignoreCase = true) ||
        contains("network", ignoreCase = true) ||
        contains("timeout", ignoreCase = true) ||
        contains("failed to connect", ignoreCase = true) ||
        contains("Supabase", ignoreCase = true)
