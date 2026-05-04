package com.kynzai.petmates.ui.vacancy

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kynzai.petmates.ui.common.ErrorStateScreen
import com.kynzai.petmates.ui.common.LoadingStateScreen
import com.kynzai.petmates.ui.common.ScreenState
import com.kynzai.petmates.ui.common.ServerUnavailableScreen
import com.kynzai.petmates.ui.common.UiEvent
import com.kynzai.petmates.ui.common.isServerUnavailableMessage
import com.kynzai.petmates.ui.theme.PetMatesBackground
import com.kynzai.petmates.ui.theme.PetMatesPrimary
import com.kynzai.petmates.ui.theme.PetMatesSurface
import com.kynzai.petmates.ui.theme.PetMatesTextPrimary
import com.kynzai.petmates.ui.theme.PetMatesTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VacancyDetailsScreen(
    vacancyId: String?,
    onBackClick: () -> Unit,
    onAuthRequested: () -> Unit,
    vm: VacancyDetailsViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(vacancyId) { vm.load(vacancyId) }
    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                UiEvent.AuthRequired -> onAuthRequested()
                is UiEvent.ShowMessage -> Toast.makeText(context, event.text, Toast.LENGTH_SHORT).show()
                else -> Unit
            }
        }
    }

    Scaffold(
        containerColor = PetMatesBackground,
        topBar = {
            TopAppBar(
                title = { Text("Вакансия", fontWeight = FontWeight.Bold, color = PetMatesTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = PetMatesTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PetMatesSurface)
            )
        }
    ) { padding ->
        when (val s = state) {
            ScreenState.Loading -> LoadingStateScreen(message = "Загружаем вакансию...")

            ScreenState.Unauthorized -> Unit
            is ScreenState.Error -> {
                val modifier = Modifier.padding(padding)
                if (s.message.isServerUnavailableMessage()) {
                    ServerUnavailableScreen(modifier = modifier, onRetryClick = { vm.load(vacancyId) })
                } else {
                    ErrorStateScreen(modifier = modifier, message = s.message, onRetryClick = { vm.load(vacancyId) })
                }
            }
            is ScreenState.Content -> VacancyDetailsContent(
                data = s.value,
                modifier = Modifier.padding(padding),
                onRespondOrCancel = vm::onRespondOrCancelClick,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VacancyDetailsContent(
    data: VacancyDetailsUiState,
    modifier: Modifier,
    onRespondOrCancel: () -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PetMatesSurface),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(data.project.name, color = PetMatesPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(data.vacancy.title, color = PetMatesTextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(data.vacancy.statusLabel, color = PetMatesTextSecondary, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
        item {
            Text("Описание", color = PetMatesTextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(data.vacancy.description, color = PetMatesTextPrimary, modifier = Modifier.padding(top = 8.dp))
        }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                data.vacancy.requiredTags.forEach { tag ->
                    AssistChip(onClick = {}, label = { Text(tag) })
                }
            }
        }
        item {
            Button(
                onClick = onRespondOrCancel,
                enabled = data.vacancy.isOpen,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PetMatesPrimary, contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(if (data.pendingResponse != null) "Отменить отклик" else "Откликнуться", fontWeight = FontWeight.Bold)
            }
        }
    }
}
