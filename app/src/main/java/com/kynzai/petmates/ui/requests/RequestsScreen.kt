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
import com.kynzai.petmates.ui.theme.PetMatesBackground
import com.kynzai.petmates.ui.theme.PetMatesPrimary
import com.kynzai.petmates.ui.theme.PetMatesSurface
import com.kynzai.petmates.ui.theme.PetMatesTextPrimary
import com.kynzai.petmates.ui.theme.PetMatesTextSecondary

private val Success = Color(0xFF3AC83D)
private val Danger = Color(0xFFE53935)
private val Warning = Color(0xFFFFB300)

private enum class RequestsTab { Incoming, Outgoing }

enum class OutgoingStatus { Pending, Accepted, Rejected }

data class IncomingRequestUi(
    val id: String,
    val fromName: String,
    val date: String,
    val text: String,
)

data class OutgoingRequestUi(
    val id: String,
    val projectName: String,
    val date: String,
    val text: String,
    val status: OutgoingStatus,
)

@Composable
fun RequestsScreen() {
    var tab by remember { mutableIntStateOf(RequestsTab.Incoming.ordinal) }

    val incoming = listOf(
        IncomingRequestUi(
            id = "in_1",
            fromName = "Clown[???]",
            date = "20.04.2026",
            text = "Отклик на роль «Проектировщик информационных систем» в проект «Чат бот семейного ресторана»"
        ),
        IncomingRequestUi(
            id = "in_2",
            fromName = "DesignMaster",
            date = "18.04.2026",
            text = "Отклик на роль «UI/UX дизайнер» в проект «PetMates»"
        ),
        IncomingRequestUi(
            id = "in_3",
            fromName = "BackendEnjoyer",
            date = "16.04.2026",
            text = "Отклик на роль «Backend разработчик» в проект «Task Tracker API»"
        ),
        IncomingRequestUi(
            id = "in_4",
            fromName = "qa_cat",
            date = "12.04.2026",
            text = "Отклик на роль «QA инженер» в проект «PetMates Mobile»"
        ),
        IncomingRequestUi(
            id = "in_5",
            fromName = "ops_guy",
            date = "10.04.2026",
            text = "Отклик на роль «DevOps» в проект «CI для PetMates»"
        ),
    )

    val outgoing = listOf(
        OutgoingRequestUi(
            id = "out_1",
            projectName = "Приложение Contacts",
            date = "14.04.2026",
            text = "Ваш отклик на роль «Backend девелопер»",
            status = OutgoingStatus.Pending
        ),
        OutgoingRequestUi(
            id = "out_2",
            projectName = "Task Tracker",
            date = "12.04.2026",
            text = "Ваш отклик на роль «Android разработчик»",
            status = OutgoingStatus.Accepted
        ),
        OutgoingRequestUi(
            id = "out_3",
            projectName = "Mobile Design System",
            date = "09.04.2026",
            text = "Ваш отклик на роль «Frontend разработчик»",
            status = OutgoingStatus.Rejected
        ),
        OutgoingRequestUi(
            id = "out_4",
            projectName = "PetMates Mobile",
            date = "05.04.2026",
            text = "Ваш отклик на роль «QA инженер»",
            status = OutgoingStatus.Pending
        ),
        OutgoingRequestUi(
            id = "out_5",
            projectName = "Landing PetMates",
            date = "02.04.2026",
            text = "Ваш отклик на роль «Frontend-разработчик»",
            status = OutgoingStatus.Accepted
        ),
    )

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
                unselectedContentColor = PetMatesTextSecondary
            )
            Tab(
                selected = tab == RequestsTab.Outgoing.ordinal,
                onClick = { tab = RequestsTab.Outgoing.ordinal },
                text = { Text("Исходящие") },
                selectedContentColor = PetMatesPrimary,
                unselectedContentColor = PetMatesTextSecondary
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (RequestsTab.entries.getOrNull(tab) ?: RequestsTab.Incoming) {
                RequestsTab.Incoming -> {
                    items(incoming, key = { it.id }) { item ->
                        IncomingRequestCard(item)
                    }
                }
                RequestsTab.Outgoing -> {
                    items(outgoing, key = { it.id }) { item ->
                        OutgoingRequestCard(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun IncomingRequestCard(item: IncomingRequestUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PetMatesSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.LightGray, CircleShape)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = item.fromName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PetMatesTextPrimary)
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
                    onClick = { /* TODO */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PetMatesPrimary, contentColor = Color.White)
                ) {
                    Text("Принять", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { /* TODO */ },
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
                id = "p",
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
                id = "a",
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
                id = "r",
                projectName = "Mobile Design System",
                date = "09.04.2026",
                text = "Ваш отклик на роль «Frontend разработчик»",
                status = OutgoingStatus.Rejected
            )
        )
    }
}
