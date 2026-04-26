package com.kynzai.petmates.ui.project

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kynzai.domain.models.ProjectStatus
import com.kynzai.petmates.ui.common.AuthRequiredScreen
import com.kynzai.petmates.ui.common.ScreenState
import com.kynzai.petmates.ui.common.UiEvent
import com.kynzai.petmates.ui.mappers.toUiLabel
import com.kynzai.petmates.ui.theme.PetMatesBackground
import com.kynzai.petmates.ui.theme.PetMatesPrimary
import com.kynzai.petmates.ui.theme.PetMatesSurface
import com.kynzai.petmates.ui.theme.PetMatesTextPrimary
import com.kynzai.petmates.ui.theme.PetMatesTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProjectScreen(
    projectId: String?,
    onBackClick: () -> Unit,
    onAuthRequested: () -> Unit,
    vm: EditProjectViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(projectId) { vm.load(projectId) }
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
                title = { Text("Редактировать проект", fontWeight = FontWeight.Bold, color = PetMatesTextPrimary) },
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
            ScreenState.Loading -> androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(color = PetMatesPrimary) }

            ScreenState.Unauthorized -> AuthRequiredScreen(
                message = "Войдите, чтобы редактировать проект.",
                onAuthClick = onAuthRequested,
                modifier = Modifier.padding(padding),
            )

            is ScreenState.Error -> Text(s.message, color = PetMatesTextSecondary, modifier = Modifier.padding(padding))

            is ScreenState.Content -> EditProjectFormContent(
                form = s.value,
                modifier = Modifier.padding(padding),
                onNameChanged = vm::onNameChanged,
                onShortChanged = vm::onShortDescriptionChanged,
                onFullChanged = vm::onFullDescriptionChanged,
                onStatusChanged = vm::onStatusChanged,
                onSave = vm::save,
            )
        }
    }
}

@Composable
private fun EditProjectFormContent(
    form: EditProjectForm,
    modifier: Modifier,
    onNameChanged: (String) -> Unit,
    onShortChanged: (String) -> Unit,
    onFullChanged: (String) -> Unit,
    onStatusChanged: (ProjectStatus) -> Unit,
    onSave: () -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            EditField(form.name, onNameChanged, "Название проекта*", form.nameError)
        }
        item {
            EditField(form.shortDescription, onShortChanged, "Краткое описание*", form.shortDescriptionError)
        }
        item {
            ProjectStatusDropdown(form.status, onStatusChanged)
        }
        item {
            EditField(form.fullDescription, onFullChanged, "Полное описание", null, singleLine = false, minLines = 4)
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
private fun EditField(
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

@Composable
private fun ProjectStatusDropdown(status: ProjectStatus, onStatusChanged: (ProjectStatus) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = status.toUiLabel(),
        onValueChange = {},
        readOnly = true,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Статус") },
        trailingIcon = {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                ProjectStatus.entries.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item.toUiLabel()) },
                        onClick = {
                            onStatusChanged(item)
                            expanded = false
                        }
                    )
                }
            }
        },
    )
}
