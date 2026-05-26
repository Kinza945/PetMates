package com.kynzai.petmates.ui.auth

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import com.kynzai.domain.models.LoginRequest
import com.kynzai.domain.models.RegisterRequest
import com.kynzai.domain.models.SocialAuthProvider
import com.kynzai.petmates.R

private val Primary = Color(0xFF40B4A4)
private val SurfaceColor = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF212121)
private val TextSecondary = Color(0xFF868E96)

private enum class Mode { Login, Register }

@Composable
fun AuthScreen(
    onLogin: (LoginRequest) -> Unit,
    onRegister: (RegisterRequest) -> Unit,
    onGuestContinue: () -> Unit,
    onOAuthClick: (SocialAuthProvider) -> Unit = {},
) {
    val context = LocalContext.current

    var mode by rememberSaveable { mutableStateOf(Mode.Login) }

    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var rememberMe by rememberSaveable { mutableStateOf(true) }

    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    val isFormValid = when (mode) {
        Mode.Login -> username.isNotBlank() && password.isNotBlank()
        Mode.Register -> {
            username.isNotBlank() &&
                email.isNotBlank() &&
                password.isNotBlank() &&
                confirmPassword.isNotBlank() &&
                password == confirmPassword
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SurfaceColor
    ) {
        ColumnLikeTop(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            Text(
                text = "PetMates",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                color = Primary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            TabRow(
                selectedTabIndex = if (mode == Mode.Login) 0 else 1,
                containerColor = SurfaceColor,
                contentColor = Primary
            ) {
                Tab(
                    selected = mode == Mode.Login,
                    onClick = { mode = Mode.Login },
                    text = {
                        Text(
                            text = "Вход в аккаунт",
                            color = if (mode == Mode.Login) Primary else TextSecondary,
                            fontWeight = if (mode == Mode.Login) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = mode == Mode.Register,
                    onClick = { mode = Mode.Register },
                    text = {
                        Text(
                            text = "Регистрация аккаунта",
                            color = if (mode == Mode.Register) Primary else TextSecondary,
                            fontWeight = if (mode == Mode.Register) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (mode == Mode.Login) {
                AuthTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = "Имя пользователя*",
                    modifier = Modifier.testTag("auth_username"),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Забыли пароль?",
                        fontSize = 14.sp,
                        color = Primary,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable {
                            Toast.makeText(context, "Восстановление пароля скоро появится", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                AuthPasswordField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Пароль*",
                    visible = passwordVisible,
                    onToggleVisible = { passwordVisible = !passwordVisible },
                    modifier = Modifier.testTag("auth_password"),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it }
                    )
                    Text(
                        text = "Запомнить меня на этом устройстве",
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                }
            } else {
                AuthTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = "Имя пользователя*",
                    modifier = Modifier.testTag("auth_username"),
                )
                Spacer(modifier = Modifier.height(12.dp))

                AuthTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email*",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.testTag("auth_email"),
                )

                Spacer(modifier = Modifier.height(12.dp))

                AuthPasswordField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Пароль*",
                    visible = passwordVisible,
                    onToggleVisible = { passwordVisible = !passwordVisible },
                    modifier = Modifier.testTag("auth_password"),
                )

                Text(
                    text = "Минимум 8 символов, включая цифры и спец. символы",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
                )

                AuthPasswordField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Подтверждение пароля*",
                    visible = confirmPasswordVisible,
                    onToggleVisible = { confirmPasswordVisible = !confirmPasswordVisible },
                    modifier = Modifier.testTag("auth_confirm_password"),
                )
            }

            Button(
                onClick = {
                    if (mode == Mode.Login) {
                        onLogin(
                            LoginRequest(
                                nicknameOrEmail = username,
                                password = password,
                                rememberMe = rememberMe,
                            )
                        )
                    } else {
                        onRegister(
                            RegisterRequest(
                                nickname = username,
                                email = email,
                                password = password,
                            )
                        )
                    }
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Login,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = if (mode == Mode.Login) "Войти" else "Зарегистрироваться",
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = onGuestContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
            ) {
                Text("Продолжить без регистрации", fontWeight = FontWeight.Bold)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "или",
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SocialCircleButton(
                    iconRes = R.drawable.ic_google,
                    modifier = Modifier.testTag("auth_oauth_google"),
                    onClick = { onOAuthClick(SocialAuthProvider.GOOGLE) },
                )
                SocialCircleButton(
                    iconRes = R.drawable.ic_github,
                    modifier = Modifier.testTag("auth_oauth_github"),
                    onClick = { onOAuthClick(SocialAuthProvider.GITHUB) },
                )
                SocialCircleButton(
                    iconRes = R.drawable.ic_twitch,
                    modifier = Modifier.testTag("auth_oauth_twitch"),
                    onClick = { onOAuthClick(SocialAuthProvider.TWITCH) },
                )


            }
        }
    }
}

@Composable
private fun ColumnLikeTop(
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier
    ) { content() }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        keyboardOptions = keyboardOptions,
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary,
            focusedLabelColor = Primary,
            cursorColor = Primary,
            unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f),
            unfocusedLabelColor = TextSecondary,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
        )
    )
}

@Composable
private fun AuthPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visible: Boolean,
    onToggleVisible: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        trailingIcon = {
            IconButton(onClick = onToggleVisible) {
                Icon(
                    imageVector = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary,
            focusedLabelColor = Primary,
            cursorColor = Primary,
            unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f),
            unfocusedLabelColor = TextSecondary,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
        )
    )
}

@Composable
private fun SocialCircleButton(
    iconRes: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        shape = RoundedCornerShape(percent = 50),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
