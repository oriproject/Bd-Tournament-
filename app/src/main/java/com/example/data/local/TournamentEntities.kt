package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

private val explicitDateTimePatterns = listOf(
    "yyyy-MM-dd, hh:mm a",
    "yyyy-MM-dd, h:mm a",
    "yyyy-MM-dd hh:mm a",
    "yyyy-MM-dd h:mm a",
    "yyyy-MM-dd, HH:mm:ss",
    "yyyy-MM-dd, HH:mm",
    "yyyy-MM-dd HH:mm:ss",
    "yyyy-MM-dd HH:mm",
    "yyyy-MM-dd'T'HH:mm:ss",
    "yyyy-MM-dd'T'HH:mm",
    "dd-MM-yyyy, hh:mm a",
    "dd-MM-yyyy, h:mm a",
    "dd-MM-yyyy hh:mm a",
    "dd-MM-yyyy h:mm a",
    "dd-MM-yyyy, HH:mm",
    "dd-MM-yyyy HH:mm",
    "dd/MM/yyyy, hh:mm a",
    "dd/MM/yyyy, h:mm a",
    "dd/MM/yyyy hh:mm a",
    "dd/MM/yyyy h:mm a",
    "dd/MM/yyyy, HH:mm",
    "dd/MM/yyyy HH:mm",
    "MM/dd/yyyy, hh:mm a",
    "MM/dd/yyyy, h:mm a",
    "MM/dd/yyyy hh:mm a",
    "MM/dd/yyyy h:mm a",
    "yyyy/MM/dd, hh:mm a",
    "yyyy/MM/dd, h:mm a",
    "yyyy/MM/dd hh:mm a",
    "yyyy/MM/dd h:mm a",
    "yyyy/MM/dd, HH:mm",
    "yyyy/MM/dd HH:mm",
    "d MMM yyyy, hh:mm a",
    "d MMM yyyy hh:mm a",
    "MMM d, yyyy, hh:mm a",
    "MMM d, yyyy hh:mm a",
    "yyyy-MM-dd",
    "dd-MM-yyyy",
    "dd/MM/yyyy"
)

private val timeOnlyPatterns = listOf(
    "hh:mm a",
    "h:mm a",
    "hh:mm:ss a",
    "h:mm:ss a",
    "HH:mm:ss",
    "HH:mm"
)

fun parseMatchScheduleToTimestamp(scheduleText: String, fallbackTimestamp: Long = 0L): Long {
    val normalizedFallback = if (fallbackTimestamp in 1..99_999_999_999L) {
        fallbackTimestamp * 1000L
    } else {
        fallbackTimestamp
    }

    val cleaned = scheduleText
        .trim()
        .replace(Regex("(?i)a\\.m\\.?"), "AM")
        .replace(Regex("(?i)p\\.m\\.?"), "PM")
        .replace(Regex("(\\d)(?i)(am|pm)\\b"), "$1 $2")
        .replace(Regex("\\s+"), " ")
        .replace(Regex("\\b(?i)am\\b"), "AM")
        .replace(Regex("\\b(?i)pm\\b"), "PM")

    if (cleaned.isBlank()) {
        return normalizedFallback
    }

    // 1. Check explicit full Date + Time patterns first (e.g., "2026-10-01, 04:00 pm")
    for (pattern in explicitDateTimePatterns) {
        try {
            val sdf = SimpleDateFormat(pattern, Locale.ENGLISH).apply {
                isLenient = false
            }
            val parsed = sdf.parse(cleaned)
            if (parsed != null) {
                return parsed.time
            }
        } catch (_: Exception) {
        }
    }

    // 2. Check relative day prefixes: "Today, 08:30 PM", "Tomorrow, 04:00 PM", "Tonight, 11:00 PM", "Yesterday, 04:00 PM"
    val lower = cleaned.lowercase(Locale.ENGLISH)
    val relativeOffsetDays: Int? = when {
        lower.startsWith("today") || lower.startsWith("tonight") -> 0
        lower.startsWith("tomorrow") -> 1
        lower.startsWith("yesterday") -> -1
        else -> null
    }
    if (relativeOffsetDays != null) {
        val timePart = cleaned.substringAfter(",", cleaned.substringAfter(" ", "")).trim()
        for (tPattern in timeOnlyPatterns) {
            try {
                val tFormat = SimpleDateFormat(tPattern, Locale.ENGLISH).apply { isLenient = false }
                val parsedTime = tFormat.parse(timePart) ?: continue
                val timeCal = Calendar.getInstance().apply { time = parsedTime }
                val targetCal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, relativeOffsetDays)
                    set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val candidate = targetCal.timeInMillis
                if (relativeOffsetDays == 0 && normalizedFallback > System.currentTimeMillis() && candidate < System.currentTimeMillis()) {
                    return normalizedFallback
                }
                return candidate
            } catch (_: Exception) {
            }
        }
    }

    // 3. Check time-only strings (e.g., "04:00 PM" or "16:00")
    for (tPattern in timeOnlyPatterns) {
        try {
            val tFormat = SimpleDateFormat(tPattern, Locale.ENGLISH).apply { isLenient = false }
            val parsedTime = tFormat.parse(cleaned) ?: continue
            val timeCal = Calendar.getInstance().apply { time = parsedTime }
            val baseCal = Calendar.getInstance().apply {
                if (normalizedFallback > 1_000_000_000_000L) {
                    timeInMillis = normalizedFallback
                }
                set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return baseCal.timeInMillis
        } catch (_: Exception) {
        }
    }

    return normalizedFallback
}

fun resolveMatchStartTimestamp(match: MatchEntity): Long {
    val parsed = parseMatchScheduleToTimestamp(match.time, match.timestamp)
    return if (parsed > 0L) parsed else match.timestamp
}

fun formatMatchScheduleTime(match: MatchEntity): String {
    val raw = match.time.trim()
    if (raw.isNotBlank()) {
        return raw
    }
    val ts = resolveMatchStartTimestamp(match)
    if (ts > 1_000_000_000_000L) {
        val sdf = SimpleDateFormat("yyyy-MM-dd, hh:mm a", Locale.ENGLISH)
        return sdf.format(Date(ts))
    }
    return "Upcoming"
}

fun formatMatchCountdown(match: MatchEntity, currentTimeMillis: Long): String {
    if (match.status.equals("Finished", ignoreCase = true)) {
        return "Match Finished"
    }
    val startMillis = resolveMatchStartTimestamp(match)
    if (startMillis <= 0L) {
        return if (match.time.isNotBlank()) "Starts At: ${match.time}" else "Upcoming"
    }
    val diff = startMillis - currentTimeMillis
    if (diff <= 0L) {
        return "Match Started"
    }
    val totalSeconds = diff / 1000L
    val days = totalSeconds / 86400L
    val hours = (totalSeconds % 86400L) / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return if (days > 0L) {
        "Starts In: ${days}d ${hours}h:${minutes}m:${seconds}s"
    } else {
        "Starts In: ${hours}h:${minutes}m:${seconds}s"
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
