package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.CategoryEntity
import com.example.data.local.LeaderboardPlayer
import com.example.data.local.MatchEntity
import com.example.data.local.resolveMatchInstructionsAndRules
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

@Composable
fun AllRulesScreen(
    categories: List<CategoryEntity>,
    matches: List<MatchEntity>,
    onBack: () -> Unit
) {
    var selectedCategoryId by remember {
        mutableStateOf(categories.firstOrNull()?.id ?: "")
    }

    LaunchedEffect(categories) {
        if (selectedCategoryId.isBlank() && categories.isNotEmpty()) {
            selectedCategoryId = categories.first().id
        } else if (categories.isNotEmpty() && categories.none { it.id == selectedCategoryId }) {
            selectedCategoryId = categories.first().id
        }
    }

    val selectedCategory = categories.find { it.id == selectedCategoryId } ?: categories.firstOrNull()
    val categoryMatches = remember(matches, selectedCategory) {
        if (selectedCategory == null) emptyList()
        else matches.filter { it.categoryId == selectedCategory.id }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .navigationBarsPadding()
            .testTag("all_rules_screen")
    ) {
        SubPageTopBar(title = "All Rules", onBack = onBack)

        if (categories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No categories available.",
                    color = TextMuted,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            return@Column
        }

        // Category Selector Bar (linked with all Categories)
        Surface(
            color = CardDark,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Select Category to View Match Instructions and Rules",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 6.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = cat.id == selectedCategory?.id
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) EsportsOrange else SurfaceElevatedDark,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) EsportsOrange else CardBorderDark
                            ),
                            modifier = Modifier
                                .clickable { selectedCategoryId = cat.id }
                                .testTag("rules_category_tab_${cat.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SportsEsports,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.Black else EsportsOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = cat.name.uppercase(),
                                    color = if (isSelected) Color.Black else TextWhite,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
                HorizontalDivider(color = CardBorderDark)
            }
        }

        // Match Instructions and Rules Content for Selected Category
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            selectedCategory?.let { cat ->
                // Category Header Banner Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    border = BorderStroke(1.dp, CardBorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(EsportsOrange.copy(alpha = 0.15f))
                                .border(1.dp, EsportsOrange, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = null,
                                tint = EsportsOrange,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cat.name.uppercase(),
                                color = EsportsGold,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Match Instructions and Rules (${categoryMatches.size} Matches)",
                                color = TextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Group matches by their resolved instructions & rules or show per-match rules
                if (categoryMatches.isEmpty()) {
                    val defaultRules = "• অবশ্যই আপনার সঠিক গেম আইডি (Game ID Name) দিয়ে জয়েন করবেন।\n• ম্যাচ শুরু হওয়ার ১০ মিনিট আগে Room ID এবং Password দেওয়া হবে।\n• যেকোনো ধরনের হ্যাক, প্যানেল বা টিমিং করলে একাউন্ট সাসপেন্ড করা হবে।"
                    RuleSectionCard(
                        headerTitle = "Match Instructions and Rules",
                        subHeader = "${cat.name} Official Rules",
                        rulesText = defaultRules
                    )
                } else {
                    val distinctRulesGroups = categoryMatches.groupBy { resolveMatchInstructionsAndRules(it).trim() }
                    if (distinctRulesGroups.size == 1) {
                        val rulesText = distinctRulesGroups.keys.first()
                        RuleSectionCard(
                            headerTitle = "Match Instructions and Rules",
                            subHeader = "${cat.name} - All Matches",
                            rulesText = rulesText
                        )
                    } else {
                        categoryMatches.forEach { m ->
                            RuleSectionCard(
                                headerTitle = "Match Instructions and Rules",
                                subHeader = "${m.title} (${m.type} • ${m.map})",
                                rulesText = resolveMatchInstructionsAndRules(m)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RuleSectionCard(
    headerTitle: String,
    subHeader: String,
    rulesText: String
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, CardBorderDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = headerTitle,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = TextWhite
            )
            if (subHeader.isNotBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subHeader,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = EsportsOrange
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(EsportsOrange)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = rulesText,
                color = TextWhite.copy(alpha = 0.92f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 21.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun TopPlayersScreen(
    players: List<LeaderboardPlayer>,
    onBack: () -> Unit
) {
    var selectedPeriod by remember { mutableStateOf("Daily") } // "Daily", "Weekly", "Monthly"

    val rankedPlayers = remember(players, selectedPeriod) {
        when (selectedPeriod) {
            "Daily" -> players.sortedWith(
                compareByDescending<LeaderboardPlayer> { it.dailyWon }
                    .thenByDescending { it.dailyKills }
                    .thenByDescending { it.wonAmount }
            )
            "Weekly" -> players.sortedWith(
                compareByDescending<LeaderboardPlayer> { it.weeklyWon }
                    .thenByDescending { it.weeklyKills }
                    .thenByDescending { it.wonAmount }
            )
            else -> players.sortedWith(
                compareByDescending<LeaderboardPlayer> { it.wonAmount }
                    .thenByDescending { it.kills }
                    .thenByDescending { it.matchesPlayed }
            )
        }
    }

    val top1 = rankedPlayers.getOrNull(0)
    val top2 = rankedPlayers.getOrNull(1)
    val top3 = rankedPlayers.getOrNull(2)
    val restPlayers = if (rankedPlayers.size > 3) rankedPlayers.drop(3) else emptyList()

    val leaderboardBg = Color(0xFF0B0E1A)
    val cardSurface = Color(0xFF15192B)
    val cardBorder = Color(0xFF232A42)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(leaderboardBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("top_players_screen")
    ) {
        // Top Header Bar matching Screenshot 3 ("<   Leaderboard")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .testTag("leaderboard_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(
                text = "Leaderboard",
                color = Color.White,
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Daily / Weekly / Monthly Segmented Filter Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF101424))
                .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                .padding(5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Daily", "Weekly", "Monthly").forEach { tab ->
                    val isSelected = selectedPeriod == tab
                    val tabModifier = if (isSelected) {
                        Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF00D2FF), Color(0xFF7B5BFF))
                                )
                            )
                            .clickable { selectedPeriod = tab }
                            .testTag("leaderboard_tab_${tab.lowercase()}")
                    } else {
                        Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(Color.Transparent)
                            .border(1.dp, Color(0xFF222940), RoundedCornerShape(9.dp))
                            .clickable { selectedPeriod = tab }
                            .testTag("leaderboard_tab_${tab.lowercase()}")
                    }
                    Box(
                        modifier = tabModifier,
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            color = if (isSelected) Color.White else Color(0xFFB0B8D1),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top 3 Podium Cards (#2 Left, #1 Center Taller, #3 Right)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    PodiumPlayerCard(
                        rank = 2,
                        player = top2,
                        period = selectedPeriod,
                        avatarSize = 60.dp,
                        cardHeight = 195.dp,
                        cardSurface = cardSurface,
                        cardBorder = cardBorder,
                        modifier = Modifier.weight(1f)
                    )
                    PodiumPlayerCard(
                        rank = 1,
                        player = top1,
                        period = selectedPeriod,
                        avatarSize = 68.dp,
                        cardHeight = 216.dp,
                        cardSurface = cardSurface,
                        cardBorder = cardBorder,
                        modifier = Modifier.weight(1.08f)
                    )
                    PodiumPlayerCard(
                        rank = 3,
                        player = top3,
                        period = selectedPeriod,
                        avatarSize = 60.dp,
                        cardHeight = 195.dp,
                        cardSurface = cardSurface,
                        cardBorder = cardBorder,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Table Header Row ("Rank", "Player", "Won")
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rank",
                        color = Color(0xFF9BA3BE),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(68.dp)
                    )
                    Text(
                        text = "Player",
                        color = Color(0xFF9BA3BE),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Won",
                        color = Color(0xFF9BA3BE),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    )
                }
            }

            // Ranked List (4, 5, 6, 7, 8, 9...)
            itemsIndexed(restPlayers, key = { _, item -> item.id }) { index, player ->
                val rankNumber = index + 4
                val wonVal = player.wonForPeriod(selectedPeriod)
                val killsVal = player.killsForPeriod(selectedPeriod)
                val matchesVal = player.matchesForPeriod(selectedPeriod)

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = cardSurface),
                    border = BorderStroke(1.dp, cardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("leaderboard_row_$rankNumber")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$rankNumber",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.width(32.dp),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        LeaderboardAvatar(
                            name = player.name,
                            avatarUrl = player.avatarUrl,
                            rank = rankNumber,
                            size = 46.dp,
                            ringColor = Color(0xFF394468)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = player.name,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "$killsVal kills | $matchesVal matches",
                                color = Color(0xFF8C95B2),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "$wonVal TK",
                            color = Color(0xFFFFC107),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PodiumPlayerCard(
    rank: Int,
    player: LeaderboardPlayer?,
    period: String,
    avatarSize: Dp,
    cardHeight: Dp,
    cardSurface: Color,
    cardBorder: Color,
    modifier: Modifier = Modifier
) {
    val name = player?.name ?: "---"
    val won = player?.wonForPeriod(period) ?: 0
    val kills = player?.killsForPeriod(period) ?: 0

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurface),
        border = BorderStroke(1.dp, cardBorder),
        modifier = modifier.height(cardHeight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "#$rank",
                color = Color(0xFFFFB300),
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LeaderboardAvatar(
                name = name,
                avatarUrl = player?.avatarUrl.orEmpty(),
                rank = rank,
                size = avatarSize,
                ringColor = Color(0xFF00E676)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = name,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "$won TK",
                color = Color(0xFFFFB300),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$kills kills",
                color = Color(0xFF8C95B2),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun LeaderboardAvatar(
    name: String,
    avatarUrl: String,
    rank: Int,
    size: Dp,
    ringColor: Color
) {
    val avatarBgPalette = listOf(
        Color(0xFFFFB300),
        Color(0xFF1E293B),
        Color(0xFF7F1D1D),
        Color(0xFFFBBF24),
        Color(0xFFF8FAFC),
        Color(0xFF0F172A)
    )
    val bgColor = avatarBgPalette[rank % avatarBgPalette.size]

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor)
            .border(2.dp, ringColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (avatarUrl.isNotBlank()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else if (rank % 3 == 2 || rank == 4 || rank == 8) {
            // Illustrated yellow circle gamer silhouette avatar (matching screenshot #2, #4, #8)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = this.size.width
                val h = this.size.height
                drawCircle(color = Color(0xFFF5B800))
                // Hoodie shoulders
                drawCircle(
                    color = Color(0xFF4B5563),
                    radius = w * 0.36f,
                    center = Offset(w * 0.5f, h * 0.92f)
                )
                // Neck & Face
                drawCircle(
                    color = Color(0xFFFDE68A),
                    radius = w * 0.20f,
                    center = Offset(w * 0.5f, h * 0.44f)
                )
                // Hair
                drawCircle(
                    color = Color(0xFF78350F),
                    radius = w * 0.19f,
                    center = Offset(w * 0.5f, h * 0.34f)
                )
                drawCircle(
                    color = Color(0xFFFDE68A),
                    radius = w * 0.18f,
                    center = Offset(w * 0.5f, h * 0.46f)
                )
            }
        } else if (rank == 1 || rank == 3) {
            // Esports badge emblem avatar (matching screenshot #1 & #3)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            if (rank == 1) listOf(Color(0xFF3B2F14), Color(0xFF090D16))
                            else listOf(Color(0xFF5A1212), Color(0xFF090D16))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = if (rank == 1) EsportsGold else EsportsOrange,
                        modifier = Modifier.size(size * 0.42f)
                    )
                    Text(
                        text = name.take(5).uppercase(),
                        color = Color.White,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = name,
                tint = if (bgColor == Color(0xFFF8FAFC)) Color(0xFF0F172A) else EsportsOrange,
                modifier = Modifier.size(size * 0.55f)
            )
        }
    }
}

private fun LeaderboardPlayer.wonForPeriod(period: String): Int = when (period) {
    "Daily" -> dailyWon
    "Weekly" -> weeklyWon
    else -> wonAmount
}

private fun LeaderboardPlayer.killsForPeriod(period: String): Int = when (period) {
    "Daily" -> dailyKills
    "Weekly" -> weeklyKills
    else -> kills
}

private fun LeaderboardPlayer.matchesForPeriod(period: String): Int = when (period) {
    "Daily" -> dailyMatches
    "Weekly" -> weeklyMatches
    else -> matchesPlayed
}
