package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val img: String
)

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val dbKey: String,
    val categoryId: String,
    val title: String,
    val time: String,
    val timestamp: Long,
    val totalPrize: Int,
    val type: String,
    val entry: Int,
    val perKill: Int,
    val map: String,
    val joined: Int,
    val total: Int,
    val status: String, // "Upcoming", "Ongoing", "Finished"
    val roomId: String = "",
    val roomPass: String = "",
    val prizeDesc: String = "",
    val matchDesc: String = ""
)

@Entity(tableName = "participants")
data class ParticipantEntity(
    @PrimaryKey val id: String,
    val matchKey: String,
    val ign: String,
    val joinedBy: String,
    val kills: Int = 0,
    val win: Int = 0
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val uid: String,
    val type: String,
    val amount: Double,
    val number: String = "",
    val method: String = "",
    val status: String = "Success",
    val txId: String = "",
    val date: String = ""
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val username: String,
    val email: String,
    val phone: String,
    val photoUrl: String = "",
    val deposit: Double = 0.0,
    val winning: Double = 0.0,
    val idToken: String = "",
    val promoCode: String = "",
    val referredBy: String = ""
)

data class BannerItem(
    val img: String,
    val link: String = ""
)

data class LeaderboardPlayer(
    val id: String,
    val name: String,
    val avatarUrl: String = "",
    val wonAmount: Int = 0,
    val kills: Int = 0,
    val matchesPlayed: Int = 0,
    val dailyWon: Int = 0,
    val dailyKills: Int = 0,
    val dailyMatches: Int = 0,
    val weeklyWon: Int = 0,
    val weeklyKills: Int = 0,
    val weeklyMatches: Int = 0
)

fun resolveMatchInstructionsAndRules(match: MatchEntity): String {
    return when {
        match.matchDesc.isNotBlank() -> match.matchDesc
        match.prizeDesc.isNotBlank() -> match.prizeDesc
        else -> "• অবশ্যই আপনার সঠিক গেম আইডি (Game ID Name) দিয়ে জয়েন করবেন।\n• ম্যাচ শুরু হওয়ার ১০ মিনিট আগে Room ID এবং Password দেওয়া হবে।\n• যেকোনো ধরনের হ্যাক, প্যানেল বা টিমিং করলে একাউন্ট সাসপেন্ড করা হবে।"
    }
}

data class AppSettingsData(
    val appName: String = "Bd Tournament",
    val appLogo: String = "https://cdn-icons-png.flaticon.com/512/149/149071.png",
    val notice: String = "Welcome to Bd Tournament! Join matches and win big prizes. ১০০% ট্রাস্টেড ফ্রি ফায়ার টুর্নামেন্ট অ্যাপ!",
    val bkashNumber: String = "01700-000000",
    val nagadNumber: String = "01800-000000",
    val rocketNumber: String = "01900-000000",
    val howToAddMoneyLink: String = "https://youtube.com",
    val howToGetRoomIdLink: String = "https://youtube.com",
    val howToPlayLink: String = "https://youtube.com",
    val supportLink: String = "https://t.me/DeveloperSketvia01",
    val shopLink: String = "https://t.me/DeveloperSketvia01",
    val showPopup: Boolean = true,
    val popupText: String = "আমাদের অ্যাপে আপনাকে স্বাগতম!\nপ্রতিদিন ফ্রি ফায়ার টুর্নামেন্ট খেলে জিতে নিন হাজার হাজার টাকা।\nযেকোনো সমস্যায় আমাদের টেলিগ্রাম সাপোর্টে যোগাযোগ করুন।",
    val banners: List<BannerItem> = emptyList()
)
