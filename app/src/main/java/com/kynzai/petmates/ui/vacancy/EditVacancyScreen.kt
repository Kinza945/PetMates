package com.kynzai.petmates.ui.vacancy

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.kynzai.petmates.ui.common.AuthRequiredScreen
import com.kynzai.petmates.ui.common.ScreenState
import com.kynzai.petmates.ui.common.UiEvent
import com.kynzai.petmates.ui.theme.PetMatesBackground
import com.kynzai.petmates.ui.theme.PetMatesPrimary
import com.kynzai.petmates.ui.theme.PetMatesSurface
import com.kynzai.petmates.ui.theme.PetMatesTextPrimary
import com.kynzai.petmates.ui.theme.PetMatesTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditVacancyScreen(
    vacancyId: String?,
    onBackClick: () -> Unit,
    onAuthRequested: () -> Unit,
    vm: EditVacancyViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(vacancyId) { vm.load(vacancyId) }
    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                UiEvent.Saved -> onBackClick()
                is UiEvent.ShowMessage -> Toast.makeText(context, event.text, Toast.LENGTH_SHORT).show()
                else -> Unit
            }
        }
    }

    Scaffold(
        containerColor = PetMatesBackground,
        topBar = {
            TopAppBar(
                title = { Text("Редактировать вакансию", fontWeight = FontWeight.Bold, color = PetMatesTextPrimary) },
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
            ScreenState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PetMatesPrimary)
            }

            ScreenState.Unauthorized -> AuthRequiredScreen(
                message = "Войдите, чтобы редактировать вакансию.",
                onAuthClick = onAuthRequested,
                modifier = Modifier.padding(padding),
            )

            is ScreenState.Error -> Text(s.message, color = PetMatesTextSecondary, modifier = Modifier.padding(padding).padding(16.dp))

            is ScreenState.Content -> VacancyFormContent(
                form = s.value,
                modifier = Modifier.padding(padding),
                onTitleChanged = vm::onTitleChanged,
                onRoleChanged = vm::onRoleChanged,
                onDescriptionChanged = vm::onDescriptionChanged,
                onRequiredTagsChanged = vm::onRequiredTagsChanged,
                onIsOpenChanged = vm::onIsOpenChanged,
                onSave = vm::save,
            )
        }
    }
}

@Composable
private fun VacancyFormContent(
    form: EditVacancyForm,
    modifier: Modifier,
    onTitleChanged: (String) -> Unit,
    onRoleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onRequiredTagsChanged: (String) -> Unit,
    onIsOpenChanged: (Boolean) -> Unit,
    onSave: () -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { VacancyField(form.title, onTitleChanged, "Название*", form.titleError) }
        item { VacancyField(form.role, onRoleChanged, "Роль*", form.roleError) }
        item { VacancyField(form.description, onDescriptionChanged, "Описание*", form.descriptionError, singleLine = false, minLines = 4) }
        item { VacancyField(form.requiredTagsRaw, onRequiredTagsChanged, "Теги через пробел", null) }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Вакансия открыта", color = PetMatesTextPrimary, fontWeight = FontWeight.Medium)
                    Text("Кандидаты могут откликаться", color = PetMatesTextSecondary)
                }
                Switch(checked = form.isOpen, onCheckedChange = onIsOpenChanged)
            }
        }
        item {
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = form.canSave,
                colors = ButtonDefaults.buttonColors(containerColor = PetMatesPrimary, contentColor = Color.White),
            ) {
                Text("Сохранить", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun VacancyField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    singleLine: Boolean = true,
    minLines: Int = 1,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        isError = error != null,
        supportingText = { if (error != null) Text(error) },
        singleLine = singleLine,
        minLines = minLines,
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PetMatesPrimary),
    )
}
