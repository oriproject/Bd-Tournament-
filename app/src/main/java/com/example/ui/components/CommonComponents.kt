package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.MatchEntity
import com.example.data.local.formatMatchCountdown
import com.example.data.local.formatMatchScheduleTime
import com.example.ui.AlertMessage
import com.example.ui.AlertType
import com.example.ui.theme.CardBorderDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DetailsBlue
import com.example.ui.theme.EsportsGold
import com.example.ui.theme.EsportsGreen
import com.example.ui.theme.EsportsOrange
import com.example.ui.theme.NavBarDark
import com.example.ui.theme.SplashBgBottom
import com.example.ui.theme.SplashBgTop
import com.example.ui.theme.SurfaceElevatedDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.TimerGreen
import com.example.ui.theme.WarningOrange

@Composable
fun SplashScreenView(
    appName: String,
    appLogoUrl: String
) {
    val transition = rememberInfiniteTransition(label = "splash")
    val lineFraction by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lineExpand"
    )
    val particleProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particles"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SplashBgTop, SplashBgBottom)))
            .testTag("splash_screen")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val xPercents = listOf(0.10f, 0.22f, 0.35f, 0.50f, 0.65f, 0.80f, 0.90f)
            val speedOffsets = listOf(0.0f, 0.25f, 0.5f, 0.15f, 0.7f, 0.4f, 0.85f)
            xPercents.forEachIndexed { index, xPct ->
                val p = (particleProgress + speedOffsets[index]) % 1f
                val y = size.height * (1f - p)
                val alpha = if (p < 0.2f) p * 4f else (1f - p) * 0.8f
                drawCircle(
                    color = EsportsOrange.copy(alpha = alpha.coerceIn(0f, 0.75f)),
                    radius = 3.dp.toPx(),
                    center = Offset(size.width * xPct, y)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(CardDark)
                    .border(2.5.dp, EsportsOrange, CircleShape)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_app_icon),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Bd Tournament",
                color = EsportsOrange,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tournaments & Esports • v2.0",
                color = TextMuted,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(80.dp))
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 52.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome back!",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .width((150 * lineFraction).dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(EsportsOrange)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(3) { i ->
                    val dotScale = ((particleProgress * 3 + i * 0.33f) % 1f)
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(EsportsOrange.copy(alpha = 0.3f + 0.6f * dotScale))
                    )
                }
            }
        }
    }
}

@Composable
fun SubPageTopBar(
    title: String,
    darkMode: Boolean = true,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(NavBarDark)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onBack)
                .padding(horizontal = 16.dp, vertical = 15.dp)
                .testTag("subpage_back_button"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = EsportsOrange,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                color = TextWhite,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(CardBorderDark)
        )
    }
}

@Composable
fun MatchCardItem(
    match: MatchEntity,
    categoryImg: String,
    mode: String, // "play", "joined", "result"
    isJoined: Boolean,
    currentTimeMillis: Long,
    onCardClick: () -> Unit,
    onJoinClick: () -> Unit,
    onRoomDetailsClick: () -> Unit,
    onPrizeDetailsClick: () -> Unit
) {
    val joined = match.joined
    val total = if (match.total > 0) match.total else 48
    val progress = (joined.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    val spotsLeft = (total - joined).coerceAtLeast(0)

    val scheduleDisplayText = formatMatchScheduleTime(match)
    val countdownText = formatMatchCountdown(match, currentTimeMillis)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clickable(onClick = onCardClick)
            .testTag("match_card_${match.dbKey}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, CardBorderDark)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                if (categoryImg.isBlank() || categoryImg.startsWith("local_")) {
                    Image(
                        painter = painterResource(id = R.drawable.img_category_br),
                        contentDescription = match.title,
                        modifier = Modifier
                            .width(90.dp)
                            .height(56.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    AsyncImage(
                        model = categoryImg,
                        contentDescription = match.title,
                        placeholder = painterResource(id = R.drawable.img_category_br),
                        error = painterResource(id = R.drawable.img_category_br),
                        modifier = Modifier
                            .width(90.dp)
                            .height(56.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${match.title} | Mobile | Regular",
                        color = TextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 19.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = scheduleDisplayText,
                        color = EsportsOrange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Stats Grid (2 rows x 3 columns)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCell("WIN PRIZE", "${match.totalPrize} TK", EsportsGold, Modifier.weight(1f))
                    StatCell("ENTRY TYPE", match.type, TextWhite, Modifier.weight(1f))
                    StatCell("ENTRY FEE", "${match.entry} TK", EsportsGreen, Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCell("PER KILL", "${match.perKill} TK", TextWhite, Modifier.weight(1f))
                    StatCell("MAP", match.map, TextWhite, Modifier.weight(1f))
                    StatCell("VERSION", "MOBILE", TextWhite, Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Join & Progress Row
            if (mode != "result") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(SurfaceElevatedDark)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(EsportsOrange, EsportsGold)
                                        )
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Only $spotsLeft spots are left",
                                fontSize = 11.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "$joined/$total",
                                fontSize = 11.sp,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (mode == "joined" || isJoined) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SurfaceElevatedDark,
                            border = BorderStroke(1.dp, EsportsGreen)
                        ) {
                            Text(
                                text = "JOINED",
                                color = EsportsGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp)
                            )
                        }
                    } else {
                        Button(
                            onClick = onJoinClick,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EsportsOrange,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier.testTag("join_match_btn_${match.dbKey}")
                        ) {
                            Text(
                                text = "JOIN",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onCardClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EsportsOrange,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.testTag("result_btn_${match.dbKey}")
                    ) {
                        Text(
                            text = "RESULT",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Room Details & Prize Details Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onRoomDetailsClick)
                        .testTag("room_details_btn_${match.dbKey}"),
                    shape = RoundedCornerShape(8.dp),
                    color = SurfaceElevatedDark,
                    border = BorderStroke(1.dp, CardBorderDark)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 9.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "Room Details",
                            tint = EsportsOrange,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Room Details",
                            color = TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (mode != "result") {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onPrizeDetailsClick)
                            .testTag("prize_details_btn_${match.dbKey}"),
                        shape = RoundedCornerShape(8.dp),
                        color = SurfaceElevatedDark,
                        border = BorderStroke(1.dp, CardBorderDark)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 9.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Prize Details",
                                tint = EsportsGold,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Prize Details",
                                color = TextWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Countdown Footer Timer
            if (mode != "result") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TimerGreen)
                        .padding(vertical = 9.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = countdownText,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCell(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceElevatedDark)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = TextMuted,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            color = valueColor,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun AllPopupsAndModals(
    showStartupModal: Boolean,
    startupPopupText: String,
    onDismissStartup: () -> Unit,
    prizeModalMatch: MatchEntity?,
    onDismissPrize: () -> Unit,
    roomModalMatch: MatchEntity?,
    onDismissRoom: () -> Unit,
    showJoinWarning: Boolean,
    onDismissJoinWarning: () -> Unit,
    showLogoutDialog: Boolean,
    onConfirmLogout: () -> Unit,
    onDismissLogout: () -> Unit,
    alertMessage: AlertMessage?,
    onDismissAlert: () -> Unit,
    onCopySuccess: () -> Unit,
    isLoading: Boolean
) {
    val clipboard = LocalClipboardManager.current

    if (showStartupModal) {
        Dialog(onDismissRequest = onDismissStartup) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CardDark,
                border = BorderStroke(1.dp, CardBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "আসসালামু আলাইকুম 😍",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        color = EsportsOrange
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = startupPopupText,
                        fontSize = 14.sp,
                        color = TextWhite.copy(alpha = 0.9f),
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onDismissStartup,
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EsportsOrange,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("startup_modal_okay_btn")
                    ) {
                        Text(
                            text = "Okay",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }

    if (prizeModalMatch != null) {
        Dialog(onDismissRequest = onDismissPrize) {
            Box(contentAlignment = Alignment.TopCenter) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    border = BorderStroke(1.dp, CardBorderDark)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(EsportsOrange)
                                .padding(vertical = 15.dp, horizontal = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "TOTAL WINPRIZE",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                            Text(
                                text = prizeModalMatch.title,
                                fontSize = 12.sp,
                                color = Color.Black.copy(alpha = 0.8f),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (prizeModalMatch.prizeDesc.isNotBlank()) {
                                prizeModalMatch.prizeDesc.split("\n").forEach { line ->
                                    Text(
                                        text = line,
                                        fontSize = 14.sp,
                                        color = TextWhite,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            } else {
                                val total = prizeModalMatch.totalPrize
                                val w = (total * 0.4).toInt()
                                val s = (total * 0.2).toInt()
                                val t = (total * 0.1).toInt()
                                Text("👑 Winner - $w Taka", fontSize = 14.sp, color = TextWhite)
                                Text("🥈 2nd Position - $s Taka", fontSize = 14.sp, color = TextWhite)
                                Text("🥉 3rd Position - $t Taka", fontSize = 14.sp, color = TextWhite)
                                Text("🏅 4th Position - 20 Taka", fontSize = 14.sp, color = TextWhite)
                                Text("🏅 5th Position - 10 Taka", fontSize = 14.sp, color = TextWhite)
                                Text("🔥 Per Kill : ${prizeModalMatch.perKill} Taka", fontSize = 14.sp, color = TextWhite)
                                Text("🏆 Total Prize Pool: $total Taka", fontSize = 14.sp, color = EsportsGold, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Surface(
                    shape = CircleShape,
                    color = SurfaceElevatedDark,
                    border = BorderStroke(1.dp, CardBorderDark),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(34.dp)
                        .clickable(onClick = onDismissPrize)
                        .testTag("close_prize_modal_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("X", fontWeight = FontWeight.Bold, color = TextWhite)
                    }
                }
            }
        }
    }

    if (roomModalMatch != null) {
        Dialog(onDismissRequest = onDismissRoom) {
            Box(contentAlignment = Alignment.TopCenter) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    border = BorderStroke(1.dp, CardBorderDark)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(EsportsOrange)
                                .padding(vertical = 15.dp, horizontal = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "ROOM DETAILS",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                            Text(
                                text = "Match Room Information",
                                fontSize = 11.sp,
                                color = Color.Black.copy(alpha = 0.8f),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Room ID", fontSize = 12.sp, color = TextMuted)
                            Text(
                                text = roomModalMatch.roomId,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SurfaceElevatedDark,
                                border = BorderStroke(1.dp, CardBorderDark),
                                modifier = Modifier.clickable {
                                    clipboard.setText(AnnotatedString(roomModalMatch.roomId))
                                    onCopySuccess()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Room ID",
                                        modifier = Modifier.size(14.dp),
                                        tint = EsportsOrange
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text("COPY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text("Password", fontSize = 12.sp, color = TextMuted)
                            Text(
                                text = roomModalMatch.roomPass.ifBlank { "--" },
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SurfaceElevatedDark,
                                border = BorderStroke(1.dp, CardBorderDark),
                                modifier = Modifier.clickable {
                                    clipboard.setText(AnnotatedString(roomModalMatch.roomPass))
                                    onCopySuccess()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Room Password",
                                        modifier = Modifier.size(14.dp),
                                        tint = EsportsOrange
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text("COPY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                }
                            }
                        }
                    }
                }
                Surface(
                    shape = CircleShape,
                    color = SurfaceElevatedDark,
                    border = BorderStroke(1.dp, CardBorderDark),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(34.dp)
                        .clickable(onClick = onDismissRoom)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("X", fontWeight = FontWeight.Bold, color = TextWhite)
                    }
                }
            }
        }
    }

    if (showJoinWarning) {
        Dialog(onDismissRequest = onDismissJoinWarning) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = CardDark,
                border = BorderStroke(1.dp, CardBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = WarningOrange,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "ম্যাচে জয়েন করুন",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "আপনি এই ম্যাচে জয়েন করেননি",
                        fontSize = 14.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = onDismissJoinWarning,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EsportsOrange,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("ঠিক আছে", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        Dialog(onDismissRequest = onDismissLogout) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = CardDark,
                border = BorderStroke(1.dp, CardBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = EsportsOrange,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Logout?", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Are you sure you want to logout?",
                        fontSize = 14.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = onConfirmLogout,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EsportsOrange,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Yes", fontWeight = FontWeight.Black)
                        }
                        Button(
                            onClick = onDismissLogout,
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (alertMessage != null) {
        Dialog(onDismissRequest = {
            val cb = alertMessage.onConfirm
            onDismissAlert()
            cb?.invoke()
        }) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = CardDark,
                border = BorderStroke(1.dp, CardBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val (icon, tint) = when (alertMessage.type) {
                        AlertType.SUCCESS -> Icons.Default.CheckCircle to EsportsGreen
                        AlertType.ERROR -> Icons.Default.Error to DangerRed
                        AlertType.WARNING -> Icons.Default.Warning to EsportsOrange
                        AlertType.INFO -> Icons.Default.Info to DetailsBlue
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = alertMessage.title,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = alertMessage.message,
                        fontSize = 14.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            val cb = alertMessage.onConfirm
                            onDismissAlert()
                            cb?.invoke()
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EsportsOrange,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.testTag("alert_confirm_btn")
                    ) {
                        Text(alertMessage.confirmText, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = EsportsOrange,
                strokeWidth = 5.dp,
                modifier = Modifier.size(50.dp)
            )
        }
    }
}
