package com.kynzai.petmates.ui.requests

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kynzai.domain.common.LoadState
import com.kynzai.petmates.ui.common.EmptyStateScreen
import com.kynzai.petmates.ui.common.ErrorStateScreen
import com.kynzai.petmates.ui.common.LoadingStateScreen
import com.kynzai.petmates.ui.common.ServerUnavailableScreen
import com.kynzai.petmates.ui.common.isServerUnavailable
import com.kynzai.petmates.ui.common.toUiMessage
import com.kynzai.petmates.ui.theme.PetMatesBackground
import com.kynzai.petmates.ui.theme.PetMatesPrimary
import com.kynzai.petmates.ui.theme.PetMatesSurface
import com.kynzai.petmates.ui.theme.PetMatesTextPrimary
import com.kynzai.petmates.ui.theme.PetMatesTextSecondary
import java.util.UUID

private val Success = Color(0xFF3AC83D)
private val Danger = Color(0xFFE53935)
private val Warning = Color(0xFFFFB300)

private enum class RequestsTab { Incoming, Outgoing }

enum class OutgoingStatus { Pending, Accepted, Rejected }

@Composable
fun RequestsRoute(
    vm: RequestsViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    RequestsScreen(
        state = state,
        onAccept = vm::accept,
        onReject = vm::reject,
        onRetryClick = vm::refresh,
    )
}

@Composable
fun RequestsScreen(
    state: LoadState<RequestsUiModel> = LoadState.Data(demoRequests()),
    onAccept: (UUID) -> Unit = {},
    onReject: (UUID) -> Unit = {},
    onRetryClick: () -> Unit = {},
) {
    var tab by remember { mutableIntStateOf(RequestsTab.Incoming.ordinal) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PetMatesBackground)
    ) {
        Text(
            text = "Заявки",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = PetMatesTextPrimary,
            modifier = Modifier.padding(16.dp)
        )

        TabRow(
            selectedTabIndex = tab,
            containerColor = PetMatesSurface,
            contentColor = PetMatesPrimary
        ) {
            Tab(
                selected = tab == RequestsTab.Incoming.ordinal,
                onClick = { tab = RequestsTab.Incoming.ordinal },
                text = { Text("Входящие") },
                selectedContentColor = PetMatesPrimary,
                unselectedContentColor = PetMatesTextSecondary,
            )
            Tab(
                selected = tab == RequestsTab.Outgoing.ordinal,
                onClick = { tab = RequestsTab.Outgoing.ordinal },
                text = { Text("Исходящие") },
                selectedContentColor = PetMatesPrimary,
                unselectedContentColor = PetMatesTextSecondary,
            )
        }

        when (state) {
            is LoadState.Loading -> LoadingStateScreen(message = "Загружаем заявки...")

            is LoadState.Error -> {
                val message = state.error.toUiMessage()
                if (state.error.isServerUnavailable()) {
                    ServerUnavailableScreen(onRetryClick = onRetryClick)
                } else {
                    ErrorStateScreen(message = message, onRetryClick = onRetryClick)
                }
            }

            is LoadState.Data -> {
                val data = state.value
                val visibleItemsCount = if (tab == RequestsTab.Incoming.ordinal) data.incoming.size else data.outgoing.size
                if (visibleItemsCount == 0) {
                    EmptyStateScreen(
                        title = "Нет заявок",
                        message = if (tab == RequestsTab.Incoming.ordinal) {
                            "Когда пользователи откликнутся на ваши вакансии, заявки появятся здесь."
                        } else {
                            "Ваши отклики на вакансии будут отображаться здесь."
                        },
                    )
                    return@Column
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (tab == RequestsTab.Incoming.ordinal) {
                        items(data.incoming, key = { it.responseId }) { item ->
                            IncomingRequestCard(
                                item = item,
                                onAccept = { onAccept(item.responseId) },
                                onReject = { onReject(item.responseId) },
                            )
                        }
                    } else {
                        items(data.outgoing, key = { it.responseId }) { item ->
                            OutgoingRequestCard(item = item)
                        }
                    }
                }
            }

            LoadState.Idle -> Unit
        }
    }
}

private fun demoRequests(): RequestsUiModel =
    RequestsUiModel(
        incoming = listOf(
            IncomingRequestUi(
                responseId = UUID.fromString("11111111-1111-1111-1111-111111111111"),
                fromName = "Dogl1X",
                date = "14.04.2026",
                text = "Отклик на роль «Проектировщик информационных систем» в проект «Чат бот семейного ресторана»",
            )
        ),
        outgoing = listOf(
            OutgoingRequestUi(
                responseId = UUID.fromString("22222222-2222-2222-2222-222222222222"),
                projectName = "Приложение Contacts",
                date = "13.04.2026",
                text = "Ваш отклик на роль «Backend девелопер»",
                status = OutgoingStatus.Pending,
            )
        ),
    )

@Composable
private fun IncomingRequestCard(
    item: IncomingRequestUi,
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PetMatesSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.LightGray, CircleShape)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = item.fromName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PetMatesTextPrimary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(text = item.date, fontSize = 12.sp, color = PetMatesTextSecondary)
            }

            Text(
                text = item.text,
                fontSize = 14.sp,
                color = PetMatesTextPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAccept,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PetMatesPrimary, contentColor = Color.White)
                ) {
                    Text("Принять", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Danger)
                ) {
                    Text("Отклонить", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun OutgoingRequestCard(item: OutgoingRequestUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PetMatesSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.projectName,
                    color = PetMatesPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(text = item.date, fontSize = 12.sp, color = PetMatesTextSecondary)
            }

            Text(
                text = item.text,
                fontSize = 14.sp,
                color = PetMatesTextPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )

            val (statusColor, statusText) = when (item.status) {
                OutgoingStatus.Pending -> Warning to "В ожидании"
                OutgoingStatus.Accepted -> Success to "Принят"
                OutgoingStatus.Rejected -> Danger to "Отклонен"
            }

            Row(
                modifier = Modifier.padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(statusColor, CircleShape)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = statusText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F9FA)
@Composable
private fun OutgoingStatusPreviewPending() {
    Column(modifier = Modifier.background(PetMatesBackground).padding(16.dp)) {
        OutgoingRequestCard(
            OutgoingRequestUi(
                responseId = UUID.randomUUID(),
                projectName = "Приложение Contacts",
                date = "14.04.2026",
                text = "Ваш отклик на роль «Backend девелопер»",
                status = OutgoingStatus.Pending
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F9FA)
@Composable
private fun OutgoingStatusPreviewAccepted() {
    Column(modifier = Modifier.background(PetMatesBackground).padding(16.dp)) {
        OutgoingRequestCard(
            OutgoingRequestUi(
                responseId = UUID.randomUUID(),
                projectName = "Task Tracker",
                date = "12.04.2026",
                text = "Ваш отклик на роль «Android разработчик»",
                status = OutgoingStatus.Accepted
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F9FA)
@Composable
private fun OutgoingStatusPreviewRejected() {
    Column(modifier = Modifier.background(PetMatesBackground).padding(16.dp)) {
        OutgoingRequestCard(
            OutgoingRequestUi(
                responseId = UUID.randomUUID(),
                projectName = "Mobile Design System",
                date = "09.04.2026",
                text = "Ваш отклик на роль «Frontend разработчик»",
                status = OutgoingStatus.Rejected
            )
        )
    }
}
