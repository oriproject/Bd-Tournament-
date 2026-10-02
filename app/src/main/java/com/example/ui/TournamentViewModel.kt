package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.TournamentRepository
import com.example.data.isSameDayAsToday
import com.example.data.local.AppSettingsData
import com.example.data.local.CategoryEntity
import com.example.data.local.LeaderboardPlayer
import com.example.data.local.MatchEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.ParticipantEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

enum class MainTab {
    HOME, MY_MATCHES, WALLET, RESULT, PROFILE
}

sealed class SubScreen {
    data object None : SubScreen()
    data class CategoryMatches(val categoryId: String) : SubScreen()
    data class MatchDetails(val matchKey: String) : SubScreen()
    data class ResultDetails(val matchKey: String) : SubScreen()
    data class JoinMatch(val matchKey: String, val returnToCategory: String?) : SubScreen()
    data object Wallet : SubScreen()
    data object AddMoney : SubScreen()
    data object Withdraw : SubScreen()
    data object History : SubScreen()
    data object Notifications : SubScreen()
    data object Refer : SubScreen()
    data object EditProfile : SubScreen()
    data object AllRules : SubScreen()
    data object TopPlayers : SubScreen()
}

enum class AlertType {
    SUCCESS, ERROR, WARNING, INFO
}

data class AlertMessage(
    val type: AlertType,
    val title: String,
    val message: String,
    val confirmText: String = "Okay",
    val onConfirm: (() -> Unit)? = null
)

class TournamentViewModel(
    private val repository: TournamentRepository
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = repository.categories
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val matches: StateFlow<List<MatchEntity>> = repository.matches
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val participants: StateFlow<List<ParticipantEntity>> = repository.participants
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.notifications
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUser: StateFlow<UserEntity?> = repository.activeUser
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val appSettings: StateFlow<AppSettingsData> = repository.appSettings

    val leaderboardPlayers: StateFlow<List<LeaderboardPlayer>> = repository.leaderboardPlayers

    @OptIn(ExperimentalCoroutinesApi::class)
    val userTransactions: StateFlow<List<TransactionEntity>> = currentUser
        .map { it?.uid }
        .distinctUntilChanged()
        .flatMapLatest { uid ->
            if (uid != null) repository.getTransactionsForUser(uid).distinctUntilChanged() else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showSplash = MutableStateFlow(true)
    val showSplash: StateFlow<Boolean> = _showSplash.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authMode = MutableStateFlow("login") // "login" or "register"
    val authMode: StateFlow<String> = _authMode.asStateFlow()

    private val _activeTab = MutableStateFlow(MainTab.HOME)
    val activeTab: StateFlow<MainTab> = _activeTab.asStateFlow()

    private val _subScreen = MutableStateFlow<SubScreen>(SubScreen.None)
    val subScreen: StateFlow<SubScreen> = _subScreen.asStateFlow()

    private val _matchFilterStatus = MutableStateFlow("Upcoming") // "Upcoming", "Ongoing", "Finished"
    val matchFilterStatus: StateFlow<String> = _matchFilterStatus.asStateFlow()

    private val _currentPayMethod = MutableStateFlow("bkash") // "bkash", "rocket", "nagad"
    val currentPayMethod: StateFlow<String> = _currentPayMethod.asStateFlow()

    private val _selectedWithdrawMethod = MutableStateFlow("bKash") // "bKash", "Nagad", "Rocket"
    val selectedWithdrawMethod: StateFlow<String> = _selectedWithdrawMethod.asStateFlow()

    private val _showStartupModal = MutableStateFlow(false)
    val showStartupModal: StateFlow<Boolean> = _showStartupModal.asStateFlow()

    private val _prizeModalMatch = MutableStateFlow<MatchEntity?>(null)
    val prizeModalMatch: StateFlow<MatchEntity?> = _prizeModalMatch.asStateFlow()

    private val _roomModalMatch = MutableStateFlow<MatchEntity?>(null)
    val roomModalMatch: StateFlow<MatchEntity?> = _roomModalMatch.asStateFlow()

    private val _showJoinWarningModal = MutableStateFlow(false)
    val showJoinWarningModal: StateFlow<Boolean> = _showJoinWarningModal.asStateFlow()

    private val _showLogoutDialog = MutableStateFlow(false)
    val showLogoutDialog: StateFlow<Boolean> = _showLogoutDialog.asStateFlow()

    private val _alertMessage = MutableStateFlow<AlertMessage?>(null)
    val alertMessage: StateFlow<AlertMessage?> = _alertMessage.asStateFlow()

    private val _currentTimeMillis = MutableStateFlow(System.currentTimeMillis())
    val currentTimeMillis: StateFlow<Long> = _currentTimeMillis.asStateFlow()

    init {
        viewModelScope.launch {
            val minSplashJob = async { delay(1200L) }
            withTimeoutOrNull(3200L) {
                try {
                    repository.initializeAndSync()
                } catch (_: Exception) {
                }
            }
            minSplashJob.await()
            _showSplash.value = false
            if (repository.appSettings.value.showPopup && repository.appSettings.value.popupText.isNotBlank()) {
                _showStartupModal.value = true
            }
        }
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                _currentTimeMillis.value = System.currentTimeMillis()
            }
        }
        // Live periodic sync with Firebase Realtime Database so Admin Panel updates reflect automatically
        viewModelScope.launch(Dispatchers.IO) {
            delay(2000L)
            while (true) {
                try {
                    repository.syncLiveFromFirebase()
                } catch (_: Exception) {
                }
                delay(2000L)
            }
        }
    }

    fun refreshFromFirebase() {
        triggerImmediateSync()
    }

    private fun triggerImmediateSync() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.syncLiveFromFirebase(forceWait = true)
            } catch (_: Exception) {
            }
        }
    }

    fun toggleAuthMode(mode: String) {
        _authMode.value = mode
    }

    fun selectTab(tab: MainTab) {
        _subScreen.value = SubScreen.None
        _activeTab.value = tab
        triggerImmediateSync()
    }

    fun navigateSub(screen: SubScreen) {
        _subScreen.value = screen
        triggerImmediateSync()
    }

    fun navigateBack() {
        triggerImmediateSync()
        when (val cur = _subScreen.value) {
            is SubScreen.AddMoney, is SubScreen.Withdraw, is SubScreen.History -> {
                if (_activeTab.value == MainTab.WALLET) {
                    _subScreen.value = SubScreen.None
                } else {
                    _subScreen.value = SubScreen.Wallet
                }
            }
            is SubScreen.Wallet, is SubScreen.Notifications, is SubScreen.Refer,
            is SubScreen.EditProfile, is SubScreen.AllRules, is SubScreen.TopPlayers -> {
                _subScreen.value = SubScreen.None
                _activeTab.value = MainTab.PROFILE
            }
            is SubScreen.CategoryMatches -> {
                _subScreen.value = SubScreen.None
                _activeTab.value = MainTab.HOME
            }
            is SubScreen.MatchDetails -> {
                _subScreen.value = SubScreen.None
            }
            is SubScreen.ResultDetails -> {
                _subScreen.value = SubScreen.None
                _activeTab.value = MainTab.RESULT
            }
            is SubScreen.JoinMatch -> {
                val catId = cur.returnToCategory
                if (catId != null) {
                    _subScreen.value = SubScreen.CategoryMatches(catId)
                } else {
                    _subScreen.value = SubScreen.None
                    _activeTab.value = MainTab.HOME
                }
            }
            SubScreen.None -> {
                if (_activeTab.value != MainTab.HOME) {
                    _activeTab.value = MainTab.HOME
                }
            }
        }
    }

    fun openCategory(categoryId: String) {
        _matchFilterStatus.value = "Upcoming"
        _subScreen.value = SubScreen.CategoryMatches(categoryId)
        triggerImmediateSync()
    }

    fun setMatchFilter(status: String) {
        _matchFilterStatus.value = status
    }

    fun setPayMethod(method: String) {
        _currentPayMethod.value = method
    }

    fun setWithdrawMethod(method: String) {
        _selectedWithdrawMethod.value = method
    }

    fun dismissStartupModal() {
        _showStartupModal.value = false
    }

    fun openPrizePool(match: MatchEntity) {
        _prizeModalMatch.value = match
    }

    fun dismissPrizePool() {
        _prizeModalMatch.value = null
    }

    fun checkRoomDetails(match: MatchEntity, isJoined: Boolean) {
        if (!isJoined) {
            _showJoinWarningModal.value = true
            return
        }
        val rId = match.roomId.trim()
        if (rId.isEmpty() || rId == "undefined" || rId.length < 2) {
            _alertMessage.value = AlertMessage(
                type = AlertType.INFO,
                title = "অপেক্ষা করুন",
                message = "ম্যাচ শুরু হওয়ার ১০ মিনিট আগে দেওয়া হবে",
                confirmText = "ঠিক আছে"
            )
        } else {
            _roomModalMatch.value = match
        }
    }

    fun dismissRoomModal() {
        _roomModalMatch.value = null
    }

    fun dismissJoinWarningModal() {
        _showJoinWarningModal.value = false
    }

    fun dismissAlert() {
        _alertMessage.value = null
    }

    fun prepJoinMatch(match: MatchEntity, isJoined: Boolean, currentCategoryId: String?) {
        if (isJoined) {
            _alertMessage.value = AlertMessage(
                type = AlertType.WARNING,
                title = "Already Joined",
                message = "You have already joined this match!"
            )
            return
        }
        _subScreen.value = SubScreen.JoinMatch(match.dbKey, currentCategoryId)
    }

    fun confirmJoinMatch(match: MatchEntity, playerIgns: List<String>) {
        val user = currentUser.value ?: return
        val cleaned = playerIgns.map { it.trim() }.filter { it.isNotEmpty() }
        if (cleaned.size != playerIgns.size) {
            _alertMessage.value = AlertMessage(
                type = AlertType.ERROR,
                title = "Error",
                message = "Fill all player Game ID names"
            )
            return
        }

        val alreadyJoined = participants.value.any { it.matchKey == match.dbKey && it.joinedBy == user.uid }
        if (alreadyJoined) {
            _alertMessage.value = AlertMessage(
                type = AlertType.WARNING,
                title = "Already Joined",
                message = "You have already joined this match!"
            )
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.joinMatch(user, match, cleaned)
            _isLoading.value = false
            if (res.isSuccess) {
                _alertMessage.value = AlertMessage(
                    type = AlertType.SUCCESS,
                    title = "Joined!",
                    message = "You have successfully joined the match.",
                    onConfirm = { navigateBack() }
                )
            } else {
                _alertMessage.value = AlertMessage(
                    type = AlertType.ERROR,
                    title = "Error",
                    message = res.exceptionOrNull()?.message ?: "Could not join match"
                )
            }
        }
    }

    fun loginWithEmail(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _alertMessage.value = AlertMessage(AlertType.ERROR, "Error", "Please fill all fields")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.loginWithEmail(email, pass)
            _isLoading.value = false
            if (res.isFailure) {
                _alertMessage.value = AlertMessage(
                    type = AlertType.ERROR,
                    title = "Login Error",
                    message = res.exceptionOrNull()?.message ?: "Invalid credentials"
                )
            } else {
                _activeTab.value = MainTab.HOME
                _subScreen.value = SubScreen.None
            }
        }
    }

    fun registerWithEmail(username: String, email: String, phone: String, pass: String, promoCode: String = "") {
        if (username.isBlank() || email.isBlank() || phone.isBlank() || pass.isBlank()) {
            _alertMessage.value = AlertMessage(AlertType.ERROR, "Error", "Please fill all fields")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.registerWithEmail(username, email, phone, pass, promoCode)
            _isLoading.value = false
            if (res.isFailure) {
                _alertMessage.value = AlertMessage(
                    type = AlertType.ERROR,
                    title = "Registration Error",
                    message = res.exceptionOrNull()?.message ?: "Could not register account"
                )
            } else {
                _activeTab.value = MainTab.HOME
                _subScreen.value = SubScreen.None
            }
        }
    }

    fun loginWithGoogle(googleIdToken: String?, email: String, displayName: String, photoUrl: String = "") {
        if (email.isBlank()) {
            _alertMessage.value = AlertMessage(AlertType.ERROR, "Error", "Please select or enter a valid Google email")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.loginWithGoogleAccount(googleIdToken, email, displayName, photoUrl)
            _isLoading.value = false
            if (res.isSuccess) {
                _activeTab.value = MainTab.HOME
                _subScreen.value = SubScreen.None
            } else {
                _alertMessage.value = AlertMessage(
                    type = AlertType.ERROR,
                    title = "Google Sign-In Error",
                    message = res.exceptionOrNull()?.message ?: "Could not sign in with Google"
                )
            }
        }
    }

    fun requestLogout() {
        _showLogoutDialog.value = true
    }

    fun dismissLogoutDialog() {
        _showLogoutDialog.value = false
    }

    fun confirmLogout() {
        _showLogoutDialog.value = false
        viewModelScope.launch {
            repository.logout()
            _subScreen.value = SubScreen.None
            _activeTab.value = MainTab.HOME
        }
    }

    fun verifyAutoPay(trxId: String, amountStr: String = "", senderNumber: String = "") {
        if (_isLoading.value) return
        val user = currentUser.value ?: return
        val enteredAmount = amountStr.trim().toDoubleOrNull()
        if (enteredAmount == null || enteredAmount <= 0.0) {
            _alertMessage.value = AlertMessage(
                type = AlertType.WARNING,
                title = "টাকার পরিমাণ দিন",
                message = "অনুগ্রহ করে কত টাকা ডিপোজিট করেছেন তা লিখুন (Amount)।"
            )
            return
        }
        val cleanSender = senderNumber.trim()
        if (cleanSender.length < 11) {
            _alertMessage.value = AlertMessage(
                type = AlertType.WARNING,
                title = "একাউন্ট নাম্বার দিন",
                message = "অনুগ্রহ করে যে নাম্বার থেকে টাকা পাঠিয়েছেন সেই একাউন্ট নাম্বারটি লিখুন।"
            )
            return
        }
        if (trxId.isBlank()) {
            _alertMessage.value = AlertMessage(
                type = AlertType.WARNING,
                title = "ট্রানজেকশন আইডি দিন",
                message = "অনুগ্রহ করে আপনার Transaction ID (TrxID) লিখুন।"
            )
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.verifyAndAddMoney(user, _currentPayMethod.value, trxId, enteredAmount, cleanSender)
            _isLoading.value = false
            if (res.isSuccess) {
                val added = res.getOrThrow()
                _alertMessage.value = AlertMessage(
                    type = AlertType.INFO,
                    title = "Pending Submitted!",
                    message = "আপনার ৳${added.toInt()} ডিপোজিট রিকোয়েস্ট (নাম্বার: $cleanSender, TrxID: ${trxId.trim().uppercase()}) সফলভাবে সাবমিট হয়েছে। এডমিন যাচাই করে Approve করলেই আপনার একাউন্টে টাকা যোগ হয়ে যাবে।",
                    onConfirm = { navigateBack() }
                )
            } else {
                _alertMessage.value = AlertMessage(
                    type = AlertType.ERROR,
                    title = "Error",
                    message = res.exceptionOrNull()?.message ?: "Could not submit deposit request."
                )
            }
        }
    }

    fun submitWithdraw(number: String, amountStr: String) {
        if (_isLoading.value) return
        val user = currentUser.value ?: return
        val alreadyWithdrawnToday = userTransactions.value.any { tx ->
            tx.uid == user.uid &&
                tx.type.contains("Withdraw", ignoreCase = true) &&
                !tx.status.contains("Reject", ignoreCase = true) &&
                isSameDayAsToday(tx.date)
        }
        if (alreadyWithdrawnToday) {
            _alertMessage.value = AlertMessage(
                type = AlertType.WARNING,
                title = "উইথড্র লিমিট শেষ!",
                message = "আপনি দিনে সর্বোচ্চ ১ বার উইথড্র করতে পারবেন! আপনার আজকের উইথড্র লিমিট (১ / ১ বার) শেষ হয়েছে, অনুগ্রহ করে আগামীকাল চেষ্টা করুন।"
            )
            return
        }
        val amount = amountStr.trim().toDoubleOrNull()
        if (number.isBlank() || amount == null || amount <= 0.0) {
            _alertMessage.value = AlertMessage(
                type = AlertType.WARNING,
                title = "সব তথ্য দিন",
                message = "অনুগ্রহ করে মোবাইল নাম্বার এবং উইথড্র করার টাকার পরিমাণ লিখুন।"
            )
            return
        }
        if (amount > user.winning) {
            _alertMessage.value = AlertMessage(
                type = AlertType.ERROR,
                title = "উইথড্র করা যাবে না!",
                message = "ডিপোজিট করা টাকা উইথড্র করা যাবে না! শুধুমাত্র ম্যাচ জিতে পাওয়া Winning টাকা উইথড্র করতে পারবেন। (আপনার বর্তমান Winning Balance: ৳${user.winning.toInt().coerceAtLeast(0)})"
            )
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val res = repository.submitWithdrawRequest(user, _selectedWithdrawMethod.value, number.trim(), amount)
            _isLoading.value = false
            if (res.isSuccess) {
                _alertMessage.value = AlertMessage(
                    type = AlertType.INFO,
                    title = "Pending Submitted!",
                    message = "আপনার ৳${amount.toInt()} উইথড্র রিকোয়েস্ট (${_selectedWithdrawMethod.value}: ${number.trim()}) সফলভাবে Pending হিসেবে সাবমিট হয়েছে। এডমিন ভেরিফাই করে Approve করলেই আপনার নাম্বারে টাকা পাঠিয়ে দেওয়া হবে।",
                    onConfirm = { navigateBack() }
                )
            } else {
                _alertMessage.value = AlertMessage(
                    type = AlertType.ERROR,
                    title = "Withdraw Error",
                    message = res.exceptionOrNull()?.message ?: "Could not submit withdraw"
                )
            }
        }
    }

    fun saveProfile(username: String, phone: String, curPass: String, newPass: String, conPass: String) {
        val user = currentUser.value ?: return
        if (newPass.isNotBlank()) {
            if (newPass != conPass) {
                _alertMessage.value = AlertMessage(AlertType.ERROR, "Error", "New passwords do not match")
                return
            }
            if (curPass.isBlank()) {
                _alertMessage.value = AlertMessage(AlertType.ERROR, "Error", "Current password required")
                return
            }
        }
        viewModelScope.launch {
            _isLoading.value = true
            repository.updateProfileDetails(user, username.trim(), phone.trim(), newPass.trim())
            _isLoading.value = false
            _alertMessage.value = AlertMessage(AlertType.SUCCESS, "Success", "Profile Updated")
        }
    }

    fun showCopiedToast() {
        _alertMessage.value = AlertMessage(
            type = AlertType.SUCCESS,
            title = "Copied!",
            message = "Copied to clipboard"
        )
    }

    companion object {
        fun provideFactory(repository: TournamentRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TournamentViewModel(repository) as T
                }
            }
    }
}
