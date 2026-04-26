package com.kynzai.petmates.ui.project

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kynzai.domain.common.LoadState
import com.kynzai.domain.common.toLoadState
import com.kynzai.domain.models.Project
import com.kynzai.domain.models.ProjectStatus
import com.kynzai.domain.usecases.CreateProjectUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class CreateProjectViewModel @Inject constructor(
    private val createProject: CreateProjectUseCase,
) : ViewModel() {
    var name by mutableStateOf("")
        private set
    var shortDescription by mutableStateOf("")
        private set
    var fullDescription by mutableStateOf("")
        private set
    var status by mutableStateOf(ProjectStatus.IN_PROGRESS)
        private set

    var createState: LoadState<Project> by mutableStateOf(LoadState.Idle)
        private set

    val isValid: Boolean
        get() = name.isNotBlank() && shortDescription.isNotBlank()

    fun updateName(v: String) {
        name = v
    }

    fun updateShortDescription(v: String) {
        shortDescription = v
    }

    fun updateFullDescription(v: String) {
        fullDescription = v
    }

    fun updateStatus(v: ProjectStatus) {
        status = v
    }

    fun submit() {
        if (!isValid) return
        if (createState is LoadState.Loading) return

        createState = LoadState.Loading
        viewModelScope.launch {
            val res = createProject(
                name = name,
                shortDescription = shortDescription,
                fullDescription = fullDescription.ifBlank { null },
                status = status,
            )
            createState = res.toLoadState()
        }
    }

    fun consumeCreated() {
            // Защита от повторной навигации после recomposition или пересоздания конфигурации.
        if (createState is LoadState.Data) {
            createState = LoadState.Idle
        }
    }
}
