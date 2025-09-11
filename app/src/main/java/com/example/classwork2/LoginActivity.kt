package com.example.classwork2

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classwork2.ui.theme.Classwork2Theme

sealed class AvatarType {
    data class IconAvatar(
        val icon: ImageVector,
    ) : AvatarType()

    data class ImageAvatar(
        @DrawableRes val drawableRes: Int,
    ) : AvatarType()
}

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Classwork2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreen(
                        onLogin = {
                            // 跳转到MainActivity
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish() // 关闭当前Activity，防止返回到登录页
                        },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    onLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedAvatar by remember {
        mutableStateOf<AvatarType>(AvatarType.IconAvatar(Icons.Default.Person))
    }
    var showAvatarSelector by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        // 横屏布局：左右分栏
        LandscapeLoginLayout(
            username = username,
            password = password,
            selectedAvatar = selectedAvatar,
            showAvatarSelector = showAvatarSelector,
            onUsernameChange = { username = it },
            onPasswordChange = { password = it },
            onAvatarSelect = { selectedAvatar = it },
            onAvatarSelectorToggle = { showAvatarSelector = it },
            onLogin = onLogin,
            modifier = modifier,
        )
    } else {
        // 竖屏布局：垂直排列
        PortraitLoginLayout(
            username = username,
            password = password,
            selectedAvatar = selectedAvatar,
            showAvatarSelector = showAvatarSelector,
            onUsernameChange = { username = it },
            onPasswordChange = { password = it },
            onAvatarSelect = { selectedAvatar = it },
            onAvatarSelectorToggle = { showAvatarSelector = it },
            onLogin = onLogin,
            modifier = modifier,
        )
    }
}

@Composable
fun PortraitLoginLayout(
    username: String,
    password: String,
    selectedAvatar: AvatarType,
    showAvatarSelector: Boolean,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onAvatarSelect: (AvatarType) -> Unit,
    onAvatarSelectorToggle: (Boolean) -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "魔法图书馆", fontSize = 24.sp, modifier = Modifier.padding(bottom = 32.dp))

        // 可点击的圆形头像框
        AvatarSelector(
            selectedAvatar = selectedAvatar,
            showSelector = showAvatarSelector,
            onAvatarClick = { onAvatarSelectorToggle(!showAvatarSelector) },
            onAvatarSelect = { avatar ->
                onAvatarSelect(avatar)
                onAvatarSelectorToggle(false)
            },
            modifier = Modifier.size(100.dp),
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text("用户名") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        )

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("密码") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
        )

        Button(onClick = onLogin, modifier = Modifier.fillMaxWidth()) { Text("登入") }
    }
}

@Composable
fun LandscapeLoginLayout(
    username: String,
    password: String,
    selectedAvatar: AvatarType,
    showAvatarSelector: Boolean,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onAvatarSelect: (AvatarType) -> Unit,
    onAvatarSelectorToggle: (Boolean) -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 左侧：头像和标题
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = "魔法图书馆", fontSize = 28.sp, modifier = Modifier.padding(bottom = 24.dp))

            AvatarSelector(
                selectedAvatar = selectedAvatar,
                showSelector = showAvatarSelector,
                onAvatarClick = { onAvatarSelectorToggle(!showAvatarSelector) },
                onAvatarSelect = { avatar ->
                    onAvatarSelect(avatar)
                    onAvatarSelectorToggle(false)
                },
                modifier = Modifier.size(120.dp),
            )
        }

        Spacer(modifier = Modifier.width(32.dp))

        // 右侧：输入框和按钮
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
                label = { Text("用户名") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            )

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("密码") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            )

            Button(onClick = onLogin, modifier = Modifier.fillMaxWidth()) { Text("登入") }
        }
    }
}

@Composable
fun AvatarSelector(
    selectedAvatar: AvatarType,
    showSelector: Boolean,
    onAvatarClick: () -> Unit,
    onAvatarSelect: (AvatarType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val avatarOptions =
        listOf(
            AvatarType.IconAvatar(Icons.Default.Person),
            AvatarType.ImageAvatar(R.drawable.av1),
            AvatarType.ImageAvatar(R.drawable.av2),
            AvatarType.ImageAvatar(R.drawable.av3),
        )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // 主头像显示
        Surface(
            modifier = modifier.clip(CircleShape).clickable { onAvatarClick() },
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            when (selectedAvatar) {
                is AvatarType.IconAvatar -> {
                    Icon(
                        selectedAvatar.icon,
                        contentDescription = "用户头像",
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                is AvatarType.ImageAvatar -> {
                    Image(
                        painter = painterResource(selectedAvatar.drawableRes),
                        contentDescription = "用户头像",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }

        // 头像选择器
        if (showSelector) {
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                LazyRow(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(avatarOptions) { avatar ->
                        Surface(
                            modifier =
                                Modifier.size(48.dp).clip(CircleShape).clickable {
                                    onAvatarSelect(avatar)
                                },
                            color =
                                if (avatar == selectedAvatar) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                        ) {
                            when (avatar) {
                                is AvatarType.IconAvatar -> {
                                    Icon(
                                        avatar.icon,
                                        contentDescription = "头像选项",
                                        modifier = Modifier.fillMaxSize().padding(8.dp),
                                        tint =
                                            if (avatar == selectedAvatar) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                    )
                                }
                                is AvatarType.ImageAvatar -> {
                                    Image(
                                        painter = painterResource(avatar.drawableRes),
                                        contentDescription = "头像选项",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
