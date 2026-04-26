package com.kynzai.petmates.ui.vacancy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kynzai.domain.common.LoadState
import com.kynzai.petmates.ui.common.AuthRequiredScreen
import com.kynzai.petmates.ui.theme.PetMatesBackground
import com.kynzai.petmates.ui.theme.PetMatesPrimary
import com.kynzai.petmates.ui.theme.PetMatesSurface
import com.kynzai.petmates.ui.theme.PetMatesTextPrimary
import com.kynzai.petmates.ui.theme.PetMatesTextSecondary
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateVacancyScreen(
    projectId: String?,
    onBackClick: () -> Unit,
    onCreated: () -> Unit,
    isAuthorized: Boolean,
    onAuthRequested: () -> Unit,
    vm: CreateVacancyViewModel = hiltViewModel(),
) {
    val parsed = runCatching { projectId?.let(UUID::fromString) }.getOrNull()
    val createState = vm.createState

    LaunchedEffect(createState) {
        if (createState is LoadState.Data) {
            onCreated()
            vm.consumeCreated()
        }
    }

    Scaffold(
        containerColor = PetMatesBackground,
        topBar = {
            TopAppBar(
                title = { Text("Новая вакансия", fontWeight = FontWeight.Bold, color = PetMatesTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = PetMatesTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PetMatesSurface)
            )
        },
    ) { padding ->
        if (!isAuthorized) {
            AuthRequiredScreen(
                message = "Войдите, чтобы создавать вакансии.",
                onAuthClick = onAuthRequested,
                modifier = Modifier.padding(padding),
            )
            return@Scaffold
        }

        if (parsed == null) {
            Text(
                text = "Некорректный projectId",
                color = PetMatesTextSecondary,
                modifier = Modifier.padding(padding).padding(16.dp)
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(PetMatesBackground)
                .padding(padding)
                .testTag("create_vacancy_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Заявка к проекту",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PetMatesTextPrimary
                )
                Text(
                    text = parsed.toString(),
                    fontSize = 12.sp,
                    color = PetMatesTextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            item {
                Field(vm.title, vm::updateTitle, "Название вакансии*", "create_vacancy_title")
            }
            item {
                Field(vm.role, vm::updateRole, "Роль*", "create_vacancy_role")
            }
            item {
                Field(
                    value = vm.description,
                    onValueChange = vm::updateDescription,
                    label = "Описание*",
                    tag = "create_vacancy_description",
                    singleLine = false,
                    minLines = 4,
                )
            }
            item {
                Field(
                    value = vm.requiredTagsRaw,
                    onValueChange = vm::updateRequiredTagsRaw,
                    label = "Теги (через пробел/запятую)",
                    tag = "create_vacancy_tags",
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Вакансия открыта", color = PetMatesTextPrimary, fontWeight = FontWeight.Medium)
                        Text("Можно откликаться", color = PetMatesTextSecondary, fontSize = 12.sp)
                    }
                    Switch(checked = vm.isOpen, onCheckedChange = vm::updateIsOpen)
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                Button(
                    onClick = { vm.submit(parsed) },
                    enabled = vm.isValid && createState !is LoadState.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("create_vacancy_submit"),
                    colors = ButtonDefaults.buttonColors(containerColor = PetMatesPrimary, contentColor = Color.White),
                ) {
                    Text("Создать вакансию", fontWeight = FontWeight.Bold)
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
