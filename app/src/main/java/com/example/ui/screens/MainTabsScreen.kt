package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.AppSettingsData
import com.example.data.local.CategoryEntity
import com.example.data.local.MatchEntity
import com.example.data.local.UserEntity
import com.example.ui.MainTab
import com.example.ui.SubScreen
import com.example.ui.components.MatchCardItem
import com.example.ui.theme.BgDark
import com.example.ui.theme.CardBorderDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EsportsGold
import com.example.ui.theme.EsportsGreen
import com.example.ui.theme.EsportsOrange
import com.example.ui.theme.NavBarDark
import com.example.ui.theme.SurfaceElevatedDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.delay

@Composable
fun MainTabsContainer(
    activeTab: MainTab,
    onSelectTab: (MainTab) -> Unit,
    appSettings: AppSettingsData,
    categories: List<CategoryEntity>,
    matches: List<MatchEntity>,
    joinedMatchKeys: Set<String>,
    currentUser: UserEntity?,
    notifCount: Int,
    currentTimeMillis: Long,
    onOpenCategory: (String) -> Unit,
    onOpenMatchDetails: (String) -> Unit,
    onOpenResultDetails: (String) -> Unit,
    onPrepJoin: (MatchEntity, Boolean) -> Unit,
    onCheckRoom: (MatchEntity, Boolean) -> Unit,
    onOpenPrizePool: (MatchEntity) -> Unit,
    onNavigateSub: (SubScreen) -> Unit,
    onLogoutClick: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 70.dp)
        ) {
            when (activeTab) {
                MainTab.HOME -> HomeTabView(
                    appSettings = appSettings,
                    categories = categories,
                    matches = matches,
                    currentUser = currentUser,
                    onOpenCategory = onOpenCategory,
                    onOpenWallet = { onSelectTab(MainTab.WALLET) },
                    onOpenNotifications = { onNavigateSub(SubScreen.Notifications) }
                )
                MainTab.MY_MATCHES -> MyMatchesTabView(
                    matches = matches,
                    categories = categories,
                    joinedMatchKeys = joinedMatchKeys,
                    currentTimeMillis = currentTimeMillis,
                    onOpenMatchDetails = onOpenMatchDetails,
                    onCheckRoom = onCheckRoom,
                    onOpenPrizePool = onOpenPrizePool
                )
                MainTab.WALLET -> WalletScreen(
                    user = currentUser,
                    appSettings = appSettings,
                    onBack = { onSelectTab(MainTab.HOME) },
                    onNavigateSub = onNavigateSub
                )
                MainTab.RESULT -> ResultsTabView(
                    matches = matches,
                    categories = categories,
                    currentTimeMillis = currentTimeMillis,
                    onOpenResultDetails = onOpenResultDetails,
                    onCheckRoom = onCheckRoom
                )
                MainTab.PROFILE -> ProfileTabView(
                    user = currentUser,
                    notifCount = notifCount,
                    onNavigateSub = onNavigateSub,
                    onLogoutClick = onLogoutClick
                )
            }
        }

        // Floating Support Button (Orange circle with black headset icon as in screenshot)
        Surface(
            shape = CircleShape,
            color = EsportsOrange,
            shadowElevation = 8.dp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 86.dp)
                .navigationBarsPadding()
                .size(54.dp)
                .clickable {
                    openExternalUrl(context, appSettings.supportLink)
                }
                .testTag("fab_support_button")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.SupportAgent,
                    contentDescription = "Support",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Bottom Navigation Bar
        CustomBottomTabBar(
            activeTab = activeTab,
            onSelectTab = onSelectTab,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeTabView(
    appSettings: AppSettingsData,
    categories: List<CategoryEntity>,
    matches: List<MatchEntity>,
    currentUser: UserEntity?,
    onOpenCategory: (String) -> Unit,
    onOpenWallet: () -> Unit,
    onOpenNotifications: () -> Unit
) {
    val context = LocalContext.current
    val banners = appSettings.banners.ifEmpty {
        listOf(
            com.example.data.local.BannerItem("local_hero", appSettings.supportLink),
            com.example.data.local.BannerItem("local_shop", appSettings.shopLink)
        )
    }
    val pagerState = rememberPagerState(pageCount = { banners.size })

    val totalBalance = ((currentUser?.deposit ?: 0.0) + (currentUser?.winning ?: 0.0)).toInt()
    val liveAndUpcomingCount = matches.count { it.status != "Finished" }

    LaunchedEffect(banners.size) {
        if (banners.size > 1) {
            while (true) {
                delay(3000L)
                val next = (pagerState.currentPage + 1) % banners.size
                pagerState.animateScrollToPage(next)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .testTag("home_tab_view")
    ) {
        // Top Header Bar (matching screenshot: Left Logo + "BD TOURNAMENT" / "Tournaments & Esports", Right Wallet Pill + Notification Bell)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .testTag("home_top_app_bar"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_app_icon),
                    contentDescription = "Bd Tournament Logo",
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, CardBorderDark, CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Bd Tournament",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        color = EsportsOrange,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Tournaments & Esports",
                        fontSize = 12.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Wallet Balance Pill
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = CardDark,
                    border = BorderStroke(1.dp, CardBorderDark),
                    modifier = Modifier
                        .clickable(onClick = onOpenWallet)
                        .testTag("home_header_wallet_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Wallet",
                            tint = EsportsGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "৳$totalBalance",
                            color = TextWhite,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }

                // Notification Bell Button
                Surface(
                    shape = CircleShape,
                    color = CardDark,
                    border = BorderStroke(1.dp, CardBorderDark),
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(onClick = onOpenNotifications)
                        .testTag("home_header_notif_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = EsportsGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Rounded Hero Banner Slider
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, CardBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val banner = banners[page]
                        val mod = Modifier
                            .fillMaxSize()
                            .clickable {
                                if (banner.link.isNotBlank()) {
                                    openExternalUrl(context, banner.link)
                                }
                            }
                        if (banner.img == "local_hero") {
                            Image(
                                painter = painterResource(id = R.drawable.img_hero_tournament),
                                contentDescription = "Tournament Banner",
                                modifier = mod,
                                contentScale = ContentScale.Crop
                            )
                        } else if (banner.img == "local_shop") {
                            Image(
                                painter = painterResource(id = R.drawable.img_category_br),
                                contentDescription = "Esports Banner",
                                modifier = mod,
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            AsyncImage(
                                model = banner.img,
                                contentDescription = "Banner",
                                placeholder = painterResource(id = R.drawable.img_hero_tournament),
                                error = painterResource(id = R.drawable.img_hero_tournament),
                                modifier = mod,
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // Bottom Gradient Overlay with Title & Subtitle
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.85f)
                                    )
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Column {
                            Text(
                                text = "FREE FIRE PRO RUSH",
                                color = EsportsGold,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Daily Solo & Squad Matches",
                                color = TextWhite.copy(alpha = 0.9f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Slider Dots
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(banners.size) { i ->
                    val isSelected = pagerState.currentPage == i
                    val dotWidth by animateDpAsState(
                        targetValue = if (isSelected) 22.dp else 8.dp,
                        label = "dotWidth"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .width(dotWidth)
                            .height(8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) EsportsOrange else Color(0xFF333D4F))
                    )
                }
            }
        }

        // Home Content Wrapper
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // Rounded Dark Marquee Notice Container
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CardDark,
                border = BorderStroke(1.dp, CardBorderDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "Notice",
                        tint = EsportsOrange,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "🔥 ${appSettings.notice}",
                        color = EsportsGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        modifier = Modifier
                            .weight(1f)
                            .basicMarquee(iterations = Int.MAX_VALUE)
                            .testTag("dynamic_notice_marquee")
                    )
                }
            }

            // Section Title Row: SELECT TOURNAMENT | X Live & Upcoming
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SELECT TOURNAMENT",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = TextWhite,
                    letterSpacing = 0.4.sp
                )
                Text(
                    text = "$liveAndUpcomingCount Live & Upcoming",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = EsportsOrange
                )
            }

            // 2-Column Category Grid
            val chunked = categories.chunked(2)
            chunked.forEach { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    rowItems.forEachIndexed { idx, cat ->
                        val count = matches.count { it.categoryId == cat.id && it.status != "Finished" }
                        CategoryCardItem(
                            category = cat,
                            matchCount = count,
                            useAltArtwork = idx % 2 == 1,
                            onClick = { onOpenCategory(cat.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun CategoryCardItem(
    category: CategoryEntity,
    matchCount: Int,
    useAltArtwork: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subtitle = when {
        category.name.contains("BR", ignoreCase = true) -> "Bermuda Championship"
        category.name.contains("CLASH", ignoreCase = true) || category.name.contains("CS", ignoreCase = true) -> "Custom Room Arena"
        category.name.contains("LONE", ignoreCase = true) -> "1v1 Grand Arena"
        category.name.contains("FREE", ignoreCase = true) -> "Daily Booyah Pass"
        else -> "Multiplayer & BR"
    }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("category_card_${category.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, CardBorderDark)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
            ) {
                val fallbackRes = if (useAltArtwork) R.drawable.img_hero_tournament else R.drawable.img_category_br
                if (category.img.isBlank() || category.img.startsWith("local_")) {
                    Image(
                        painter = painterResource(id = fallbackRes),
                        contentDescription = category.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    AsyncImage(
                        model = category.img,
                        contentDescription = category.name,
                        placeholder = painterResource(id = fallbackRes),
                        error = painterResource(id = fallbackRes),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Top-Right Orange Match Count Badge ("X MATCHES")
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = EsportsOrange,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "$matchCount MATCHES",
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardDark)
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Text(
                    text = category.name.uppercase(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = TextWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MyMatchesTabView(
    matches: List<MatchEntity>,
    categories: List<CategoryEntity>,
    joinedMatchKeys: Set<String>,
    currentTimeMillis: Long,
    onOpenMatchDetails: (String) -> Unit,
    onCheckRoom: (MatchEntity, Boolean) -> Unit,
    onOpenPrizePool: (MatchEntity) -> Unit
) {
    val catMap = categories.associateBy { it.id }
    val joinedList = matches.filter { joinedMatchKeys.contains(it.dbKey) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .statusBarsPadding()
            .testTag("my_matches_tab_view")
    ) {
        Text(
            text = "MY MATCHES",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = TextWhite,
            modifier = Modifier.padding(16.dp)
        )
        if (joinedList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(36.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No joined matches yet",
                    color = TextMuted,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 15.dp, vertical = 4.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(joinedList, key = { it.dbKey }) { m ->
                    MatchCardItem(
                        match = m,
                        categoryImg = catMap[m.categoryId]?.img ?: "",
                        mode = "joined",
                        isJoined = true,
                        currentTimeMillis = currentTimeMillis,
                        onCardClick = { onOpenMatchDetails(m.dbKey) },
                        onJoinClick = {},
                        onRoomDetailsClick = { onCheckRoom(m, true) },
                        onPrizeDetailsClick = { onOpenPrizePool(m) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultsTabView(
    matches: List<MatchEntity>,
    categories: List<CategoryEntity>,
    currentTimeMillis: Long,
    onOpenResultDetails: (String) -> Unit,
    onCheckRoom: (MatchEntity, Boolean) -> Unit
) {
    val catMap = categories.associateBy { it.id }
    val finishedList = matches.filter { it.status == "Finished" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .statusBarsPadding()
            .testTag("results_tab_view")
    ) {
        Text(
            text = "MATCH RESULTS",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = TextWhite,
            modifier = Modifier.padding(16.dp)
        )
        if (finishedList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(36.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No results yet",
                    color = TextMuted,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 15.dp, vertical = 4.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(finishedList, key = { it.dbKey }) { m ->
                    MatchCardItem(
                        match = m,
                        categoryImg = catMap[m.categoryId]?.img ?: "",
                        mode = "result",
                        isJoined = true,
                        currentTimeMillis = currentTimeMillis,
                        onCardClick = { onOpenResultDetails(m.dbKey) },
                        onJoinClick = {},
                        onRoomDetailsClick = { onCheckRoom(m, true) },
                        onPrizeDetailsClick = {}
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileTabView(
    user: UserEntity?,
    notifCount: Int,
    onNavigateSub: (SubScreen) -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(rememberScrollState())
            .testTag("profile_tab_view")
    ) {
        // Dark Esports Profile Header Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1A2234), CardDark)
                    )
                )
                .statusBarsPadding()
                .padding(top = 28.dp, bottom = 26.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(SurfaceElevatedDark)
                    .border(2.5.dp, EsportsOrange, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Avatar",
                    tint = EsportsOrange,
                    modifier = Modifier.size(46.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = user?.username ?: "Guest User",
                color = TextWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = user?.email ?: "Please login to continue",
                color = TextMuted,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(36.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Deposit", color = TextMuted, fontSize = 12.sp)
                    Text(
                        text = "৳ ${user?.deposit?.toInt() ?: 0}",
                        color = EsportsGreen,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Winnings", color = TextMuted, fontSize = 12.sp)
                    Text(
                        text = "৳ ${user?.winning?.toInt() ?: 0}",
                        color = EsportsGold,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // Menu List
        Column(modifier = Modifier.padding(20.dp)) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, CardBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ProfileMenuItem(
                        icon = Icons.Default.AccountBalanceWallet,
                        title = "My Wallet",
                        testTag = "menu_my_wallet",
                        onClick = { onNavigateSub(SubScreen.Wallet) }
                    )
                    ProfileMenuItem(
                        icon = Icons.Default.Edit,
                        title = "Edit Profile",
                        testTag = "menu_edit_profile",
                        onClick = { onNavigateSub(SubScreen.EditProfile) }
                    )
                    ProfileMenuItem(
                        icon = Icons.Default.GroupAdd,
                        title = "Refer & Earn",
                        testTag = "menu_refer_earn",
                        onClick = { onNavigateSub(SubScreen.Refer) }
                    )
                    ProfileMenuItem(
                        icon = Icons.Default.History,
                        title = "All Transactions",
                        testTag = "menu_all_transactions",
                        onClick = { onNavigateSub(SubScreen.History) }
                    )
                    ProfileMenuItem(
                        icon = Icons.Default.Notifications,
                        title = "Notification",
                        badgeCount = notifCount,
                        testTag = "menu_notifications",
                        onClick = { onNavigateSub(SubScreen.Notifications) }
                    )
                    ProfileMenuItem(
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        title = "Logout",
                        tint = DangerRed,
                        showDivider = false,
                        testTag = "menu_logout",
                        onClick = onLogoutClick
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "App Version 1.0.0",
                color = TextMuted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    tint: Color = TextWhite,
    badgeCount: Int = 0,
    showDivider: Boolean = true,
    testTag: String,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp)
                .testTag(testTag),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (tint == DangerRed) DangerRed else EsportsOrange,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(15.dp))
            Text(
                text = title,
                color = tint,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            if (badgeCount > 0) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = EsportsOrange
                ) {
                    Text(
                        text = badgeCount.toString(),
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            } else if (tint != DangerRed) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = TextMuted
                )
            }
        }
        if (showDivider) {
            HorizontalDivider(color = CardBorderDark)
        }
    }
}

@Composable
private fun CustomBottomTabBar(
    activeTab: MainTab,
    onSelectTab: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = NavBarDark,
        shadowElevation = 12.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(color = CardBorderDark)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabBarItem(
                    label = "Home",
                    icon = Icons.Default.Home,
                    selected = activeTab == MainTab.HOME,
                    testTag = "nav_home",
                    onClick = { onSelectTab(MainTab.HOME) }
                )
                TabBarItem(
                    label = "Matches",
                    icon = Icons.Default.MilitaryTech,
                    selected = activeTab == MainTab.MY_MATCHES,
                    testTag = "nav_mymatches",
                    onClick = { onSelectTab(MainTab.MY_MATCHES) }
                )
                TabBarItem(
                    label = "Wallet",
                    icon = Icons.Default.AccountBalanceWallet,
                    selected = activeTab == MainTab.WALLET,
                    testTag = "nav_wallet",
                    onClick = { onSelectTab(MainTab.WALLET) }
                )
                TabBarItem(
                    label = "Result",
                    icon = Icons.Default.EmojiEvents,
                    selected = activeTab == MainTab.RESULT,
                    testTag = "nav_result",
                    onClick = { onSelectTab(MainTab.RESULT) }
                )
                TabBarItem(
                    label = "Profile",
                    icon = Icons.Default.Person,
                    selected = activeTab == MainTab.PROFILE,
                    testTag = "nav_profile",
                    onClick = { onSelectTab(MainTab.PROFILE) }
                )
            }
        }
    }
}

@Composable
private fun TabBarItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .width(if (selected) 62.dp else 34.dp)
                .height(34.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(if (selected) EsportsOrange else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) Color.Black else TextMuted,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (selected) EsportsOrange else TextMuted
        )
    }
}

fun openExternalUrl(context: android.content.Context, url: String) {
    if (url.isBlank()) return
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (_: Exception) {
    }
}
