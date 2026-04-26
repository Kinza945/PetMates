package com.kynzai.petmates.ui.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kynzai.domain.common.LoadState
import com.kynzai.domain.models.ProjectStatus
import com.kynzai.petmates.ui.theme.PetMatesBackground
import com.kynzai.petmates.ui.theme.PetMatesPrimary
import com.kynzai.petmates.ui.theme.PetMatesSurface
import com.kynzai.petmates.ui.theme.PetMatesTextPrimary
import com.kynzai.petmates.ui.theme.PetMatesTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectScreen(
    onBackClick: () -> Unit,
    onCreated: (projectId: String) -> Unit,
    vm: CreateProjectViewModel = hiltViewModel(),
) {
    val createState = vm.createState

    LaunchedEffect(createState) {
        val data = (createState as? LoadState.Data)?.value ?: return@LaunchedEffect
        onCreated(data.projectId.toString())
        vm.consumeCreated()
    }

    Scaffold(
        containerColor = PetMatesBackground,
        topBar = {
            TopAppBar(
                title = { Text("Новый проект", fontWeight = FontWeight.Bold, color = PetMatesTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = PetMatesTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PetMatesSurface)
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(PetMatesBackground)
                .padding(padding)
                .testTag("create_project_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Ключевая информация",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PetMatesTextPrimary
                )
            }

            item {
                Field(
                    value = vm.name,
                    onValueChange = vm::updateName,
                    label = "Название проекта*",
                    tag = "create_project_name"
                )
            }

            item {
                Field(
                    value = vm.shortDescription,
                    onValueChange = vm::updateShortDescription,
                    label = "Краткое описание*",
                    tag = "create_project_short"
                )
            }

            item {
                StatusDropdown(
                    status = vm.status,
                    onStatusChange = vm::updateStatus,
                )
            }

            item {
                Field(
                    value = vm.fullDescription,
                    onValueChange = vm::updateFullDescription,
                    label = "Полное описание (опционально)",
                    tag = "create_project_full",
                    singleLine = false,
                    minLines = 4,
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                Button(
                    onClick = vm::submit,
                    enabled = vm.isValid && createState !is LoadState.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("create_project_submit"),
                    colors = ButtonDefaults.buttonColors(containerColor = PetMatesPrimary, contentColor = Color.White),
                ) {
                    Text("Создать проект", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun Field(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    tag: String,
    singleLine: Boolean = true,
    minLines: Int = 1,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag),
        label = { Text(label) },
        singleLine = singleLine,
        minLines = minLines,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PetMatesPrimary,
            cursorColor = PetMatesPrimary,
            unfocusedBorderColor = PetMatesTextSecondary.copy(alpha = 0.5f),
            focusedTextColor = PetMatesTextPrimary,
            unfocusedTextColor = PetMatesTextPrimary,
        )
    )
}

@Composable
private fun StatusDropdown(
    status: ProjectStatus,
    onStatusChange: (ProjectStatus) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        OutlinedTextField(
            value = status.toUi(),
            onValueChange = { },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("create_project_status"),
            label = { Text("Статус*") },
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = PetMatesTextSecondary)
                }
            },
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PetMatesPrimary,
                cursorColor = PetMatesPrimary,
                unfocusedBorderColor = PetMatesTextSecondary.copy(alpha = 0.5f),
                focusedTextColor = PetMatesTextPrimary,
                unfocusedTextColor = PetMatesTextPrimary,
            )
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ProjectStatus.entries.forEach { st ->
                DropdownMenuItem(
                    text = { Text(st.toUi()) },
                    onClick = {
                        onStatusChange(st)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun ProjectStatus.toUi(): String =
    when (this) {
        ProjectStatus.IN_PROGRESS -> "В процессе"
        ProjectStatus.PAUSED -> "Пауза"
        ProjectStatus.COMPLETED -> "Завершён"
    }
