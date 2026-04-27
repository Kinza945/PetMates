package com.kynzai.petmates.ui.profile

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kynzai.domain.models.Gender
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
fun ProfileEditScreen(
    onBackClick: () -> Unit = {},
    onAuthRequested: () -> Unit = {},
    vm: ProfileEditViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) { vm.load() }
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
                title = { Text("Редактировать профиль", fontWeight = FontWeight.Bold, color = PetMatesTextPrimary) },
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
            ScreenState.Loading -> androidx.compose.foundation.layout.Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PetMatesPrimary)
            }
            ScreenState.Unauthorized -> AuthRequiredScreen(onAuthClick = onAuthRequested, modifier = Modifier.padding(padding))
            is ScreenState.Error -> Text(s.message, color = PetMatesTextSecondary, modifier = Modifier.padding(padding).padding(16.dp))
            is ScreenState.Content -> ProfileEditContent(
                form = s.value,
                modifier = Modifier.padding(padding),
                vm = vm,
            )
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun ProfileEditContent(
    form: ProfileEditForm,
    modifier: Modifier,
    vm: ProfileEditViewModel,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text("Основное", fontWeight = FontWeight.Bold, color = PetMatesTextPrimary) }
        item { EditTextField(form.realName, vm::onRealNameChanged, "Имя*", form.realNameError) }
        item { EditTextField(form.age, vm::onAgeChanged, "Возраст", form.ageError) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Gender.entries.forEach { gender ->
                    FilterChip(
                        selected = form.gender == gender,
                        onClick = { vm.onGenderChanged(gender) },
                        label = { Text(gender.toUi()) },
                    )
                }
            }
        }
        item { EditTextField(form.country, vm::onCountryChanged, "Страна", null) }
        item { EditTextField(form.city, vm::onCityChanged, "Город", null) }
        item { EditTextField(form.workplace, vm::onWorkplaceChanged, "Работа/учёба", null) }
        item { EditTextField(form.profileRole, vm::onProfileRoleChanged, "Роль", null) }
        item { EditTextField(form.description, vm::onDescriptionChanged, "Описание", null, singleLine = false, minLines = 4) }

        item { SkillEditor("Hard-skills", form.newHardSkill, vm::onNewHardSkillChanged, vm::addHardSkill, form.hardSkills, vm::removeHardSkill) }
        item { SkillEditor("Soft-skills", form.newSoftSkill, vm::onNewSoftSkillChanged, vm::addSoftSkill, form.softSkills, vm::removeSoftSkill) }

        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Контакты", fontWeight = FontWeight.Bold, color = PetMatesTextPrimary, modifier = Modifier.weight(1f))
                IconButton(onClick = vm::addContact) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить", tint = PetMatesPrimary)
                }
            }
        }
        itemsIndexed(form.contacts) { index, contact ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                EditTextField(contact.name, { vm.onContactNameChanged(index, it) }, "Название", contact.nameError, modifier = Modifier.weight(1f))
                EditTextField(contact.link, { vm.onContactLinkChanged(index, it) }, "Ссылка", contact.linkError, modifier = Modifier.weight(1f))
                IconButton(onClick = { vm.removeContact(index) }) {
                    Icon(Icons.Default.Close, contentDescription = "Удалить", tint = Color.Red)
                }
            }
        }
        item {
            Button(
                onClick = vm::save,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PetMatesPrimary, contentColor = Color.White),
            ) {
                Text("Сохранить", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EditTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    modifier: Modifier = Modifier.fillMaxWidth(),
    singleLine: Boolean = true,
    minLines: Int = 1,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(label) },
        isError = error != null,
        supportingText = { if (error != null) Text(error) },
        singleLine = singleLine,
        minLines = minLines,
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PetMatesPrimary),
    )
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun SkillEditor(
    title: String,
    input: String,
    onInput: (String) -> Unit,
    onAdd: () -> Unit,
    skills: List<String>,
    onRemove: (String) -> Unit,
) {
    Text(title, fontWeight = FontWeight.Bold, color = PetMatesTextPrimary)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        EditTextField(input, onInput, "Новый тег", null, modifier = Modifier.weight(1f))
        IconButton(onClick = onAdd) { Icon(Icons.Default.Add, contentDescription = "Добавить", tint = PetMatesPrimary) }
    }
    androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        skills.forEach { skill ->
            AssistChip(onClick = { onRemove(skill) }, label = { Text(skill) })
        }
    }
}

private fun Gender.toUi(): String =
    when (this) {
        Gender.MALE -> "Мужской"
        Gender.FEMALE -> "Женский"
        Gender.UNSPECIFIED -> "Не указано"
    }
