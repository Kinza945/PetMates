package com.kynzai.petmates.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kynzai.domain.models.Contact
import com.kynzai.domain.models.Gender
import com.kynzai.domain.models.UserProfileUpdate
import com.kynzai.domain.repositories.UserRepository
import com.kynzai.petmates.session.SessionManager
import com.kynzai.petmates.ui.common.ScreenState
import com.kynzai.petmates.ui.common.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditableContactUi(
    val name: String = "",
    val link: String = "",
    val nameError: String? = null,
    val linkError: String? = null,
)

data class ProfileEditForm(
    val realName: String = "",
    val age: String = "",
    val gender: Gender = Gender.UNSPECIFIED,
    val country: String = "",
    val city: String = "",
    val workplace: String = "",
    val profileRole: String = "",
    val description: String = "",
    val hardSkills: List<String> = emptyList(),
    val softSkills: List<String> = emptyList(),
    val contacts: List<EditableContactUi> = emptyList(),
    val newHardSkill: String = "",
    val newSoftSkill: String = "",
    val realNameError: String? = null,
    val ageError: String? = null,
)

@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    private val users: UserRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val _state = MutableStateFlow<ScreenState<ProfileEditForm>>(ScreenState.Loading)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    fun load() {
        val userId = sessionManager.state.value.currentUserId
        if (userId == null) {
            _state.value = ScreenState.Unauthorized
            return
        }

        viewModelScope.launch {
            _state.value = ScreenState.Loading
            val user = users.getUserById(userId).getOrElse {
                _state.value = ScreenState.Error(it.message ?: "Не удалось загрузить профиль")
                return@launch
            }
            _state.value = ScreenState.Content(
                ProfileEditForm(
                    realName = user.realName.orEmpty(),
                    age = user.age?.toString().orEmpty(),
                    gender = user.gender,
                    country = user.country.orEmpty(),
                    city = user.city.orEmpty(),
                    workplace = user.workplace.orEmpty(),
                    profileRole = user.profileRole.orEmpty(),
                    description = user.description.orEmpty(),
                    hardSkills = user.hardSkills,
                    softSkills = user.softSkills,
                    contacts = user.contacts.map { EditableContactUi(it.name, it.link) },
                )
            )
        }
    }

    fun onRealNameChanged(v: String) = updateForm { copy(realName = v, realNameError = null) }
    fun onAgeChanged(v: String) = updateForm { copy(age = v.filter { it.isDigit() }, ageError = null) }
    fun onGenderChanged(v: Gender) = updateForm { copy(gender = v) }
    fun onCountryChanged(v: String) = updateForm { copy(country = v) }
    fun onCityChanged(v: String) = updateForm { copy(city = v) }
    fun onWorkplaceChanged(v: String) = updateForm { copy(workplace = v) }
    fun onProfileRoleChanged(v: String) = updateForm { copy(profileRole = v) }
    fun onDescriptionChanged(v: String) = updateForm { copy(description = v) }
    fun onNewHardSkillChanged(v: String) = updateForm { copy(newHardSkill = v) }
    fun onNewSoftSkillChanged(v: String) = updateForm { copy(newSoftSkill = v) }

    fun addHardSkill() = addSkill(isHard = true)
    fun addSoftSkill() = addSkill(isHard = false)
    fun removeHardSkill(skill: String) = updateForm { copy(hardSkills = hardSkills - skill) }
    fun removeSoftSkill(skill: String) = updateForm { copy(softSkills = softSkills - skill) }

    fun addContact() = updateForm { copy(contacts = contacts + EditableContactUi()) }
    fun removeContact(index: Int) = updateForm { copy(contacts = contacts.filterIndexed { i, _ -> i != index }) }
    fun onContactNameChanged(index: Int, value: String) = updateContact(index) { copy(name = value, nameError = null) }
    fun onContactLinkChanged(index: Int, value: String) = updateContact(index) { copy(link = value, linkError = null) }

    fun save() {
        val form = (_state.value as? ScreenState.Content)?.value ?: return
        val validated = validate(form)
        _state.value = ScreenState.Content(validated)
        if (validated.realNameError != null || validated.ageError != null || validated.contacts.any { it.nameError != null || it.linkError != null }) {
            return
        }

        viewModelScope.launch {
            users.updateProfile(
                UserProfileUpdate(
                    realName = validated.realName.trim(),
                    age = validated.age.toIntOrNull(),
                    gender = validated.gender,
                    country = validated.country.trim().ifBlank { null },
                    city = validated.city.trim().ifBlank { null },
                    workplace = validated.workplace.trim().ifBlank { null },
                    profileRole = validated.profileRole.trim().ifBlank { null },
                    description = validated.description.trim().ifBlank { null },
                    hardSkills = validated.hardSkills,
                    softSkills = validated.softSkills,
                    contacts = validated.contacts.map { Contact(it.name.trim(), it.link.trim()) },
                )
            ).onSuccess {
                _events.emit(UiEvent.Saved)
            }.onFailure {
                _events.emit(UiEvent.ShowMessage(it.message ?: "Не удалось сохранить профиль"))
            }
        }
    }

    private fun addSkill(isHard: Boolean) {
        updateForm {
            val raw = if (isHard) newHardSkill else newSoftSkill
            val skill = raw.trim().takeIf { it.isNotBlank() }?.let { if (it.startsWith("#")) it else "#$it" }
                ?: return@updateForm this
            if (isHard) {
                copy(hardSkills = (hardSkills + skill).distinct(), newHardSkill = "")
            } else {
                copy(softSkills = (softSkills + skill).distinct(), newSoftSkill = "")
            }
        }
    }

    private fun validate(form: ProfileEditForm): ProfileEditForm {
        val ageValue = form.age.toIntOrNull()
        return form.copy(
            realNameError = if (form.realName.isBlank()) "Введите имя" else null,
            ageError = if (form.age.isNotBlank() && (ageValue == null || ageValue !in 1..120)) "Введите корректный возраст" else null,
            contacts = form.contacts.map { contact ->
                contact.copy(
                    nameError = if (contact.name.isBlank()) "Введите название" else null,
                    linkError = if (!isContactLinkValid(contact.link)) "Введите корректную ссылку" else null,
                )
            },
        )
    }

    private fun isContactLinkValid(link: String): Boolean {
        val v = link.trim()
        return v.isNotBlank() && (v.startsWith("http") || v.startsWith("@") || "." in v)
    }

    private fun updateContact(index: Int, block: EditableContactUi.() -> EditableContactUi) {
        updateForm {
            copy(contacts = contacts.mapIndexed { i, contact -> if (i == index) contact.block() else contact })
        }
    }

    private fun updateForm(block: ProfileEditForm.() -> ProfileEditForm) {
        _state.update { current ->
            val form = (current as? ScreenState.Content)?.value ?: return@update current
            ScreenState.Content(form.block())
        }
    }
}
