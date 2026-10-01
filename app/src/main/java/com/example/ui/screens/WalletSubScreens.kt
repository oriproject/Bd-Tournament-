package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.isSameDayAsToday
import com.example.data.local.AppSettingsData
import com.example.data.local.TransactionEntity
import com.example.data.local.UserEntity
import com.example.ui.SubScreen
import com.example.ui.components.SubPageTopBar
import com.example.ui.theme.BgDark
import com.example.ui.theme.BkashDark
import com.example.ui.theme.BkashPink
import com.example.ui.theme.CardBorderDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EsportsGold
import com.example.ui.theme.EsportsGreen
import com.example.ui.theme.EsportsOrange
import com.example.ui.theme.NagadDark
import com.example.ui.theme.NagadOrange
import com.example.ui.theme.RocketDark
import com.example.ui.theme.RocketPurple
import com.example.ui.theme.SurfaceElevatedDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WithdrawGreen

@Composable
fun WalletScreen(
    user: UserEntity?,
    appSettings: AppSettingsData,
    onBack: () -> Unit,
    onNavigateSub: (SubScreen) -> Unit
) {
    val context = LocalContext.current
    val deposit = user?.deposit?.toInt() ?: 0
    val winning = user?.winning?.toInt() ?: 0
    val totalBalance = deposit + winning

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .testTag("wallet_screen")
    ) {
        SubPageTopBar(title = "My Wallet", darkMode = true, onBack = onBack)

        // Wallet Gradient Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF1F2937), Color(0xFF111827))
                    )
                )
                .border(1.dp, EsportsOrange.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = EsportsOrange,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bd Tournament ",
                        color = TextWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "WALLET",
                        color = EsportsOrange,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(26.dp))
                Text(
                    text = "Available Balance",
                    color = EsportsGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "৳ $totalBalance",
                    color = TextWhite,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Text(
                text = "ESPORTS",
                color = EsportsOrange.copy(alpha = 0.8f),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }

        // Stats Card (Deposited | Winning)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = BorderStroke(1.dp, CardBorderDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp, horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = EsportsGreen,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "DEPOSITED",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "৳ $deposit",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite
                    )
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(60.dp)
                        .background(CardBorderDark)
                )
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = EsportsGold,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "WINNING",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "৳ $winning",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = EsportsGold
                    )
                }
            }
        }

        // Action Row: Add Money & Withdraw
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Button(
                onClick = { onNavigateSub(SubScreen.AddMoney) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EsportsOrange,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("wallet_add_money_btn")
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Money", fontWeight = FontWeight.Black, fontSize = 15.sp)
            }
            Button(
                onClick = { onNavigateSub(SubScreen.Withdraw) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WithdrawGreen),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("wallet_withdraw_btn")
            ) {
                Icon(Icons.Default.Payments, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Withdraw", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
            }
        }

        // Quick Actions
        Text(
            text = "Quick Actions",
            color = TextWhite,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        )
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = BorderStroke(1.dp, CardBorderDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 8.dp)
                .clickable { onNavigateSub(SubScreen.History) }
                .testTag("wallet_history_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceElevatedDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ShowChart,
                        contentDescription = null,
                        tint = EsportsOrange
                    )
                }
                Spacer(modifier = Modifier.width(15.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Transaction History", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = TextWhite)
                    Text("View all your transactions", fontSize = 12.sp, color = TextMuted)
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextMuted)
            }
        }

        // Learn and Support
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = EsportsOrange, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Learn and Support", color = TextWhite, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LearnSupportItem(
                icon = Icons.Default.SportsEsports,
                title = "How to add money?",
                subtitle = "কিভাবে টাকা অ্যাড করবেন",
                onClick = { openExternalUrl(context, appSettings.howToAddMoneyLink) }
            )
            LearnSupportItem(
                icon = Icons.Default.EmojiEvents,
                title = "How to join a match?",
                subtitle = "কিভাবে ম্যাচে জয়েন করবেন",
                onClick = { openExternalUrl(context, appSettings.howToPlayLink) }
            )
            LearnSupportItem(
                icon = Icons.Default.VpnKey,
                title = "How to get Room ID?",
                subtitle = "কিভাবে রুম আইডি পাবেন",
                onClick = { openExternalUrl(context, appSettings.howToGetRoomIdLink) }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LearnSupportItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, CardBorderDark),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = EsportsOrange,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(15.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = TextWhite)
                Text(subtitle, fontSize = 12.sp, color = TextMuted)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextMuted)
        }
    }
}

@Composable
fun AddMoneyScreen(
    currentMethod: String,
    appSettings: AppSettingsData,
    onSelectMethod: (String) -> Unit,
    onVerify: (String, String, String) -> Unit,
    onCopySuccess: () -> Unit,
    onBack: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current
    var amountInput by remember(currentMethod) { mutableStateOf("") }
    var senderNumber by remember(currentMethod) { mutableStateOf("") }
    var trxId by remember(currentMethod) { mutableStateOf("") }

    val (boxColor, inputBgColor, dialCode, methodLabel, recipientNumber) = when (currentMethod) {
        "rocket" -> PayConfig(RocketPurple, RocketDark, "*322#", "Rocket", appSettings.rocketNumber)
        "nagad" -> PayConfig(NagadOrange, NagadDark, "*167#", "NAGAD", appSettings.nagadNumber)
        else -> PayConfig(BkashPink, BkashDark, "*247#", "BKASH", appSettings.bkashNumber)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .imePadding()
            .testTag("add_money_screen")
    ) {
        SubPageTopBar(
            title = "Add Money",
            onBack = {
                focusManager.clearFocus()
                onBack()
            }
        )

        // 3 Method Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 15.dp, end = 15.dp, top = 15.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PayMethodTabCard(
                name = "bKash",
                logoUrl = "https://i.ibb.co.com/qF1xD5xj/image-search-1769091253468.png",
                selected = currentMethod == "bkash",
                badgeColor = BkashPink,
                onClick = { onSelectMethod("bkash") },
                modifier = Modifier.weight(1f)
            )
            PayMethodTabCard(
                name = "Rocket",
                logoUrl = "https://i.ibb.co.com/MDt4TzDf/image-search-1769091463880.png",
                selected = currentMethod == "rocket",
                badgeColor = RocketPurple,
                onClick = { onSelectMethod("rocket") },
                modifier = Modifier.weight(1f)
            )
            PayMethodTabCard(
                name = "Nagad",
                logoUrl = "https://i.ibb.co.com/4gmdTj1f/image-search-1769091285966.png",
                selected = currentMethod == "nagad",
                badgeColor = NagadOrange,
                onClick = { onSelectMethod("nagad") },
                modifier = Modifier.weight(1f)
            )
        }

        // Colored Box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = boxColor,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "টাকার পরিমাণ, একাউন্ট নাম্বার ও ট্রানজেকশন আইডি দিন",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Amount Input Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(inputBgColor)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (amountInput.isEmpty()) {
                            Text(
                                text = "টাকার পরিমাণ লিখুন (যেমন: 100)",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                        BasicTextField(
                            value = amountInput,
                            onValueChange = { amountInput = it },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.SemiBold
                            ),
                            cursorBrush = SolidColor(Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_money_amount_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Sender Account Number Input Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(inputBgColor)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (senderNumber.isEmpty()) {
                            Text(
                                text = "যে নাম্বার থেকে টাকা পাঠিয়েছেন (Account Number)",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                        BasicTextField(
                            value = senderNumber,
                            onValueChange = { senderNumber = it },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.SemiBold
                            ),
                            cursorBrush = SolidColor(Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_money_sender_number_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Transaction ID Input Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(inputBgColor)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (trxId.isEmpty()) {
                            Text(
                                text = "ট্রানজেকশন আইডি দিন (TrxID)",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                        BasicTextField(
                            value = trxId,
                            onValueChange = { trxId = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.SemiBold
                            ),
                            cursorBrush = SolidColor(Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("trx_id_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(15.dp))

                    BulletInstruction("$dialCode ডায়াল করে আপনার $methodLabel মোবাইল মেনুতে যান অথবা $methodLabel অ্যাপে যান ।")
                    BulletInstruction(
                        text = if (currentMethod == "bkash") "Send Money/Make Payment - এ ক্লিক করুন ।" else "Send Money - এ ক্লিক করুন ।",
                        textColor = Color(0xFFFFEB3B),
                        bold = true
                    )
                    BulletInstruction("প্রাপক নম্বর হিসেবে নিচের এই নম্বরটি লিখুন")

                    Row(
                        modifier = Modifier.padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = recipientNumber,
                            color = Color(0xFFFFEB3B),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(Send Money)",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.Black.copy(alpha = 0.35f),
                            modifier = Modifier.clickable {
                                clipboard.setText(AnnotatedString(recipientNumber))
                                onCopySuccess()
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    BulletInstruction("নিশ্চিত করতে এখন আপনার $methodLabel মোবাইল মেনু পিন লিখুন।")
                    BulletInstruction("এখন উপরের বক্সে আপনার Amount, যে নাম্বার থেকে টাকা পাঠিয়েছেন এবং Transaction ID দিন আর নিচের VERIFY বাটনে ক্লিক করুন।")
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    onVerify(trxId, amountInput, senderNumber)
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = boxColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("verify_trx_button")
            ) {
                Text(
                    text = "VERIFY",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

private data class PayConfig(
    val boxColor: Color,
    val inputBgColor: Color,
    val dialCode: String,
    val methodLabel: String,
    val recipientNumber: String
)

@Composable
private fun PayMethodTabCard(
    name: String,
    logoUrl: String,
    selected: Boolean,
    badgeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CardDark)
            .border(
                width = 2.dp,
                color = if (selected) EsportsOrange else CardBorderDark,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 5.dp)
            .testTag("pay_tab_${name.lowercase()}")
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = EsportsOrange,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(16.dp)
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = logoUrl,
                    contentDescription = name,
                    modifier = Modifier.size(30.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) EsportsOrange else TextWhite
            )
        }
    }
}

@Composable
private fun BulletInstruction(
    text: String,
    textColor: Color = Color.White,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 7.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun WithdrawScreen(
    user: UserEntity?,
    transactions: List<TransactionEntity> = emptyList(),
    selectedMethod: String,
    onSelectMethod: (String) -> Unit,
    onSubmitWithdraw: (String, String) -> Unit,
    onBack: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var mobileNumber by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    val winningAvailable = user?.winning?.toInt()?.coerceAtLeast(0) ?: 0
    val todayWithdrawCount = remember(transactions, user?.uid) {
        transactions.count { tx ->
            (user == null || tx.uid == user.uid) &&
                tx.type.contains("Withdraw", ignoreCase = true) &&
                !tx.status.contains("Reject", ignoreCase = true) &&
                isSameDayAsToday(tx.date)
        }.coerceIn(0, 1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .imePadding()
            .testTag("withdraw_screen")
    ) {
        SubPageTopBar(
            title = "Withdraw Money",
            darkMode = true,
            onBack = {
                focusManager.clearFocus()
                onBack()
            }
        )

        Column(modifier = Modifier.padding(20.dp)) {
            // Balance Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, CardBorderDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 26.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = EsportsGold,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Withdrawable Winning Balance",
                        color = TextMuted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "BDT $winningAvailable",
                        color = EsportsOrange,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "শুধুমাত্র ম্যাচ জিতে পাওয়া Winning টাকা উইথড্র করা যাবে",
                        color = EsportsGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Daily Withdraw Limit Box (1 Withdraw per Day)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
                        )
                    )
                    .padding(horizontal = 18.dp, vertical = 16.dp)
                    .testTag("withdraw_limit_card")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "আজকের উইথড্র লিমিট",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.22f))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                .testTag("withdraw_limit_badge")
                        ) {
                            Text(
                                text = if (todayWithdrawCount >= 1) "১ / ১ বার" else "০ / ১ বার",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.28f))
                    ) {
                        if (todayWithdrawCount >= 1) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFFFFD54F))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (todayWithdrawCount >= 1) {
                            "আজকের উইথড্র লিমিট শেষ (দিনে সর্বোচ্চ ১ বার)"
                        } else {
                            "আজ আরও ১ বার উইথড্র করতে পারবেন"
                        },
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.testTag("withdraw_limit_status_text")
                    )
                }
            }

            // Select Payment Method
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
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = EsportsOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Select Payment Method",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite
                        )
                    }
                    Spacer(modifier = Modifier.height(15.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        WithdrawMethodCard(
                            name = "bKash",
                            logoUrl = "https://i.ibb.co.com/qF1xD5xj/image-search-1769091253468.png",
                            selected = selectedMethod == "bKash",
                            onClick = { onSelectMethod("bKash") },
                            modifier = Modifier.weight(1f)
                        )
                        WithdrawMethodCard(
                            name = "Nagad",
                            logoUrl = "https://i.ibb.co.com/4gmdTj1f/image-search-1769091285966.png",
                            selected = selectedMethod == "Nagad",
                            onClick = { onSelectMethod("Nagad") },
                            modifier = Modifier.weight(1f)
                        )
                        WithdrawMethodCard(
                            name = "Rocket",
                            logoUrl = "https://i.ibb.co.com/MDt4TzDf/image-search-1769091463880.png",
                            selected = selectedMethod == "Rocket",
                            onClick = { onSelectMethod("Rocket") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Withdrawal Details
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, CardBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = EsportsOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Withdrawal Details",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Mobile Number Input
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = EsportsOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (mobileNumber.isEmpty()) {
                                Text("Mobile Number", color = TextMuted, fontSize = 16.sp)
                            }
                            BasicTextField(
                                value = mobileNumber,
                                onValueChange = { mobileNumber = it },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                textStyle = TextStyle(
                                    color = TextWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                cursorBrush = SolidColor(EsportsOrange),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("withdraw_number_input")
                            )
                        }
                    }
                    HorizontalDivider(color = CardBorderDark)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Amount Input
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = EsportsGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (amountStr.isEmpty()) {
                                Text("Amount to Withdraw", color = TextMuted, fontSize = 16.sp)
                            }
                            BasicTextField(
                                value = amountStr,
                                onValueChange = { amountStr = it },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = TextStyle(
                                    color = TextWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                cursorBrush = SolidColor(EsportsOrange),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("withdraw_amount_input")
                            )
                        }
                    }
                    HorizontalDivider(color = CardBorderDark)

                    Spacer(modifier = Modifier.height(18.dp))

                    // Min Notice
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceElevatedDark)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = EsportsGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MINIMUM WITHDRAW 80 TK",
                            color = EsportsGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            onSubmitWithdraw(mobileNumber, amountStr)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EsportsOrange,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("withdraw_submit_button")
                    ) {
                        Text(
                            text = "WITHDRAW MONEY",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WithdrawMethodCard(
    name: String,
    logoUrl: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) SurfaceElevatedDark else CardDark)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) EsportsOrange else CardBorderDark,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = logoUrl,
                contentDescription = name,
                modifier = Modifier.size(30.dp),
                contentScale = ContentScale.Fit
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) EsportsOrange else TextWhite
        )
    }
}

@Composable
fun HistoryScreen(
    transactions: List<TransactionEntity>,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .testTag("history_screen")
    ) {
        SubPageTopBar(title = "Transactions", onBack = onBack)

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(36.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No Transactions",
                    color = TextMuted,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(transactions, key = { it.id }) { t ->
                    val statusText = t.status.ifBlank { "Pending" }
                    val isApproved = statusText.contains("Approv", ignoreCase = true) ||
                            statusText.contains("Success", ignoreCase = true) ||
                            statusText.contains("Complet", ignoreCase = true)
                    val isRejected = statusText.contains("Reject", ignoreCase = true) ||
                            statusText.contains("Cancel", ignoreCase = true) ||
                            statusText.contains("Fail", ignoreCase = true)
                    val statusColor = when {
                        isApproved -> EsportsGreen
                        isRejected -> DangerRed
                        else -> EsportsGold
                    }
                    val trxLabel = if (t.txId.isNotBlank()) "TrxID: ${t.txId}" else "#TRX"

                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        border = BorderStroke(1.dp, CardBorderDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 15.dp, vertical = 5.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .width(5.dp)
                                    .height(78.dp)
                                    .background(statusColor)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(15.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = t.type.uppercase(),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = TextWhite
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    if (t.number.isNotBlank()) {
                                        Text(
                                            text = "Number : ${t.number}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = EsportsOrange
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                    }
                                    Text(
                                        text = t.date,
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Amount : ৳${t.amount.toInt()}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = statusColor
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = SurfaceElevatedDark,
                                        border = BorderStroke(1.dp, CardBorderDark)
                                    ) {
                                        Text(
                                            text = trxLabel,
                                            color = EsportsOrange,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = statusText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor
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
