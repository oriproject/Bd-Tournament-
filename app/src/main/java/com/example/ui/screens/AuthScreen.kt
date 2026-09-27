package com.example.ui.screens

import android.accounts.AccountManager
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.R
import com.example.ui.theme.BgDark
import com.example.ui.theme.CardBorderDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.EsportsOrange
import com.example.ui.theme.SurfaceElevatedDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    authMode: String,
    onToggleMode: (String) -> Unit,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String, String, String) -> Unit,
    onGoogleLogin: (String?, String, String, String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var loginEmail by remember { mutableStateOf("") }
    var loginPass by remember { mutableStateOf("") }
    var loginPassVisible by remember { mutableStateOf(false) }
    var loginAgreed by remember { mutableStateOf(true) }

    var regUsername by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regPass by remember { mutableStateOf("") }
    var regPromo by remember { mutableStateOf("") }
    var regPassVisible by remember { mutableStateOf(false) }
    var regAgreed by remember { mutableStateOf(true) }

    var showGoogleChooserDialog by remember { mutableStateOf(false) }

    val googleAccountPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedEmail = result.data
                ?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                ?.trim()
                .orEmpty()
            if (selectedEmail.isNotBlank()) {
                val derivedName = selectedEmail
                    .substringBefore("@")
                    .replace(Regex("[._-]+"), " ")
                    .trim()
                    .split(" ")
                    .filter { it.isNotBlank() }
                    .joinToString(" ") { word ->
                        word.replaceFirstChar { c -> c.uppercase() }
                    }
                    .ifBlank { selectedEmail.substringBefore("@") }
                onGoogleLogin(null, selectedEmail, derivedName, "")
            }
        }
    }

    fun triggerGoogleSignIn() {
        try {
            val intent = AccountManager.newChooseAccountIntent(
                null,
                null,
                arrayOf("com.google"),
                null,
                null,
                null,
                null
            )
            googleAccountPickerLauncher.launch(intent)
        } catch (_: Exception) {
            showGoogleChooserDialog = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .statusBarsPadding()
            .testTag("auth_screen")
    ) {
        // Top Header Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.28f)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_app_icon),
                contentDescription = "Bd Tournament Logo",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .border(2.dp, EsportsOrange, CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Bd Tournament",
                color = EsportsOrange,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (authMode == "login") "Sign in to your esports account" else "Join daily tournaments & win prizes",
                color = TextMuted,
                fontSize = 13.sp
            )
        }

        // Bottom Dark Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.72f),
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            color = CardDark,
            border = BorderStroke(1.dp, CardBorderDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 25.dp, vertical = 26.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (authMode == "login") {
                    Text(
                        text = "Sign In",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    AuthTextField(
                        value = loginEmail,
                        onValueChange = { loginEmail = it },
                        placeholder = "Email Address",
                        keyboardType = KeyboardType.Email,
                        testTag = "login_email_input"
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    AuthPasswordField(
                        value = loginPass,
                        onValueChange = { loginPass = it },
                        placeholder = "Password",
                        visible = loginPassVisible,
                        onToggleVisible = { loginPassVisible = !loginPassVisible },
                        testTag = "login_password_input"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = loginAgreed,
                            onCheckedChange = { loginAgreed = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = EsportsOrange,
                                checkmarkColor = Color.Black,
                                uncheckedColor = TextMuted
                            )
                        )
                        Text(
                            text = "I agree to the Terms and Conditions and Privacy Policy",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = { onLogin(loginEmail, loginPass) },
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EsportsOrange,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("login_submit_button")
                    ) {
                        Text(
                            text = "SIGN IN",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    OrDivider()

                    GoogleSignInButton(
                        text = "Continue with Google",
                        testTag = "google_login_button",
                        onClick = ::triggerGoogleSignIn
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        Text(
                            text = "Forget Password ?",
                            fontSize = 12.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = TextMuted)) {
                                append("Im a new user ")
                            }
                            withStyle(SpanStyle(color = EsportsOrange, fontWeight = FontWeight.Bold)) {
                                append("Register Now")
                            }
                        },
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clickable { onToggleMode("register") }
                            .padding(8.dp)
                            .testTag("switch_to_register_btn")
                    )
                } else {
                    Text(
                        text = "Sign Up",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    AuthTextField(
                        value = regUsername,
                        onValueChange = { regUsername = it },
                        placeholder = "UserName",
                        testTag = "reg_username_input"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    AuthTextField(
                        value = regEmail,
                        onValueChange = { regEmail = it },
                        placeholder = "Email Address",
                        keyboardType = KeyboardType.Email,
                        testTag = "reg_email_input"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    AuthTextField(
                        value = regPhone,
                        onValueChange = { regPhone = it },
                        placeholder = "Mobile Number",
                        keyboardType = KeyboardType.Phone,
                        testTag = "reg_phone_input"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    AuthPasswordField(
                        value = regPass,
                        onValueChange = { regPass = it },
                        placeholder = "Password",
                        visible = regPassVisible,
                        onToggleVisible = { regPassVisible = !regPassVisible },
                        testTag = "reg_password_input"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    AuthTextField(
                        value = regPromo,
                        onValueChange = { regPromo = it },
                        placeholder = "Promo Code (Optional)",
                        testTag = "reg_promo_input"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = regAgreed,
                            onCheckedChange = { regAgreed = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = EsportsOrange,
                                checkmarkColor = Color.Black,
                                uncheckedColor = TextMuted
                            )
                        )
                        Text(
                            text = "I agree to the Terms and Conditions and Privacy Policy",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }

                    Button(
                        onClick = { onRegister(regUsername, regEmail, regPhone, regPass, regPromo) },
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EsportsOrange,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("reg_submit_button")
                    ) {
                        Text(
                            text = "SIGN UP",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    OrDivider()

                    GoogleSignInButton(
                        text = "Sign up with Google",
                        testTag = "google_signup_button",
                        onClick = ::triggerGoogleSignIn
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = TextMuted)) {
                                append("Already have an account ")
                            }
                            withStyle(SpanStyle(color = EsportsOrange, fontWeight = FontWeight.Bold)) {
                                append("Sign in here")
                            }
                        },
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clickable { onToggleMode("login") }
                            .padding(8.dp)
                            .testTag("switch_to_login_btn")
                    )
                }
            }
        }
    }

    if (showGoogleChooserDialog) {
        GoogleAccountChooserDialog(
            onDismiss = { showGoogleChooserDialog = false },
            onSelectAccount = { email, name ->
                showGoogleChooserDialog = false
                onGoogleLogin(null, email, name, "")
            }
        )
    }
}

@Composable
private fun OrDivider() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = CardBorderDark)
        Text(
            text = "OR",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = CardBorderDark)
    }
}

@Composable
private fun GoogleSignInButton(
    text: String,
    testTag: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(1.dp, CardBorderDark),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = SurfaceElevatedDark,
            contentColor = TextWhite
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            GoogleLogoCanvas(modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite
            )
        }
    }
}

@Composable
private fun GoogleLogoCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.22f
        val inset = strokeWidth / 2f
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
        val topLeft = Offset(inset, inset)

        drawArc(
            color = Color(0xFFEA4335),
            startAngle = -145f,
            sweepAngle = 105f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 145f,
            sweepAngle = 70f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 45f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -10f,
            sweepAngle = 55f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(size.width * 0.50f, size.height * 0.50f),
            end = Offset(size.width - inset * 0.4f, size.height * 0.50f),
            strokeWidth = strokeWidth
        )
    }
}

@Composable
private fun GoogleAccountChooserDialog(
    onDismiss: () -> Unit,
    onSelectAccount: (String, String) -> Unit
) {
    var customName by remember { mutableStateOf("") }
    var customEmail by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardDark,
            border = BorderStroke(1.dp, CardBorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GoogleLogoCanvas(modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Sign in with Google",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Enter your Google account to continue to Bd Tournament",
                    fontSize = 13.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(18.dp))

                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    placeholder = { Text("Your Name", fontSize = 14.sp, color = TextMuted) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EsportsOrange,
                        unfocusedBorderColor = CardBorderDark,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("google_custom_name_input")
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = customEmail,
                    onValueChange = { customEmail = it },
                    placeholder = { Text("yourname@gmail.com", fontSize = 14.sp, color = TextMuted) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EsportsOrange,
                        unfocusedBorderColor = CardBorderDark,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("google_custom_email_input")
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val email = customEmail.trim()
                        if (email.isNotBlank()) {
                            val name = customName.trim().ifBlank { email.substringBefore("@") }
                            onSelectAccount(email, name)
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EsportsOrange,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("google_custom_continue_btn")
                ) {
                    Text("Continue", fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = TextMuted, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    testTag: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextMuted, fontSize = 14.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EsportsOrange,
            unfocusedBorderColor = CardBorderDark,
            focusedTextColor = TextWhite,
            unfocusedTextColor = TextWhite,
            focusedContainerColor = SurfaceElevatedDark,
            unfocusedContainerColor = SurfaceElevatedDark
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    )
}

@Composable
private fun AuthPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    visible: Boolean,
    onToggleVisible: () -> Unit,
    testTag: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextMuted, fontSize = 14.sp) },
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = onToggleVisible) {
                Icon(
                    imageVector = if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = "Toggle password visibility",
                    tint = TextMuted
                )
            }
        },
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EsportsOrange,
            unfocusedBorderColor = CardBorderDark,
            focusedTextColor = TextWhite,
            unfocusedTextColor = TextWhite,
            focusedContainerColor = SurfaceElevatedDark,
            unfocusedContainerColor = SurfaceElevatedDark
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    )
}
