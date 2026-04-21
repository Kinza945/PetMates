package com.kynzai.petmates.ui.users

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kynzai.petmates.ui.theme.PetMatesBackground
import com.kynzai.petmates.ui.theme.PetMatesPrimary
import com.kynzai.petmates.ui.theme.PetMatesSurface
import com.kynzai.petmates.ui.theme.PetMatesTextPrimary
import com.kynzai.petmates.ui.theme.PetMatesTextSecondary

private data class DummyUser(
    val nickname: String,
    val role: String,
    val skills: List<String>,
)

@Composable
fun UsersTab(
    onUserClick: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }

    val users = listOf(
        DummyUser("kynzai", "Android Developer", listOf("Kotlin", "Compose", "Ktor")),
        DummyUser("ClownZzz", "Python Data Science", listOf("Python", "Pandas", "SQL")),
        DummyUser("DesignMaster", "UI/UX Designer", listOf("Figma", "Photoshop", "Design Systems")),
        DummyUser("BackendEnjoyer", "Backend Developer", listOf("Ktor", "Postgres", "Supabase")),
        DummyUser("qa_cat", "QA Engineer", listOf("Test Plan", "Compose UI Test", "MockServer")),
        DummyUser("ops_guy", "DevOps", listOf("GitHub Actions", "Docker", "CI/CD")),
        DummyUser("swiftie", "iOS Developer", listOf("Swift", "SwiftUI", "Networking")),
        DummyUser("data_fox", "Data Analyst", listOf("SQL", "BI", "Retention")),
        DummyUser("web_tiger", "Frontend Developer", listOf("React", "TypeScript", "CSS")),
        DummyUser("ml_raccoon", "ML Engineer", listOf("Python", "PyTorch", "NLP")),
        DummyUser("ui_bird", "Product Designer", listOf("Figma", "Prototyping", "UX Research")),
        DummyUser("kotlin_fan", "Android Developer", listOf("Coroutines", "Flow", "Room")),
        DummyUser("api_wizard", "Backend Developer", listOf("REST", "Auth", "Caching")),
        DummyUser("db_otter", "DB Engineer", listOf("Postgres", "Indexes", "Migrations")),
        DummyUser("pm_ninja", "Product Manager", listOf("Roadmap", "Backlog", "Metrics")),
        DummyUser("support_bear", "Community Manager", listOf("Support", "Moderation", "Content")),
    ).filter {
        it.nickname.contains(query, ignoreCase = true) ||
            it.role.contains(query, ignoreCase = true) ||
            it.skills.any { s -> s.contains(query, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PetMatesBackground)
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Найти по нику или навыку...", color = PetMatesTextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PetMatesTextSecondary) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PetMatesPrimary,
                cursorColor = PetMatesPrimary,
                unfocusedBorderColor = PetMatesTextSecondary.copy(alpha = 0.5f),
                focusedTextColor = PetMatesTextPrimary,
                unfocusedTextColor = PetMatesTextPrimary,
            )
        )

        Spacer(modifier = Modifier.size(16.dp))

        if (users.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Ничего не найдено", color = PetMatesTextSecondary)
            }
        } else {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(users.size) { idx ->
                    UserCard(user = users[idx], onClick = { onUserClick(users[idx].nickname) })
                }
            }
        }
    }
}

@Composable
private fun UserCard(
    user: DummyUser,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PetMatesSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(50.dp).background(Color.LightGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.size(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.nickname, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PetMatesTextPrimary)
                Text(text = user.role, color = PetMatesTextSecondary, fontSize = 14.sp)
                Spacer(modifier = Modifier.size(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    user.skills.take(3).forEach { skill ->
                        Text(
                            text = "#$skill",
                            color = PetMatesPrimary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
