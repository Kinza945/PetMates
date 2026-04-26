package com.kynzai.petmates.ui.vacancy

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kynzai.domain.common.LoadState
import com.kynzai.domain.common.toLoadState
import com.kynzai.domain.models.Vacancy
import com.kynzai.domain.usecases.CreateVacancyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class CreateVacancyViewModel @Inject constructor(
    private val createVacancy: CreateVacancyUseCase,
) : ViewModel() {
    var title by mutableStateOf("")
        private set
    var role by mutableStateOf("")
        private set
    var description by mutableStateOf("")
        private set
    var requiredTagsRaw by mutableStateOf("")
        private set
    var isOpen by mutableStateOf(true)
        private set

    var createState: LoadState<Vacancy> by mutableStateOf(LoadState.Idle)
        private set

    val isValid: Boolean
        get() = title.isNotBlank() && role.isNotBlank() && description.isNotBlank()

    fun updateTitle(v: String) {
        title = v
    }

    fun updateRole(v: String) {
        role = v
    }

    fun updateDescription(v: String) {
        description = v
    }

    fun updateRequiredTagsRaw(v: String) {
        requiredTagsRaw = v
    }

    fun updateIsOpen(v: Boolean) {
        isOpen = v
    }

    fun submit(projectId: UUID) {
        if (!isValid) return
        if (createState is LoadState.Loading) return

        val tags = requiredTagsRaw
            .split(",", " ")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { if (it.startsWith("#")) it else "#$it" }
            .distinct()

        createState = LoadState.Loading
        viewModelScope.launch {
            val res = createVacancy(
                projectId = projectId,
                title = title,
                role = role,
                description = description,
                requiredTags = tags,
                isOpen = isOpen,
            )
            createState = res.toLoadState()
        }
    }

    fun consumeCreated() {
        if (createState is LoadState.Data) {
            createState = LoadState.Idle
        }
    }
}
