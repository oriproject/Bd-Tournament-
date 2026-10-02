package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.TournamentRepository
import com.example.data.local.AppDatabase
import com.example.ui.MainTab
import com.example.ui.SubScreen
import com.example.ui.TournamentViewModel
import com.example.ui.components.AllPopupsAndModals
import com.example.ui.components.SplashScreenView
import com.example.ui.screens.AddMoneyScreen
import com.example.ui.screens.AllRulesScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CategoryMatchesScreen
import com.example.ui.screens.EditProfileScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.JoinMatchScreen
import com.example.ui.screens.MainTabsContainer
import com.example.ui.screens.MatchDetailsScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.ReferScreen
import com.example.ui.screens.ResultDetailsScreen
import com.example.ui.screens.TopPlayersScreen
import com.example.ui.screens.WalletScreen
import com.example.ui.screens.WithdrawScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BdTournamentApp()
            }
        }
    }
}

@Composable
fun BdTournamentApp() {
    val context = LocalContext.current
    val repository = remember {
        val db = AppDatabase.getInstance(context)
        TournamentRepository(db.tournamentDao())
    }
    val viewModel: TournamentViewModel = viewModel(
        factory = TournamentViewModel.provideFactory(repository)
    )

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START || event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshFromFirebase()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val showSplash by viewModel.showSplash.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val authMode by viewModel.authMode.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val subScreen by viewModel.subScreen.collectAsStateWithLifecycle()
    val matchFilterStatus by viewModel.matchFilterStatus.collectAsStateWithLifecycle()
    val currentPayMethod by viewModel.currentPayMethod.collectAsStateWithLifecycle()
    val selectedWithdrawMethod by viewModel.selectedWithdrawMethod.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val matches by viewModel.matches.collectAsStateWithLifecycle()
    val participants by viewModel.participants.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val userTransactions by viewModel.userTransactions.collectAsStateWithLifecycle()
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
    val leaderboardPlayers by viewModel.leaderboardPlayers.collectAsStateWithLifecycle()

    val showStartupModal by viewModel.showStartupModal.collectAsStateWithLifecycle()
    val prizeModalMatch by viewModel.prizeModalMatch.collectAsStateWithLifecycle()
    val roomModalMatch by viewModel.roomModalMatch.collectAsStateWithLifecycle()
    val showJoinWarningModal by viewModel.showJoinWarningModal.collectAsStateWithLifecycle()
    val showLogoutDialog by viewModel.showLogoutDialog.collectAsStateWithLifecycle()
    val alertMessage by viewModel.alertMessage.collectAsStateWithLifecycle()

    val joinedMatchKeys = remember(participants, currentUser) {
        val uid = currentUser?.uid
        if (uid == null) emptySet()
        else participants.filter { it.joinedBy == uid }.map { it.matchKey }.toSet()
    }

    // BackHandler for secondary screens and non-home tabs
    BackHandler(enabled = subScreen != SubScreen.None || activeTab != MainTab.HOME) {
        viewModel.navigateBack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            showSplash -> {
                SplashScreenView(
                    appName = appSettings.appName,
                    appLogoUrl = appSettings.appLogo
                )
            }
            currentUser == null -> {
                AuthScreen(
                    authMode = authMode,
                    onToggleMode = viewModel::toggleAuthMode,
                    onLogin = viewModel::loginWithEmail,
                    onRegister = viewModel::registerWithEmail,
                    onGoogleLogin = viewModel::loginWithGoogle
                )
            }
            else -> {
                when (val curSub = subScreen) {
                    SubScreen.None -> {
                        val timerMillis = if (activeTab == MainTab.MY_MATCHES) {
                            viewModel.currentTimeMillis.collectAsStateWithLifecycle().value
                        } else {
                            0L
                        }
                        MainTabsContainer(
                            activeTab = activeTab,
                            onSelectTab = viewModel::selectTab,
                            appSettings = appSettings,
                            categories = categories,
                            matches = matches,
                            joinedMatchKeys = joinedMatchKeys,
                            currentUser = currentUser,
                            notifCount = notifications.size,
                            currentTimeMillis = timerMillis,
                            onOpenCategory = viewModel::openCategory,
                            onOpenMatchDetails = { key -> viewModel.navigateSub(SubScreen.MatchDetails(key)) },
                            onOpenResultDetails = { key -> viewModel.navigateSub(SubScreen.ResultDetails(key)) },
                            onPrepJoin = { m, isJoined -> viewModel.prepJoinMatch(m, isJoined, null) },
                            onCheckRoom = viewModel::checkRoomDetails,
                            onOpenPrizePool = viewModel::openPrizePool,
                            onNavigateSub = viewModel::navigateSub,
                            onLogoutClick = viewModel::requestLogout
                        )
                    }
                    is SubScreen.CategoryMatches -> {
                        val currentTimeMillis by viewModel.currentTimeMillis.collectAsStateWithLifecycle()
                        CategoryMatchesScreen(
                            categoryId = curSub.categoryId,
                            categories = categories,
                            matches = matches,
                            joinedMatchKeys = joinedMatchKeys,
                            filterStatus = matchFilterStatus,
                            currentTimeMillis = currentTimeMillis,
                            onFilterChange = viewModel::setMatchFilter,
                            onBack = viewModel::navigateBack,
                            onOpenMatchDetails = { key -> viewModel.navigateSub(SubScreen.MatchDetails(key)) },
                            onOpenResultDetails = { key -> viewModel.navigateSub(SubScreen.ResultDetails(key)) },
                            onPrepJoin = { m, isJoined ->
                                viewModel.prepJoinMatch(m, isJoined, curSub.categoryId)
                            },
                            onCheckRoom = viewModel::checkRoomDetails,
                            onOpenPrizePool = viewModel::openPrizePool
                        )
                    }
                    is SubScreen.MatchDetails -> {
                        val currentTimeMillis by viewModel.currentTimeMillis.collectAsStateWithLifecycle()
                        val m = matches.find { it.dbKey == curSub.matchKey }
                        MatchDetailsScreen(
                            match = m,
                            participants = participants,
                            currentTimeMillis = currentTimeMillis,
                            onBack = viewModel::navigateBack
                        )
                    }
                    is SubScreen.ResultDetails -> {
                        val m = matches.find { it.dbKey == curSub.matchKey }
                        ResultDetailsScreen(
                            match = m,
                            participants = participants,
                            onBack = viewModel::navigateBack
                        )
                    }
                    is SubScreen.JoinMatch -> {
                        val m = matches.find { it.dbKey == curSub.matchKey }
                        val catId = m?.categoryId?.ifBlank { curSub.returnToCategory.orEmpty() }
                            ?: curSub.returnToCategory.orEmpty()
                        val catName = categories.find { it.id == catId }?.name.orEmpty()
                        JoinMatchScreen(
                            match = m,
                            categoryName = catName,
                            onBack = viewModel::navigateBack,
                            onConfirmJoin = viewModel::confirmJoinMatch
                        )
                    }
                    SubScreen.Wallet -> {
                        WalletScreen(
                            user = currentUser,
                            appSettings = appSettings,
                            onBack = viewModel::navigateBack,
                            onNavigateSub = viewModel::navigateSub
                        )
                    }
                    SubScreen.AddMoney -> {
                        AddMoneyScreen(
                            currentMethod = currentPayMethod,
                            appSettings = appSettings,
                            onSelectMethod = viewModel::setPayMethod,
                            onVerify = viewModel::verifyAutoPay,
                            onCopySuccess = viewModel::showCopiedToast,
                            onBack = viewModel::navigateBack
                        )
                    }
                    SubScreen.Withdraw -> {
                        WithdrawScreen(
                            user = currentUser,
                            transactions = userTransactions,
                            selectedMethod = selectedWithdrawMethod,
                            onSelectMethod = viewModel::setWithdrawMethod,
                            onSubmitWithdraw = viewModel::submitWithdraw,
                            onBack = viewModel::navigateBack
                        )
                    }
                    SubScreen.History -> {
                        HistoryScreen(
                            transactions = userTransactions,
                            onBack = viewModel::navigateBack
                        )
                    }
                    SubScreen.Notifications -> {
                        NotificationsScreen(
                            notifications = notifications,
                            onBack = viewModel::navigateBack
                        )
                    }
                    SubScreen.Refer -> {
                        ReferScreen(
                            user = currentUser,
                            onCopySuccess = viewModel::showCopiedToast,
                            onBack = viewModel::navigateBack
                        )
                    }
                    SubScreen.EditProfile -> {
                        EditProfileScreen(
                            user = currentUser,
                            onSaveProfile = viewModel::saveProfile,
                            onBack = viewModel::navigateBack
                        )
                    }
                    SubScreen.AllRules -> {
                        AllRulesScreen(
                            categories = categories,
                            matches = matches,
                            onBack = viewModel::navigateBack
                        )
                    }
                    SubScreen.TopPlayers -> {
                        TopPlayersScreen(
                            players = leaderboardPlayers,
                            onBack = viewModel::navigateBack
                        )
                    }
                }
            }
        }

        // Global Modals & Loader
        if (!showSplash) {
            AllPopupsAndModals(
                showStartupModal = showStartupModal && currentUser != null,
                startupPopupText = appSettings.popupText,
                onDismissStartup = viewModel::dismissStartupModal,
                prizeModalMatch = prizeModalMatch,
                onDismissPrize = viewModel::dismissPrizePool,
                roomModalMatch = roomModalMatch,
                onDismissRoom = viewModel::dismissRoomModal,
                showJoinWarning = showJoinWarningModal,
                onDismissJoinWarning = viewModel::dismissJoinWarningModal,
                showLogoutDialog = showLogoutDialog,
                onConfirmLogout = viewModel::confirmLogout,
                onDismissLogout = viewModel::dismissLogoutDialog,
                alertMessage = alertMessage,
                onDismissAlert = viewModel::dismissAlert,
                onCopySuccess = viewModel::showCopiedToast,
                isLoading = isLoading
            )
        }
    }
}
