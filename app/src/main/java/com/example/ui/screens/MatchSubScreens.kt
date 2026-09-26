package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CategoryEntity
import com.example.data.local.MatchEntity
import com.example.data.local.ParticipantEntity
import com.example.ui.components.MatchCardItem
import com.example.ui.components.SubPageTopBar
import com.example.ui.theme.BgDark
import com.example.ui.theme.CardBorderDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.EsportsGold
import com.example.ui.theme.EsportsGreen
import com.example.ui.theme.EsportsOrange
import com.example.ui.theme.SurfaceElevatedDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.TimerGreen

@Composable
fun CategoryMatchesScreen(
    categoryId: String,
    categories: List<CategoryEntity>,
    matches: List<MatchEntity>,
    joinedMatchKeys: Set<String>,
    filterStatus: String,
    currentTimeMillis: Long,
    onFilterChange: (String) -> Unit,
    onBack: () -> Unit,
    onOpenMatchDetails: (String) -> Unit,
    onOpenResultDetails: (String) -> Unit,
    onPrepJoin: (MatchEntity, Boolean) -> Unit,
    onCheckRoom: (MatchEntity, Boolean) -> Unit,
    onOpenPrizePool: (MatchEntity) -> Unit
) {
    val category = categories.find { it.id == categoryId }
    val categoryImg = category?.img ?: ""
    val filtered = matches.filter { it.categoryId == categoryId && it.status == filterStatus }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .testTag("category_matches_screen")
    ) {
        SubPageTopBar(title = category?.name?.uppercase() ?: "Back to Home", onBack = onBack)

        // Match Filter Tabs
        Surface(
            color = CardDark,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    MatchFilterTabItem(
                        label = "UPCOMING",
                        selected = filterStatus == "Upcoming",
                        onClick = { onFilterChange("Upcoming") },
                        modifier = Modifier.weight(1f)
                    )
                    MatchFilterTabItem(
                        label = "ON-GOING",
                        selected = filterStatus == "Ongoing",
                        onClick = { onFilterChange("Ongoing") },
                        modifier = Modifier.weight(1f)
                    )
                    MatchFilterTabItem(
                        label = "RESULT",
                        selected = filterStatus == "Finished",
                        onClick = { onFilterChange("Finished") },
                        modifier = Modifier.weight(1f)
                    )
                }
                HorizontalDivider(color = CardBorderDark)
            }
        }

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(36.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No $filterStatus Matches Found",
                    color = TextMuted,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(15.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered, key = { it.dbKey }) { m ->
                    val isJoined = joinedMatchKeys.contains(m.dbKey)
                    val mode = if (filterStatus == "Finished") "result" else "play"
                    MatchCardItem(
                        match = m,
                        categoryImg = categoryImg,
                        mode = mode,
                        isJoined = isJoined,
                        currentTimeMillis = currentTimeMillis,
                        onCardClick = {
                            if (filterStatus == "Finished") {
                                onOpenResultDetails(m.dbKey)
                            } else {
                                onOpenMatchDetails(m.dbKey)
                            }
                        },
                        onJoinClick = { onPrepJoin(m, isJoined) },
                        onRoomDetailsClick = { onCheckRoom(m, isJoined) },
                        onPrizeDetailsClick = { onOpenPrizePool(m) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MatchFilterTabItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold,
            color = if (selected) EsportsOrange else TextMuted,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(if (selected) EsportsOrange else Color.Transparent)
        )
    }
}

@Composable
fun MatchDetailsScreen(
    match: MatchEntity?,
    participants: List<ParticipantEntity>,
    currentTimeMillis: Long,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .testTag("match_details_screen")
    ) {
        SubPageTopBar(title = "Match Details", onBack = onBack)

        if (match == null) return@Column

        val diff = match.timestamp - currentTimeMillis
        val countdownText = if (diff <= 0L) {
            "Match Started"
        } else {
            val h = (diff / (1000 * 60 * 60)) % 24
            val m = (diff / (1000 * 60)) % 60
            val s = (diff / 1000) % 60
            "Starts In: ${h}h:${m}m:${s}s"
        }

        val matchParts = participants.filter { it.matchKey == match.dbKey }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Info Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardDark)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = match.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = TextWhite
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Starts In: ${match.time}",
                    fontSize = 14.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
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
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            HorizontalDivider(color = CardBorderDark)

            Spacer(modifier = Modifier.height(12.dp))

            // Participants Table Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardDark)
                    .padding(15.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Participants",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = TextWhite
                    )
                    Text(
                        text = "${matchParts.size}/${if (match.total > 0) match.total else 48}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = EsportsOrange
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(EsportsOrange)
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (matchParts.isEmpty()) {
                    Text(
                        text = "No participants joined yet.",
                        color = TextMuted,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    )
                } else {
                    matchParts.forEachIndexed { idx, p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${idx + 1}",
                                fontSize = 13.sp,
                                color = TextMuted,
                                modifier = Modifier.weight(0.12f)
                            )
                            Text(
                                text = p.ign.ifBlank { "Unknown" },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite,
                                modifier = Modifier.weight(0.48f)
                            )
                            Text(
                                text = "${p.kills}",
                                fontSize = 13.sp,
                                color = TextWhite,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(0.20f)
                            )
                            Text(
                                text = "${p.win}",
                                fontSize = 13.sp,
                                color = EsportsGold,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(0.20f)
                            )
                        }
                        HorizontalDivider(color = CardBorderDark)
                    }
                }
            }
        }
    }
}

@Composable
fun ResultDetailsScreen(
    match: MatchEntity?,
    participants: List<ParticipantEntity>,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .testTag("result_details_screen")
    ) {
        SubPageTopBar(title = "Results", darkMode = true, onBack = onBack)

        if (match == null) return@Column

        val matchParts = participants
            .filter { it.matchKey == match.dbKey }
            .sortedByDescending { it.win }
        val winners = matchParts.filter { it.win > 0 }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(15.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, CardBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Title Box
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${match.title} | Mobile |".uppercase(),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = TextWhite,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Organized On ${match.time}",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                    HorizontalDivider(color = CardBorderDark)

                    // Stats Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 15.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ResultStatCol("WIN PRIZE", "৳${match.totalPrize}", EsportsGold)
                        ResultStatCol("PER KILL", "৳${match.perKill}", TextWhite)
                        ResultStatCol("ENTRY FEE", "৳${match.entry}", EsportsGreen)
                    }

                    // WINNER WINNER CHICKEN DINNER
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(EsportsOrange)
                            .padding(9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "WINNER WINNER CHICKEN DINNER",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                    ResultTableHead()
                    if (winners.isEmpty()) {
                        Text(
                            text = "No winners declared yet",
                            color = TextMuted,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        )
                    } else {
                        winners.forEachIndexed { idx, p ->
                            ResultTableRow(idx + 1, p.ign, p.kills, p.win)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // FULL RESULT
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(EsportsOrange)
                            .padding(9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "FULL RESULT",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                    ResultTableHead()
                    if (matchParts.isEmpty()) {
                        Text(
                            text = "No participants",
                            color = TextMuted,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        )
                    } else {
                        matchParts.forEachIndexed { idx, p ->
                            ResultTableRow(idx + 1, p.ign, p.kills, p.win)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultStatCol(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 15.sp, color = valueColor, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun ResultTableHead() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceElevatedDark)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text("#", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.12f), textAlign = TextAlign.Center)
        Text("Player Name", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.48f))
        Text("Kills", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.20f), textAlign = TextAlign.Center)
        Text("Winning", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.20f), textAlign = TextAlign.End)
    }
}

@Composable
private fun ResultTableRow(rank: Int, name: String, kills: Int, win: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardDark)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("$rank", fontSize = 13.sp, color = TextMuted, modifier = Modifier.weight(0.12f), textAlign = TextAlign.Center)
            Text(name.ifBlank { "Unknown" }, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextWhite, modifier = Modifier.weight(0.48f))
            Text("$kills", fontSize = 13.sp, color = TextWhite, modifier = Modifier.weight(0.20f), textAlign = TextAlign.Center)
            Text("৳$win", fontSize = 13.sp, color = EsportsGold, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.20f), textAlign = TextAlign.End)
        }
        HorizontalDivider(color = CardBorderDark)
    }
}

@Composable
fun JoinMatchScreen(
    match: MatchEntity?,
    onBack: () -> Unit,
    onConfirmJoin: (MatchEntity, List<String>) -> Unit
) {
    if (match == null) return

    val slots = when {
        match.type.contains("duo", ignoreCase = true) -> 2
        match.type.contains("squad", ignoreCase = true) -> 4
        else -> 1
    }

    val playerNames = remember(match.dbKey) {
        mutableStateListOf<String>().apply {
            repeat(slots) { add("") }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .testTag("join_match_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 90.dp)
        ) {
            SubPageTopBar(title = "Match Joining", onBack = onBack)

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, CardBorderDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = match.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = match.time,
                        fontSize = 13.sp,
                        color = EsportsOrange,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(15.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Win Prize: ${match.totalPrize}TK",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = EsportsGold
                        )
                        Text(
                            text = "Entry Fee: ${match.entry}TK",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = EsportsGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(15.dp))

                    // Dashed Warning Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                drawRoundRect(
                                    color = EsportsOrange,
                                    style = Stroke(
                                        width = 1.5.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                                    ),
                                    cornerRadius = CornerRadius(6.dp.toPx())
                                )
                            }
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "*অবশ্যই এখানে আপনার গেমের এর নামটি দিয়ে জয়েন করবেন।",
                            color = EsportsGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(15.dp))

                    // Badge
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = EsportsOrange
                        ) {
                            Text(
                                text = match.type,
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 22.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Dynamic Inputs
                    repeat(slots) { i ->
                        OutlinedTextField(
                            value = playerNames[i],
                            onValueChange = { playerNames[i] = it },
                            placeholder = {
                                Text(
                                    text = "Player ${i + 1} Name (Game ID)",
                                    fontSize = 14.sp,
                                    color = TextMuted
                                )
                            },
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
                                .padding(bottom = 10.dp)
                                .testTag("join_player_input_${i + 1}")
                        )
                    }
                }
            }
        }

        // Fixed Bottom Join Now! Button
        Button(
            onClick = { onConfirmJoin(match, playerNames.toList()) },
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = EsportsOrange,
                contentColor = Color.Black
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .navigationBarsPadding()
                .height(52.dp)
                .testTag("confirm_join_now_button")
        ) {
            Text(
                text = "Join Now!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
