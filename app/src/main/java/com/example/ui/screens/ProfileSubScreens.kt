package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.NotificationEntity
import com.example.data.local.UserEntity
import com.example.ui.components.SubPageTopBar
import com.example.ui.theme.BgDark
import com.example.ui.theme.CardBorderDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.EsportsGold
import com.example.ui.theme.EsportsOrange
import com.example.ui.theme.SurfaceElevatedDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun EditProfileScreen(
    user: UserEntity?,
    onSaveProfile: (String, String, String, String, String) -> Unit,
    onBack: () -> Unit
) {
    var username by remember(user?.username) { mutableStateOf(user?.username ?: "") }
    val email = user?.email ?: "user@mail.com"
    var phone by remember(user?.phone) { mutableStateOf(user?.phone ?: "") }

    var curPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var conPass by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .testTag("edit_profile_screen")
    ) {
        SubPageTopBar(title = "My Profile", darkMode = true, onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar Ring
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(CardDark)
                    .border(3.dp, EsportsOrange, CircleShape)
                    .padding(5.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = EsportsOrange,
                    modifier = Modifier.size(54.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = user?.username ?: "User",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite
            )
            Text(
                text = email,
                fontSize = 13.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Basic Details Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, CardBorderDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = EsportsOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Basic Details",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite
                        )
                    }
                    Spacer(modifier = Modifier.height(15.dp))

                    EpInputField(
                        value = username,
                        onValueChange = { username = it },
                        placeholder = "Username",
                        leadingIcon = Icons.Default.AccountCircle,
                        testTag = "ep_username_input"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    EpInputField(
                        value = email,
                        onValueChange = {},
                        placeholder = "Email",
                        leadingIcon = Icons.Default.Email,
                        enabled = false,
                        testTag = "ep_email_input"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    EpInputField(
                        value = phone,
                        onValueChange = { phone = it },
                        placeholder = "Mobile Number",
                        leadingIcon = Icons.Default.Phone,
                        keyboardType = KeyboardType.Phone,
                        testTag = "ep_phone_input"
                    )
                }
            }

            // Password Change Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, CardBorderDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = EsportsOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Password Change",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite
                        )
                    }
                    Spacer(modifier = Modifier.height(15.dp))

                    EpInputField(
                        value = curPass,
                        onValueChange = { curPass = it },
                        placeholder = "Current Password",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        testTag = "ep_cur_pass_input"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    EpInputField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        placeholder = "New Password",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        testTag = "ep_new_pass_input"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    EpInputField(
                        value = conPass,
                        onValueChange = { conPass = it },
                        placeholder = "Confirm Password",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        testTag = "ep_con_pass_input"
                    )
                }
            }

            Button(
                onClick = {
                    onSaveProfile(username, phone, curPass, newPass, conPass)
                    curPass = ""
                    newPass = ""
                    conPass = ""
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EsportsOrange,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("ep_save_btn")
            ) {
                Text(
                    text = "Save Profile Changes",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun EpInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    enabled: Boolean = true,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    testTag: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        placeholder = { Text(placeholder, fontSize = 14.sp, color = TextMuted) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = EsportsOrange,
                modifier = Modifier.size(20.dp)
            )
        },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else keyboardType),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EsportsOrange,
            unfocusedBorderColor = CardBorderDark,
            focusedTextColor = TextWhite,
            unfocusedTextColor = TextWhite,
            disabledBorderColor = CardBorderDark,
            disabledContainerColor = SurfaceElevatedDark,
            disabledTextColor = TextMuted
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    )
}

@Composable
fun ReferScreen(
    onCopySuccess: () -> Unit,
    onBack: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val promoCode = "BDTOURNAMENT"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .testTag("refer_screen")
    ) {
        SubPageTopBar(title = "Refer & Earn", darkMode = true, onBack = onBack)

        // Dark Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(CardDark)
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(SurfaceElevatedDark)
                    .border(3.dp, EsportsOrange, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = "Team",
                    tint = EsportsOrange,
                    modifier = Modifier.size(60.dp)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "REFER MORE TO EARN MORE!",
                color = EsportsGold,
                fontWeight = FontWeight.Black,
                fontSize = 21.sp,
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "আপনি যখন কাউকে আপনার রেফার কোড দিয়ে রেফার করবেন তখন উক্ত ইউজার যদি প্রথমবার ১০০ টাকা বা তার বেশি ডিপোজিট করে তাহলে আপনি ১০ টাকা পাবেন",
                color = TextMuted,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center
            )
        }

        // Refer Body
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "YOUR PROMO CODE",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(15.dp))

            // Dashed Promo Code Box
            Box(
                modifier = Modifier
                    .background(SurfaceElevatedDark)
                    .drawBehind {
                        drawRect(
                            color = EsportsOrange,
                            style = Stroke(
                                width = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                            )
                        )
                    }
                    .clickable {
                        clipboard.setText(AnnotatedString(promoCode))
                        onCopySuccess()
                    }
                    .padding(horizontal = 32.dp, vertical = 15.dp)
                    .testTag("refer_code_box"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = promoCode,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = EsportsGold,
                    letterSpacing = 1.5.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 3-Step Visual Illustration Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 28.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ReferStepItem(Icons.Default.Share, "Share Code")
                ReferStepItem(Icons.Default.Person, "Friend Joins")
                ReferStepItem(Icons.Default.CardGiftcard, "Get 10 TK")
            }

            Button(
                onClick = {
                    clipboard.setText(AnnotatedString(promoCode))
                    onCopySuccess()
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EsportsOrange,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("refer_now_button")
            ) {
                Text(
                    text = "REFER NOW",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun ReferStepItem(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(CardDark)
                .border(1.dp, CardBorderDark, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = EsportsOrange,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted
        )
    }
}

@Composable
fun NotificationsScreen(
    notifications: List<NotificationEntity>,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .testTag("notifications_screen")
    ) {
        SubPageTopBar(title = "Notifications", onBack = onBack)

        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(36.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No Notifications",
                    color = TextMuted,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(notifications, key = { it.id }) { n ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardDark)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = n.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = EsportsOrange
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = n.body,
                            fontSize = 13.sp,
                            color = TextWhite.copy(alpha = 0.85f),
                            lineHeight = 19.sp
                        )
                    }
                    HorizontalDivider(color = CardBorderDark)
                }
            }
        }
    }
}
