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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CategoryEntity
import com.example.data.local.MatchEntity
import com.example.data.local.ParticipantEntity
import com.example.data.local.formatMatchCountdown
import com.example.data.local.formatMatchScheduleTime
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

        val scheduleDisplayText = formatMatchScheduleTime(match)
        val countdownText = formatMatchCountdown(match, currentTimeMillis)

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
                    text = scheduleDisplayText,
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

            // Match Description Container (Above Participants)
            val resolvedDescription = remember(match.matchDesc, match.prizeDesc) {
                when {
                    match.matchDesc.isNotBlank() -> match.matchDesc
                    match.prizeDesc.isNotBlank() -> match.prizeDesc
                    else -> "• অবশ্যই আপনার সঠিক গেম আইডি (Game ID Name) দিয়ে জয়েন করবেন।\n• ম্যাচ শুরু হওয়ার ১০ মিনিট আগে Room ID এবং Password দেওয়া হবে।\n• যেকোনো ধরনের হ্যাক, প্যানেল বা টিমিং করলে একাউন্ট সাসপেন্ড করা হবে।"
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardDark)
                    .padding(15.dp)
                    .testTag("match_description_section")
            ) {
                Text(
                    text = "Match Instructions and Rules",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = TextWhite,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(EsportsOrange)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = resolvedDescription,
                    color = TextWhite.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp,
                    modifier = Modifier.fillMaxWidth()
                )
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

                    // WINNER LIST
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(EsportsOrange)
                            .padding(9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "WINNER LIST",
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

fun resolveAvailableTeamModes(match: MatchEntity, categoryName: String = ""): List<String> {
    val typeLower = match.type.trim().lowercase()
    val catWithoutCS = categoryName.lowercase().replace("clash squad", "")
    val titleWithoutCS = match.title.lowercase().replace("clash squad", "")
    val catIdLower = match.categoryId.lowercase()

    if (typeLower.contains("squad") || typeLower.contains("4v4")) {
        return listOf("Solo", "Duo", "Squad")
    }
    if (typeLower.contains("duo") || typeLower.contains("2v2")) {
        return listOf("Solo", "Duo")
    }
    if (typeLower.contains("1v1") || titleWithoutCS.contains("1v1")) {
        return listOf("Solo")
    }
    if (catWithoutCS.contains("squad") || catIdLower.contains("squad") ||
        titleWithoutCS.contains("squad") || titleWithoutCS.contains("4v4")
    ) {
        return listOf("Solo", "Duo", "Squad")
    }
    if (catWithoutCS.contains("duo") || catIdLower.contains("duo") ||
        titleWithoutCS.contains("duo") || titleWithoutCS.contains("2v2")
    ) {
        return listOf("Solo", "Duo")
    }
    return listOf("Solo")
}

@Composable
fun JoinMatchScreen(
    match: MatchEntity?,
    categoryName: String = "",
    onBack: () -> Unit,
    onConfirmJoin: (MatchEntity, List<String>) -> Unit
) {
    if (match == null) return
    val focusManager = LocalFocusManager.current

    val availableModes = remember(match.dbKey, match.type, match.title, categoryName) {
        resolveAvailableTeamModes(match, categoryName)
    }
    var selectedMode by remember(match.dbKey) {
        mutableStateOf(availableModes.first())
    }
    val requiredSlots = when (selectedMode) {
        "Squad" -> 4
        "Duo" -> 2
        else -> 1
    }
    val totalEntryFee = match.entry * requiredSlots

    val playerNames = remember(match.dbKey) {
        mutableStateListOf("", "", "", "")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .imePadding()
            .testTag("join_match_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 96.dp)
        ) {
            SubPageTopBar(
                title = "Join Match",
                darkMode = true,
                onBack = {
                    focusManager.clearFocus()
                    onBack()
                }
            )

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, CardBorderDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = match.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = formatMatchScheduleTime(match),
                        fontSize = 13.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Prize Pool & Entry Fee Cards side-by-side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SurfaceElevatedDark,
                            border = BorderStroke(1.dp, CardBorderDark),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)
                            ) {
                                Text(
                                    text = "Prize Pool",
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Win Prize: ${match.totalPrize}TK",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF18D2A6)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SurfaceElevatedDark,
                            border = BorderStroke(1.dp, CardBorderDark),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)
                            ) {
                                Text(
                                    text = "Entry Fee",
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Entry Fee: ${totalEntryFee}TK",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EsportsGold,
                                    modifier = Modifier.testTag("join_entry_fee_text")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Warning Box
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SurfaceElevatedDark,
                        border = BorderStroke(1.dp, CardBorderDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "*অবশ্যই এখানে আপনার গেমের এর নামটি দিয়ে জয়েন করবেন।",
                                color = EsportsGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = 19.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Mode Selection Row: Solo / Duo / Squad
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        availableModes.forEach { mode ->
                            val isSelected = selectedMode == mode
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFF18D2A6) else SurfaceElevatedDark,
                                border = if (isSelected) null else BorderStroke(1.dp, CardBorderDark),
                                modifier = Modifier
                                    .clickable { selectedMode = mode }
                                    .testTag("join_mode_${mode.lowercase()}")
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 9.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = mode,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Dynamic Player Name Inputs (1 for Solo, 2 for Duo, 4 for Squad)
                    for (idx in 0 until requiredSlots) {
                        OutlinedTextField(
                            value = playerNames[idx],
                            onValueChange = { playerNames[idx] = it },
                            placeholder = {
                                Text(
                                    text = "Player ${idx + 1} Name",
                                    fontSize = 14.sp,
                                    color = TextMuted
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceElevatedDark,
                                unfocusedContainerColor = SurfaceElevatedDark,
                                focusedBorderColor = Color(0xFF18D2A6),
                                unfocusedBorderColor = CardBorderDark,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("join_player_input_${idx + 1}")
                        )
                    }
                }
            }
        }

        // Fixed Bottom Join Now! Button
        Button(
            onClick = {
                focusManager.clearFocus()
                onConfirmJoin(match, playerNames.take(requiredSlots))
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF5C518),
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .navigationBarsPadding()
                .height(52.dp)
                .testTag("confirm_join_now_button")
        ) {
            Text(
                text = "Join Now!",
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}
