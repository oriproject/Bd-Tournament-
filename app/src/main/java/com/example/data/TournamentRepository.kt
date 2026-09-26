package com.example.data

import com.example.data.local.AppSettingsData
import com.example.data.local.BannerItem
import com.example.data.local.CategoryEntity
import com.example.data.local.MatchEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.ParticipantEntity
import com.example.data.local.TournamentDao
import com.example.data.local.TransactionEntity
import com.example.data.local.UserEntity
import com.example.data.remote.FirebaseRestClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TournamentRepository(
    private val dao: TournamentDao,
    private val remote: FirebaseRestClient = FirebaseRestClient()
) {
    val categories: Flow<List<CategoryEntity>> = dao.getAllCategories()
    val matches: Flow<List<MatchEntity>> = dao.getAllMatches()
    val participants: Flow<List<ParticipantEntity>> = dao.getAllParticipants()
    val notifications: Flow<List<NotificationEntity>> = dao.getAllNotifications()
    val activeUser: Flow<UserEntity?> = dao.getActiveUser()

    private val _appSettings = MutableStateFlow(
        AppSettingsData(
            banners = listOf(
                BannerItem(img = "local_hero", link = "https://t.me/DeveloperSketvia01"),
                BannerItem(img = "local_shop", link = "https://t.me/DeveloperSketvia01")
            )
        )
    )
    val appSettings: StateFlow<AppSettingsData> = _appSettings.asStateFlow()

    fun getTransactionsForUser(uid: String): Flow<List<TransactionEntity>> =
        dao.getTransactionsForUser(uid)

    suspend fun initializeAndSync() {
        // 1. Seed rich fallback tournament data locally if local DB is empty
        if (dao.getAllCategories().first().isEmpty()) {
            seedDefaultData()
        }

        // 2. If a user is already logged in locally, ensure they are registered & synced in Firebase Auth + RTDB
        val localUser = dao.getActiveUser().first()
        var activeToken: String? = null
        if (localUser != null) {
            val syncedUser = remote.ensureUserSyncedWithFirebase(localUser)
            if (syncedUser != localUser) {
                dao.clearUsers()
                dao.insertUser(syncedUser)
            }
            activeToken = syncedUser.idToken
            val remoteTxs = remote.fetchUserTransactions(syncedUser.uid, syncedUser.idToken)
            if (remoteTxs.isNotEmpty()) {
                dao.insertTransactions(remoteTxs)
            }
        }

        // 3. Seed Firebase Realtime Database (bd-tournament-3) if any root node is empty so Admin Panel has full structure
        remote.seedInitialDatabaseIfNeeded(
            defaultSettings = _appSettings.value,
            defaultCategories = dao.getAllCategories().first(),
            defaultMatches = dao.getAllMatches().first(),
            defaultParticipants = dao.getAllParticipants().first(),
            defaultNotifications = dao.getAllNotifications().first(),
            authToken = activeToken
        )

        // 4. Pull live data from Firebase Realtime Database
        syncLiveFromFirebase()
    }

    suspend fun syncLiveFromFirebase() {
        remote.fetchAppSettings()?.let { remoteSettings ->
            val mergedBanners = if (remoteSettings.banners.isNotEmpty()) {
                remoteSettings.banners
            } else {
                _appSettings.value.banners
            }
            _appSettings.value = remoteSettings.copy(banners = mergedBanners)
        }

        val remoteCats = remote.fetchCategories()
        if (remoteCats.isNotEmpty()) {
            dao.clearCategories()
            dao.insertCategories(remoteCats)
        }

        val remoteMatches = remote.fetchMatches()
        if (remoteMatches.isNotEmpty()) {
            dao.clearMatches()
            dao.insertMatches(remoteMatches)
        }

        val remoteParts = remote.fetchParticipants()
        if (remoteParts.isNotEmpty()) {
            dao.clearParticipants()
            dao.insertParticipants(remoteParts)
        }

        val remoteNotifs = remote.fetchNotifications()
        if (remoteNotifs.isNotEmpty()) {
            dao.insertNotifications(remoteNotifs)
        }

        // Sync active user's balance & transactions from Firebase (in case updated by Admin Panel)
        val curUser = dao.getActiveUser().first()
        if (curUser != null) {
            val remoteUser = remote.fetchRemoteUser(curUser.uid, curUser.idToken)
            if (remoteUser != null) {
                dao.insertUser(curUser.copy(
                    username = remoteUser.username.ifBlank { curUser.username },
                    phone = remoteUser.phone.ifBlank { curUser.phone },
                    deposit = remoteUser.deposit,
                    winning = remoteUser.winning
                ))
            }
            val remoteTxs = remote.fetchUserTransactions(curUser.uid, curUser.idToken)
            if (remoteTxs.isNotEmpty()) {
                dao.insertTransactions(remoteTxs)
            }
        }
    }

    private suspend fun seedDefaultData() {
        val now = System.currentTimeMillis()
        val defaultCategories = listOf(
            CategoryEntity("cat_br_full", "BR FULL MAP", "local_cat_br"),
            CategoryEntity("cat_cs_rank", "CLASH SQUAD 1V1 / 4V4", "local_cat_br"),
            CategoryEntity("cat_lone_wolf", "LONE WOLF 1V1", "local_cat_br"),
            CategoryEntity("cat_free_match", "DAILY FREE TOURNAMENT", "local_cat_br")
        )
        dao.insertCategories(defaultCategories)

        val defaultMatches = listOf(
            MatchEntity(
                dbKey = "match_br_101",
                categoryId = "cat_br_full",
                title = "BD Grand Survival #101",
                time = "Today, 08:30 PM",
                timestamp = now + 2 * 3600_000L + 15 * 60_000L,
                totalPrize = 520,
                type = "Solo",
                entry = 15,
                perKill = 8,
                map = "Bermuda",
                joined = 34,
                total = 48,
                status = "Upcoming",
                roomId = "",
                roomPass = "",
                prizeDesc = "👑 Winner - 150 Taka\n🥈 2nd Position - 90 Taka\n🥉 3rd Position - 50 Taka\n🏅 4th Position - 25 Taka\n🏅 5th Position - 15 Taka\n🔥 Per Kill : 8 Taka\n🏆 Total Prize Pool: 520 Taka"
            ),
            MatchEntity(
                dbKey = "match_br_102",
                categoryId = "cat_br_full",
                title = "Pro Squad Showdown #102",
                time = "Today, 10:00 PM",
                timestamp = now + 4 * 3600_000L,
                totalPrize = 1200,
                type = "Squad",
                entry = 30,
                perKill = 15,
                map = "Purgatory",
                joined = 40,
                total = 48,
                status = "Upcoming",
                roomId = "88492011",
                roomPass = "9922",
                prizeDesc = "👑 Winner Squad - 500 Taka\n🥈 2nd Squad - 300 Taka\n🥉 3rd Squad - 150 Taka\n🔥 Per Kill : 15 Taka"
            ),
            MatchEntity(
                dbKey = "match_br_live",
                categoryId = "cat_br_full",
                title = "Evening Rush Hour #99",
                time = "Today, 06:00 PM",
                timestamp = now - 900_000L,
                totalPrize = 450,
                type = "Duo",
                entry = 15,
                perKill = 7,
                map = "Bermuda",
                joined = 48,
                total = 48,
                status = "Ongoing",
                roomId = "77381920",
                roomPass = "1234",
                prizeDesc = "👑 Winner Duo - 180 Taka\n🥈 Runner Up - 100 Taka\n🔥 Per Kill : 7 Taka"
            ),
            MatchEntity(
                dbKey = "match_cs_201",
                categoryId = "cat_cs_rank",
                title = "CS Custom Headshot Only #201",
                time = "Today, 09:00 PM",
                timestamp = now + 2 * 3600_000L + 45 * 60_000L,
                totalPrize = 180,
                type = "Squad",
                entry = 25,
                perKill = 0,
                map = "Bermuda CS",
                joined = 4,
                total = 8,
                status = "Upcoming",
                roomId = "",
                roomPass = "",
                prizeDesc = "👑 Winning Squad - 180 Taka (Unlimited Gloowall, No Grenade)"
            ),
            MatchEntity(
                dbKey = "match_lw_301",
                categoryId = "cat_lone_wolf",
                title = "Lone Wolf 1v1 King #301",
                time = "Today, 08:00 PM",
                timestamp = now + 5400_000L,
                totalPrize = 90,
                type = "Solo",
                entry = 50,
                perKill = 0,
                map = "Iron Cage",
                joined = 1,
                total = 2,
                status = "Upcoming",
                roomId = "55410293",
                roomPass = "55",
                prizeDesc = "👑 Winner Takes All - 90 Taka"
            ),
            MatchEntity(
                dbKey = "match_free_401",
                categoryId = "cat_free_match",
                title = "Night Free Giveaway Match #401",
                time = "Tonight, 11:00 PM",
                timestamp = now + 5 * 3600_000L,
                totalPrize = 200,
                type = "Solo",
                entry = 0,
                perKill = 4,
                map = "Bermuda",
                joined = 42,
                total = 48,
                status = "Upcoming",
                roomId = "",
                roomPass = "",
                prizeDesc = "🎁 Free Entry Special Match!\n👑 Booyah - 60 Taka\n🔥 Per Kill - 4 Taka"
            ),
            MatchEntity(
                dbKey = "match_fin_90",
                categoryId = "cat_br_full",
                title = "Afternoon Booyah Cup #90",
                time = "Yesterday, 04:00 PM",
                timestamp = now - 86400_000L,
                totalPrize = 500,
                type = "Solo",
                entry = 15,
                perKill = 8,
                map = "Bermuda",
                joined = 48,
                total = 48,
                status = "Finished",
                roomId = "44129800",
                roomPass = "8811",
                prizeDesc = ""
            ),
            MatchEntity(
                dbKey = "match_fin_91",
                categoryId = "cat_cs_rank",
                title = "CS 4v4 Champions #91",
                time = "Yesterday, 09:30 PM",
                timestamp = now - 60000_000L,
                totalPrize = 360,
                type = "Squad",
                entry = 50,
                perKill = 0,
                map = "Bermuda",
                joined = 8,
                total = 8,
                status = "Finished",
                roomId = "19283746",
                roomPass = "4400",
                prizeDesc = ""
            )
        )
        dao.insertMatches(defaultMatches)

        val defaultParticipants = listOf(
            ParticipantEntity("p1", "match_br_101", "BD_Sniper99", "uid_other_1", 0, 0),
            ParticipantEntity("p2", "match_br_101", "SK_Rafi_FF", "uid_other_2", 0, 0),
            ParticipantEntity("p3", "match_br_101", "Toxic_Sakib", "uid_other_3", 0, 0),
            ParticipantEntity("p4", "match_br_101", "Crimson_King", "uid_other_4", 0, 0),
            ParticipantEntity("p5", "match_fin_90", "TX_Tanvir_YT", "uid_win_1", 9, 222),
            ParticipantEntity("p6", "match_fin_90", "BD_Hunter_07", "uid_win_2", 6, 138),
            ParticipantEntity("p7", "match_fin_90", "Mehedi_Boss", "uid_win_3", 5, 90),
            ParticipantEntity("p8", "match_fin_90", "Noyon_Gaming", "uid_win_4", 3, 24),
            ParticipantEntity("p9", "match_fin_90", "Rohan_FF_BD", "uid_win_5", 0, 0),
            ParticipantEntity("p10", "match_fin_91", "Team_Vipers_BD", "uid_win_6", 18, 360),
            ParticipantEntity("p11", "match_fin_91", "Dhaka_Warriors", "uid_win_7", 11, 0)
        )
        dao.insertParticipants(defaultParticipants)

        val defaultNotifs = listOf(
            NotificationEntity(
                "n1",
                "স্বাগতম Bd Tournament অ্যাপে! 🔥",
                "প্রতিদিন ফ্রি ফায়ার বিআর, সিএস এবং লোন উলফ ম্যাচ খেলে জিতে নিন নগদ টাকা। রুম আইডি ম্যাচ শুরুর ১০ মিনিট আগে দেওয়া হয়।",
                now - 3600_000L
            ),
            NotificationEntity(
                "n2",
                "অটোমেটিক অ্যাড মানি চালু হয়েছে ⚡",
                "বিকাশ, নগদ ও রকেটের মাধ্যমে সেন্ড মানি করে ট্রানজেকশন আইডি দিয়ে ভেরিফাই করলেই সাথে সাথে ব্যালেন্স যোগ হয়ে যাবে।",
                now - 1800_000L
            ),
            NotificationEntity(
                "n3",
                "রেফার করে আয় করুন 💰",
                "আপনার বন্ধুদের রেফার কোড দিয়ে আমন্ত্রণ জানান এবং প্রথম ১০০ টাকা ডিপোজিটে পান ১০ টাকা বোনাস!",
                now - 600_000L
            )
        )
        dao.insertNotifications(defaultNotifs)
    }

    suspend fun loginWithEmail(email: String, pass: String): Result<UserEntity> {
        val remoteRes = remote.signInEmail(email.trim(), pass)
        return if (remoteRes.isSuccess) {
            val user = remoteRes.getOrThrow()
            dao.clearUsers()
            dao.insertUser(user)
            val remoteTxs = remote.fetchUserTransactions(user.uid, user.idToken)
            if (remoteTxs.isNotEmpty()) dao.insertTransactions(remoteTxs)
            initializeAndSync()
            Result.success(user)
        } else {
            val errMsg = remoteRes.exceptionOrNull()?.message ?: "Authentication error"
            Result.failure(Exception(errMsg))
        }
    }

    suspend fun registerWithEmail(username: String, email: String, phone: String, pass: String): Result<UserEntity> {
        val remoteRes = remote.signUpEmail(username.trim(), email.trim(), phone.trim(), pass)
        return if (remoteRes.isSuccess) {
            val user = remoteRes.getOrThrow()
            dao.clearUsers()
            dao.insertUser(user)
            initializeAndSync()
            Result.success(user)
        } else {
            val ex = remoteRes.exceptionOrNull()
            val msg = ex?.message ?: "Registration failed"
            Result.failure(Exception(msg))
        }
    }

    suspend fun loginWithGoogleAccount(
        googleIdToken: String?,
        email: String,
        displayName: String,
        photoUrl: String = ""
    ): Result<UserEntity> {
        val res = remote.signInWithGoogleAccount(googleIdToken, email.trim(), displayName.trim(), photoUrl)
        return if (res.isSuccess) {
            val user = res.getOrThrow()
            dao.clearUsers()
            dao.insertUser(user)
            val remoteTxs = remote.fetchUserTransactions(user.uid, user.idToken)
            if (remoteTxs.isNotEmpty()) dao.insertTransactions(remoteTxs)
            initializeAndSync()
            Result.success(user)
        } else {
            val errMsg = res.exceptionOrNull()?.message ?: "Google Sign-In failed"
            Result.failure(Exception(errMsg))
        }
    }

    suspend fun logout() {
        dao.clearUsers()
    }

    suspend fun joinMatch(
        user: UserEntity,
        match: MatchEntity,
        playerIgns: List<String>
    ): Result<Unit> {
        val syncedUser = remote.ensureUserSyncedWithFirebase(user)
        if (syncedUser != user) {
            dao.clearUsers()
            dao.insertUser(syncedUser)
        }

        val count = playerIgns.size
        val totalCost = (match.entry * count).toDouble()
        if (syncedUser.deposit + syncedUser.winning < totalCost) {
            return Result.failure(Exception("Insufficient Balance! Required: ৳${totalCost.toInt()}"))
        }

        var d = syncedUser.deposit
        var w = syncedUser.winning
        var rem = totalCost
        if (d >= rem) {
            d -= rem
        } else {
            rem -= d
            d = 0.0
            w -= rem
        }

        val updatedUser = syncedUser.copy(deposit = d, winning = w)
        dao.insertUser(updatedUser)

        val newParticipants = playerIgns.mapIndexed { index, ign ->
            ParticipantEntity(
                id = "${match.dbKey}_${syncedUser.uid}_${System.currentTimeMillis()}_$index",
                matchKey = match.dbKey,
                ign = ign,
                joinedBy = syncedUser.uid,
                kills = 0,
                win = 0
            )
        }
        dao.insertParticipants(newParticipants)

        val newJoinedCount = match.joined + count
        dao.insertMatch(match.copy(joined = newJoinedCount))

        val df = SimpleDateFormat("M/d/yyyy, h:mm:ss a", Locale.getDefault())
        val txKey = "JOIN${System.currentTimeMillis().toString().takeLast(6)}"
        val tx = TransactionEntity(
            id = txKey,
            uid = syncedUser.uid,
            type = "Match Join (${match.title})",
            amount = totalCost,
            method = "Wallet",
            status = "Success",
            txId = txKey,
            date = df.format(Date())
        )
        dao.insertTransaction(tx)

        // Sync to Firebase RTDB
        remote.syncUserBalance(syncedUser.uid, d, w, syncedUser.idToken)
        remote.pushParticipantAndCount(match.dbKey, playerIgns, syncedUser.uid, newJoinedCount, syncedUser.idToken)
        remote.pushTransaction(updatedUser, tx)

        return Result.success(Unit)
    }

    suspend fun verifyAndAddMoney(
        user: UserEntity,
        method: String,
        trxId: String,
        enteredAmount: Double = 100.0
    ): Result<Double> {
        val syncedUser = remote.ensureUserSyncedWithFirebase(user)
        if (syncedUser != user) {
            dao.clearUsers()
            dao.insertUser(syncedUser)
        }

        val methodDisplay = when (method.lowercase()) {
            "bkash" -> "bKash"
            "nagad" -> "Nagad"
            "rocket" -> "Rocket"
            else -> method
        }

        val res = remote.verifyAutoPayTrx(trxId.trim(), enteredAmount, methodDisplay, syncedUser)
        return if (res.isSuccess) {
            val amount = res.getOrThrow()
            val newDeposit = syncedUser.deposit + amount
            val updatedUser = syncedUser.copy(deposit = newDeposit)
            dao.insertUser(updatedUser)

            val df = SimpleDateFormat("M/d/yyyy, h:mm:ss a", Locale.getDefault())
            val txKey = trxId.trim().uppercase()
            val tx = TransactionEntity(
                id = txKey,
                uid = syncedUser.uid,
                type = "Deposit ($methodDisplay)",
                amount = amount,
                method = methodDisplay,
                status = "Success",
                txId = txKey,
                date = df.format(Date())
            )
            dao.insertTransaction(tx)
            remote.syncUserBalance(syncedUser.uid, newDeposit, syncedUser.winning, syncedUser.idToken)
            remote.pushTransaction(updatedUser, tx)
            Result.success(amount)
        } else {
            res
        }
    }

    suspend fun submitWithdrawRequest(
        user: UserEntity,
        method: String,
        number: String,
        amount: Double
    ): Result<Unit> {
        val syncedUser = remote.ensureUserSyncedWithFirebase(user)
        if (syncedUser != user) {
            dao.clearUsers()
            dao.insertUser(syncedUser)
        }

        if (amount < 100.0) {
            return Result.failure(Exception("Minimum withdraw amount is 100 TK"))
        }
        if (amount > syncedUser.winning) {
            return Result.failure(Exception("Insufficient Winning Balance (Available: ৳${syncedUser.winning.toInt()})"))
        }

        val newWinning = syncedUser.winning - amount
        val updatedUser = syncedUser.copy(winning = newWinning)
        dao.insertUser(updatedUser)

        val df = SimpleDateFormat("M/d/yyyy, h:mm:ss a", Locale.getDefault())
        val txKey = "WD${System.currentTimeMillis().toString().takeLast(6)}"
        val tx = TransactionEntity(
            id = txKey,
            uid = syncedUser.uid,
            type = "Withdraw ($method)",
            amount = amount,
            number = number,
            method = method,
            status = "Pending",
            txId = txKey,
            date = df.format(Date())
        )
        dao.insertTransaction(tx)
        remote.syncUserBalance(syncedUser.uid, syncedUser.deposit, newWinning, syncedUser.idToken)
        remote.pushWithdrawRequest(updatedUser, tx)
        return Result.success(Unit)
    }

    suspend fun updateProfileDetails(
        user: UserEntity,
        newUsername: String,
        newPhone: String,
        newPass: String = ""
    ): Result<Unit> {
        val syncedUser = remote.ensureUserSyncedWithFirebase(user)
        val finalName = newUsername.ifBlank { syncedUser.username }
        val finalPhone = newPhone.ifBlank { syncedUser.phone }
        val updated = syncedUser.copy(
            username = finalName,
            phone = finalPhone
        )
        dao.clearUsers()
        dao.insertUser(updated)
        remote.updateUserProfileInFirebase(updated, finalName, finalPhone, newPass)
        return Result.success(Unit)
    }
}
