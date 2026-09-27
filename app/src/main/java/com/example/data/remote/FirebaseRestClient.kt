package com.example.data.remote

import android.util.Log
import com.example.data.local.AppSettingsData
import com.example.data.local.BannerItem
import com.example.data.local.CategoryEntity
import com.example.data.local.MatchEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.ParticipantEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class FirebaseRestClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val dbUrl = "https://bd-tournament-3-default-rtdb.firebaseio.com"
    private val apiKey = "AIzaSyCu-u7wsjMWDYGVUxwuF8iNMPvBUTAn--s"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    @Volatile
    var currentAuthToken: String = ""

    @Volatile
    private var lastAuthEmail: String = ""

    @Volatile
    private var lastAuthPass: String = ""

    private fun encodePathSegment(segment: String): String {
        return URLEncoder.encode(segment, "UTF-8").replace("+", "%20")
    }

    private fun buildDbUrl(path: String, token: String?): String {
        val normalizedPath = path.trimStart('/').split("/").joinToString("/") { seg ->
            if (seg.contains(" ") || seg.contains("#") || seg.contains("?")) encodePathSegment(seg) else seg
        }
        return if (!token.isNullOrBlank()) {
            "$dbUrl/$normalizedPath.json?auth=$token"
        } else {
            "$dbUrl/$normalizedPath.json"
        }
    }

    private fun refreshTokenIfNeeded(): String {
        if (lastAuthEmail.isNotBlank() && lastAuthPass.isNotBlank()) {
            try {
                val authUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$apiKey"
                val payload = JSONObject().apply {
                    put("email", lastAuthEmail)
                    put("password", lastAuthPass)
                    put("returnSecureToken", true)
                }
                val req = Request.Builder()
                    .url(authUrl)
                    .post(payload.toString().toRequestBody(jsonMediaType))
                    .build()
                client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val json = JSONObject(resp.body?.string() ?: "{}")
                        val newToken = json.optString("idToken", "")
                        if (newToken.isNotBlank()) {
                            currentAuthToken = newToken
                            return newToken
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }
        // Fallback: try anonymous sign-up to get a valid Firebase Auth idToken if RTDB rules require auth != null
        try {
            val anonUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$apiKey"
            val payload = JSONObject().apply { put("returnSecureToken", true) }
            val req = Request.Builder()
                .url(anonUrl)
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()
            client.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val json = JSONObject(resp.body?.string() ?: "{}")
                    val newToken = json.optString("idToken", "")
                    if (newToken.isNotBlank()) {
                        currentAuthToken = newToken
                        return newToken
                    }
                }
            }
        } catch (_: Exception) {
        }
        return currentAuthToken
    }

    private fun getJson(path: String, authToken: String? = null): JSONObject? {
        val tokenToUse = authToken?.ifBlank { currentAuthToken } ?: currentAuthToken
        val urlsToTry = mutableListOf<String>()
        if (tokenToUse.isNotBlank()) {
            urlsToTry.add(buildDbUrl(path, tokenToUse))
        }
        urlsToTry.add(buildDbUrl(path, null))

        for (url in urlsToTry) {
            try {
                val req = Request.Builder().url(url).get().build()
                client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val body = resp.body?.string() ?: return null
                        if (body == "null" || body.isBlank()) return null
                        return JSONObject(body)
                    } else if (resp.code == 401 || resp.code == 403) {
                        val refreshed = refreshTokenIfNeeded()
                        if (refreshed.isNotBlank() && refreshed != tokenToUse) {
                            val retryReq = Request.Builder().url(buildDbUrl(path, refreshed)).get().build()
                            client.newCall(retryReq).execute().use { retryResp ->
                                if (retryResp.isSuccessful) {
                                    val body = retryResp.body?.string() ?: return null
                                    if (body == "null" || body.isBlank()) return null
                                    return JSONObject(body)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("FirebaseRestClient", "GET $path failed: ${e.message}")
            }
        }
        return null
    }

    private fun putJson(path: String, payload: JSONObject, authToken: String? = null): Boolean {
        val tokenToUse = authToken?.ifBlank { currentAuthToken } ?: currentAuthToken
        val bodyStr = payload.toString()
        val urlsToTry = mutableListOf<String>()
        if (tokenToUse.isNotBlank()) {
            urlsToTry.add(buildDbUrl(path, tokenToUse))
        }
        urlsToTry.add(buildDbUrl(path, null))

        for (url in urlsToTry) {
            try {
                val req = Request.Builder()
                    .url(url)
                    .put(bodyStr.toRequestBody(jsonMediaType))
                    .build()
                client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) return true
                    if (resp.code == 401 || resp.code == 403) {
                        val refreshed = refreshTokenIfNeeded()
                        if (refreshed.isNotBlank() && refreshed != tokenToUse) {
                            val retryReq = Request.Builder()
                                .url(buildDbUrl(path, refreshed))
                                .put(bodyStr.toRequestBody(jsonMediaType))
                                .build()
                            client.newCall(retryReq).execute().use { retryResp ->
                                if (retryResp.isSuccessful) return true
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("FirebaseRestClient", "PUT $path failed: ${e.message}")
            }
        }
        return false
    }

    private fun patchJson(path: String, payload: JSONObject, authToken: String? = null): Boolean {
        val tokenToUse = authToken?.ifBlank { currentAuthToken } ?: currentAuthToken
        val bodyStr = payload.toString()
        val urlsToTry = mutableListOf<String>()
        if (tokenToUse.isNotBlank()) {
            urlsToTry.add(buildDbUrl(path, tokenToUse))
        }
        urlsToTry.add(buildDbUrl(path, null))

        for (url in urlsToTry) {
            try {
                val req = Request.Builder()
                    .url(url)
                    .patch(bodyStr.toRequestBody(jsonMediaType))
                    .build()
                client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) return true
                    if (resp.code == 401 || resp.code == 403) {
                        val refreshed = refreshTokenIfNeeded()
                        if (refreshed.isNotBlank() && refreshed != tokenToUse) {
                            val retryReq = Request.Builder()
                                .url(buildDbUrl(path, refreshed))
                                .patch(bodyStr.toRequestBody(jsonMediaType))
                                .build()
                            client.newCall(retryReq).execute().use { retryResp ->
                                if (retryResp.isSuccessful) return true
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("FirebaseRestClient", "PATCH $path failed: ${e.message}")
            }
        }
        return false
    }

    private fun postJson(path: String, payload: JSONObject, authToken: String? = null): String? {
        val tokenToUse = authToken?.ifBlank { currentAuthToken } ?: currentAuthToken
        val bodyStr = payload.toString()
        val urlsToTry = mutableListOf<String>()
        if (tokenToUse.isNotBlank()) {
            urlsToTry.add(buildDbUrl(path, tokenToUse))
        }
        urlsToTry.add(buildDbUrl(path, null))

        for (url in urlsToTry) {
            try {
                val req = Request.Builder()
                    .url(url)
                    .post(bodyStr.toRequestBody(jsonMediaType))
                    .build()
                client.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val respJson = JSONObject(resp.body?.string() ?: "{}")
                        return respJson.optString("name", "ok")
                    }
                    if (resp.code == 401 || resp.code == 403) {
                        val refreshed = refreshTokenIfNeeded()
                        if (refreshed.isNotBlank() && refreshed != tokenToUse) {
                            val retryReq = Request.Builder()
                                .url(buildDbUrl(path, refreshed))
                                .post(bodyStr.toRequestBody(jsonMediaType))
                                .build()
                            client.newCall(retryReq).execute().use { retryResp ->
                                if (retryResp.isSuccessful) {
                                    val respJson = JSONObject(retryResp.body?.string() ?: "{}")
                                    return respJson.optString("name", "ok")
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("FirebaseRestClient", "POST $path failed: ${e.message}")
            }
        }
        return null
    }

    private fun sanitizeAppName(raw: String): String {
        return raw
            .replace("Khelo Bangladesh", "Bd Tournament", ignoreCase = true)
            .replace("KheloBangladesh", "Bd Tournament", ignoreCase = true)
            .replace("Developer Sketvia Tour", "Bd Tournament", ignoreCase = true)
            .replace("Developer Sketvia", "Bd Tournament", ignoreCase = true)
            .replace("Sketvia Tour", "Bd Tournament", ignoreCase = true)
    }

    private fun deriveGoogleAccountPassword(email: String): String {
        val normalized = email.trim().lowercase()
        return "GoogleAuth#${normalized}#BdTour2026"
    }

    suspend fun fetchAppSettings(): AppSettingsData? = withContext(Dispatchers.IO) {
        val d = getJson("app_settings") ?: return@withContext null
        val bannersList = mutableListOf<BannerItem>()
        val bannersObj = d.optJSONObject("banners")
        if (bannersObj != null) {
            val keys = bannersObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val b = bannersObj.optJSONObject(k)
                if (b != null) {
                    val img = b.optString("img", "")
                    val link = b.optString("link", "")
                    if (img.isNotBlank()) bannersList.add(BannerItem(img, link))
                }
            }
        }
        AppSettingsData(
            appName = "Bd Tournament",
            appLogo = d.optString("app_logo", "https://cdn-icons-png.flaticon.com/512/149/149071.png"),
            notice = sanitizeAppName(d.optString("notice", "Welcome to Bd Tournament! Join matches and win big prizes.")),
            bkashNumber = d.optString("bkash_number", "01700-000000"),
            nagadNumber = d.optString("nagad_number", "01800-000000"),
            rocketNumber = d.optString("rocket_number", "01900-000000"),
            howToAddMoneyLink = d.optString("how_to_add_money_link", "https://youtube.com"),
            howToGetRoomIdLink = d.optString("how_to_get_room_id_link", "https://youtube.com"),
            howToPlayLink = d.optString("how_to_play_link", "https://youtube.com"),
            supportLink = d.optString("support_link", "https://t.me/DeveloperSketvia01"),
            shopLink = d.optString("shop_link", "https://t.me/DeveloperSketvia01"),
            showPopup = d.optBoolean("show_popup", true),
            popupText = sanitizeAppName(d.optString("popup_text", "আমাদের অ্যাপে আপনাকে স্বাগতম! প্রতিদিন টুর্নামেন্ট খেলে জিতে নিন আকর্ষণীয় পুরস্কার।")),
            banners = bannersList
        )
    }

    suspend fun seedInitialDatabaseIfNeeded(
        defaultSettings: AppSettingsData,
        defaultCategories: List<CategoryEntity>,
        defaultMatches: List<MatchEntity>,
        defaultParticipants: List<ParticipantEntity>,
        defaultNotifications: List<NotificationEntity>,
        authToken: String? = null
    ) = withContext(Dispatchers.IO) {
        try {
            if (getJson("app_settings", authToken) == null) {
                val settingsObj = JSONObject().apply {
                    put("app_name", "Bd Tournament")
                    put("app_logo", defaultSettings.appLogo)
                    put("notice", defaultSettings.notice)
                    put("bkash_number", defaultSettings.bkashNumber)
                    put("nagad_number", defaultSettings.nagadNumber)
                    put("rocket_number", defaultSettings.rocketNumber)
                    put("how_to_add_money_link", defaultSettings.howToAddMoneyLink)
                    put("how_to_get_room_id_link", defaultSettings.howToGetRoomIdLink)
                    put("how_to_play_link", defaultSettings.howToPlayLink)
                    put("support_link", defaultSettings.supportLink)
                    put("shop_link", defaultSettings.shopLink)
                    put("show_popup", defaultSettings.showPopup)
                    put("popup_text", defaultSettings.popupText)
                }
                putJson("app_settings", settingsObj, authToken)
            }

            if (getJson("categories", authToken) == null && defaultCategories.isNotEmpty()) {
                val catRoot = JSONObject()
                for (c in defaultCategories) {
                    val item = JSONObject().apply {
                        put("name", c.name)
                        put("img", c.img)
                    }
                    catRoot.put(c.id, item)
                }
                putJson("categories", catRoot, authToken)
            }

            if (getJson("matches", authToken) == null && defaultMatches.isNotEmpty()) {
                val matchRoot = JSONObject()
                for (m in defaultMatches) {
                    val item = JSONObject().apply {
                        put("categoryId", m.categoryId)
                        put("title", m.title)
                        put("time", m.time)
                        put("timestamp", m.timestamp)
                        put("total_prize", m.totalPrize)
                        put("type", m.type)
                        put("entry", m.entry)
                        put("per_kill", m.perKill)
                        put("map", m.map)
                        put("joined", m.joined)
                        put("total", m.total)
                        put("status", m.status)
                        put("room_id", m.roomId)
                        put("room_pass", m.roomPass)
                        put("prize_desc", m.prizeDesc)
                        put("description", m.matchDesc)
                        put("match_desc", m.matchDesc)
                    }
                    matchRoot.put(m.dbKey, item)
                }
                putJson("matches", matchRoot, authToken)
            }

            if (getJson("match_participants", authToken) == null && defaultParticipants.isNotEmpty()) {
                val partRoot = JSONObject()
                for (p in defaultParticipants) {
                    val mNode = partRoot.optJSONObject(p.matchKey) ?: JSONObject().also {
                        partRoot.put(p.matchKey, it)
                    }
                    val pItem = JSONObject().apply {
                        put("ign", p.ign)
                        put("joinedBy", p.joinedBy)
                        put("kills", p.kills)
                        put("win", p.win)
                    }
                    mNode.put(p.id, pItem)
                }
                putJson("match_participants", partRoot, authToken)
            }

            if (getJson("notifications", authToken) == null && defaultNotifications.isNotEmpty()) {
                val notifRoot = JSONObject()
                for (n in defaultNotifications) {
                    val item = JSONObject().apply {
                        put("title", n.title)
                        put("body", n.body)
                        put("timestamp", n.timestamp)
                    }
                    notifRoot.put(n.id, item)
                }
                putJson("notifications", notifRoot, authToken)
            }
        } catch (e: Exception) {
            Log.w("FirebaseRestClient", "seedInitialDatabaseIfNeeded failed: ${e.message}")
        }
    }

    suspend fun fetchCategories(): List<CategoryEntity> = withContext(Dispatchers.IO) {
        val d = getJson("categories") ?: return@withContext emptyList()
        val list = mutableListOf<CategoryEntity>()
        val keys = d.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            val v = d.optJSONObject(k) ?: continue
            list.add(
                CategoryEntity(
                    id = k,
                    name = v.optString("name", "Battle Royale"),
                    img = v.optString("img", "")
                )
            )
        }
        list
    }

    suspend fun fetchMatches(): List<MatchEntity> = withContext(Dispatchers.IO) {
        val d = getJson("matches") ?: return@withContext emptyList()
        val list = mutableListOf<MatchEntity>()
        val keys = d.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            val v = d.optJSONObject(k) ?: continue
            val prizeDesc = v.optString("prize_desc", "")
                .ifBlank { v.optString("prizeDesc", "") }
            val matchDesc = v.optString("description", "")
                .ifBlank { v.optString("match_desc", "") }
                .ifBlank { v.optString("matchDesc", "") }
                .ifBlank { v.optString("match_description", "") }
                .ifBlank { v.optString("matchDescription", "") }
                .ifBlank { v.optString("rules", "") }
                .ifBlank { v.optString("details", "") }
                .ifBlank { v.optString("desc", "") }

            list.add(
                MatchEntity(
                    dbKey = k,
                    categoryId = v.optString("categoryId", ""),
                    title = v.optString("title", "Free Fire Match"),
                    time = v.optString("time", ""),
                    timestamp = v.optLong("timestamp", System.currentTimeMillis() + 3600_000L),
                    totalPrize = v.optInt("total_prize", v.optInt("totalPrize", 500)),
                    type = v.optString("type", "Solo"),
                    entry = v.optInt("entry", 20),
                    perKill = v.optInt("per_kill", v.optInt("perKill", 10)),
                    map = v.optString("map", "Bermuda"),
                    joined = v.optInt("joined", 0),
                    total = v.optInt("total", 48),
                    status = v.optString("status", "Upcoming"),
                    roomId = v.optString("room_id", v.optString("roomId", "")),
                    roomPass = v.optString("room_pass", v.optString("roomPass", "")),
                    prizeDesc = prizeDesc,
                    matchDesc = matchDesc
                )
            )
        }
        list
    }

    suspend fun fetchParticipants(): List<ParticipantEntity> = withContext(Dispatchers.IO) {
        val d = getJson("match_participants") ?: return@withContext emptyList()
        val list = mutableListOf<ParticipantEntity>()
        val matchKeys = d.keys()
        while (matchKeys.hasNext()) {
            val mKey = matchKeys.next()
            val mObj = d.optJSONObject(mKey) ?: continue
            val pKeys = mObj.keys()
            while (pKeys.hasNext()) {
                val pKey = pKeys.next()
                val pObj = mObj.optJSONObject(pKey) ?: continue
                list.add(
                    ParticipantEntity(
                        id = if (pKey.startsWith("${mKey}_")) pKey else "${mKey}_$pKey",
                        matchKey = mKey,
                        ign = pObj.optString("ign", "Player"),
                        joinedBy = pObj.optString("joinedBy", ""),
                        kills = pObj.optInt("kills", 0),
                        win = pObj.optInt("win", 0)
                    )
                )
            }
        }
        list
    }

    suspend fun fetchNotifications(): List<NotificationEntity> = withContext(Dispatchers.IO) {
        val d = getJson("notifications") ?: return@withContext emptyList()
        val list = mutableListOf<NotificationEntity>()
        val keys = d.keys()
        var idx = 0L
        while (keys.hasNext()) {
            val k = keys.next()
            val v = d.optJSONObject(k) ?: continue
            list.add(
                NotificationEntity(
                    id = k,
                    title = sanitizeAppName(v.optString("title", "Update")),
                    body = sanitizeAppName(v.optString("body", "")),
                    timestamp = v.optLong("timestamp", System.currentTimeMillis() + (idx++))
                )
            )
        }
        list
    }

    fun generatePromoCodeForUser(username: String, email: String, uid: String): String {
        val alphaPrefix = username.replace(Regex("[^A-Za-z]"), "").uppercase().take(4)
            .ifBlank { email.substringBefore("@").replace(Regex("[^A-Za-z]"), "").uppercase().take(4) }
            .ifBlank { "BDT" }
        val seed = (uid.ifBlank { email.lowercase() }).hashCode().let { if (it < 0) -it else it }
        val digits = (1000 + (seed % 9000)).toString()
        return "$alphaPrefix$digits"
    }

    suspend fun fetchRemoteUser(uid: String, idToken: String): UserEntity? = withContext(Dispatchers.IO) {
        val userObj = getJson("users/$uid", idToken) ?: return@withContext null
        val username = userObj.optString("username", "")
        val email = userObj.optString("email", "")
        if (username.isBlank() && email.isBlank()) return@withContext null
        val resolvedName = username.ifBlank { email.substringBefore("@") }
        val promo = userObj.optString("promoCode", "").ifBlank {
            generatePromoCodeForUser(resolvedName, email, uid)
        }
        val referredBy = userObj.optString("referredBy", "")
        UserEntity(
            uid = uid,
            username = resolvedName,
            email = email,
            phone = userObj.optString("phone", ""),
            photoUrl = userObj.optString("photoUrl", ""),
            deposit = userObj.optDouble("deposit", 0.0),
            winning = userObj.optDouble("winning", 0.0),
            idToken = idToken.ifBlank { currentAuthToken },
            promoCode = promo,
            referredBy = referredBy
        )
    }

    suspend fun signInEmail(email: String, pass: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val cleanEmail = email.trim()
            val authUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$apiKey"
            val payload = JSONObject().apply {
                put("email", cleanEmail)
                put("password", pass)
                put("returnSecureToken", true)
            }
            val req = Request.Builder()
                .url(authUrl)
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()
            client.newCall(req).execute().use { resp ->
                val bodyStr = resp.body?.string() ?: "{}"
                val json = JSONObject(bodyStr)
                if (!resp.isSuccessful) {
                    val errMsg = json.optJSONObject("error")?.optString("message") ?: "Login failed"
                    return@withContext Result.failure(Exception(errMsg))
                }
                val uid = json.optString("localId")
                val idToken = json.optString("idToken")
                currentAuthToken = idToken
                lastAuthEmail = cleanEmail
                lastAuthPass = pass

                val displayName = json.optString("displayName", cleanEmail.substringBefore("@"))
                    .ifBlank { cleanEmail.substringBefore("@") }
                val userObj = getJson("users/$uid", idToken)
                val resolvedUsername = userObj?.optString("username", displayName)?.ifBlank { displayName } ?: displayName
                val resolvedPhone = userObj?.optString("phone", "") ?: ""
                val resolvedDeposit = userObj?.optDouble("deposit", 0.0) ?: 0.0
                val resolvedWinning = userObj?.optDouble("winning", 0.0) ?: 0.0
                val resolvedPromo = userObj?.optString("promoCode", "")?.ifBlank {
                    generatePromoCodeForUser(resolvedUsername, cleanEmail, uid)
                } ?: generatePromoCodeForUser(resolvedUsername, cleanEmail, uid)
                val resolvedReferredBy = userObj?.optString("referredBy", "") ?: ""

                // Always ensure the user node exists and is up to date in Firebase Realtime Database
                val userNode = JSONObject().apply {
                    put("uid", uid)
                    put("username", resolvedUsername)
                    put("email", cleanEmail)
                    put("phone", resolvedPhone)
                    put("deposit", resolvedDeposit)
                    put("winning", resolvedWinning)
                    put("promoCode", resolvedPromo)
                    if (resolvedReferredBy.isNotBlank()) {
                        put("referredBy", resolvedReferredBy)
                    }
                    put("lastLogin", System.currentTimeMillis())
                }
                putJson("users/$uid", userNode, idToken)

                val user = UserEntity(
                    uid = uid,
                    username = resolvedUsername,
                    email = cleanEmail,
                    phone = resolvedPhone,
                    deposit = resolvedDeposit,
                    winning = resolvedWinning,
                    idToken = idToken,
                    promoCode = resolvedPromo,
                    referredBy = resolvedReferredBy
                )
                Result.success(user)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUpEmail(
        username: String,
        email: String,
        phone: String,
        pass: String,
        referredByPromo: String = ""
    ): Result<UserEntity> =
        withContext(Dispatchers.IO) {
            try {
                val cleanEmail = email.trim()
                val cleanUsername = username.trim().ifBlank { cleanEmail.substringBefore("@") }
                val cleanPhone = phone.trim()
                val cleanRefPromo = referredByPromo.trim().uppercase()

                val authUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$apiKey"
                val payload = JSONObject().apply {
                    put("email", cleanEmail)
                    put("password", pass)
                    put("returnSecureToken", true)
                }
                val req = Request.Builder()
                    .url(authUrl)
                    .post(payload.toString().toRequestBody(jsonMediaType))
                    .build()
                client.newCall(req).execute().use { resp ->
                    val bodyStr = resp.body?.string() ?: "{}"
                    val json = JSONObject(bodyStr)
                    if (!resp.isSuccessful) {
                        val errMsg = json.optJSONObject("error")?.optString("message") ?: "Registration failed"
                        return@withContext Result.failure(Exception(errMsg))
                    }
                    val uid = json.optString("localId")
                    val idToken = json.optString("idToken")
                    currentAuthToken = idToken
                    lastAuthEmail = cleanEmail
                    lastAuthPass = pass
                    val ownPromoCode = generatePromoCodeForUser(cleanUsername, cleanEmail, uid)

                    // Update profile displayName in Firebase Authentication
                    val updateUrl = "https://identitytoolkit.googleapis.com/v1/accounts:update?key=$apiKey"
                    val updPayload = JSONObject().apply {
                        put("idToken", idToken)
                        put("displayName", cleanUsername)
                        put("returnSecureToken", false)
                    }
                    try {
                        client.newCall(
                            Request.Builder().url(updateUrl).post(updPayload.toString().toRequestBody(jsonMediaType)).build()
                        ).execute().close()
                    } catch (_: Exception) {
                    }

                    // Save user node in Firebase Realtime Database
                    val userNode = JSONObject().apply {
                        put("uid", uid)
                        put("username", cleanUsername)
                        put("email", cleanEmail)
                        put("phone", cleanPhone)
                        put("deposit", 0.0)
                        put("winning", 0.0)
                        put("promoCode", ownPromoCode)
                        if (cleanRefPromo.isNotBlank()) {
                            put("referredBy", cleanRefPromo)
                        }
                        put("createdAt", System.currentTimeMillis())
                        put("lastLogin", System.currentTimeMillis())
                    }
                    putJson("users/$uid", userNode, idToken)

                    Result.success(
                        UserEntity(
                            uid = uid,
                            username = cleanUsername,
                            email = cleanEmail,
                            phone = cleanPhone,
                            deposit = 0.0,
                            winning = 0.0,
                            idToken = idToken,
                            promoCode = ownPromoCode,
                            referredBy = cleanRefPromo
                        )
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun signInWithGoogleAccount(
        googleIdToken: String?,
        email: String,
        displayName: String,
        photoUrl: String,
        preserveDeposit: Double = 0.0,
        preserveWinning: Double = 0.0,
        preservePhone: String = ""
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val cleanEmail = email.trim().lowercase()
            var resolvedEmail = cleanEmail
            var resolvedName = displayName.trim().ifBlank { cleanEmail.substringBefore("@") }
            var uid = ""
            var firebaseIdToken = ""

            // Step 1: Try Google IdP token exchange if Credential Manager provided a Google ID token
            if (!googleIdToken.isNullOrBlank()) {
                try {
                    val idpUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithIdp?key=$apiKey"
                    val idpPayload = JSONObject().apply {
                        put("postBody", "id_token=$googleIdToken&providerId=google.com")
                        put("requestUri", "http://localhost")
                        put("returnIdpCredential", true)
                        put("returnSecureToken", true)
                    }
                    val req = Request.Builder()
                        .url(idpUrl)
                        .post(idpPayload.toString().toRequestBody(jsonMediaType))
                        .build()
                    client.newCall(req).execute().use { resp ->
                        if (resp.isSuccessful) {
                            val bodyStr = resp.body?.string() ?: "{}"
                            val json = JSONObject(bodyStr)
                            uid = json.optString("localId", "")
                            firebaseIdToken = json.optString("idToken", "")
                            resolvedEmail = json.optString("email", resolvedEmail)
                            resolvedName = json.optString("displayName", resolvedName).ifBlank { resolvedName }
                        }
                    }
                } catch (_: Exception) {
                }
            }

            // Step 2: Ensure the Google email is registered & authenticated in Firebase Authentication (identitytoolkit)
            // so the user ALWAYS shows up in Firebase Console -> Authentication -> Users and has a real Firebase UID & idToken!
            val googlePass = deriveGoogleAccountPassword(cleanEmail)
            if (firebaseIdToken.isBlank() || uid.isBlank()) {
                // 2a. Try signing in with deterministic Google Auth credential
                val signInUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$apiKey"
                val signInPayload = JSONObject().apply {
                    put("email", cleanEmail)
                    put("password", googlePass)
                    put("returnSecureToken", true)
                }
                client.newCall(
                    Request.Builder().url(signInUrl).post(signInPayload.toString().toRequestBody(jsonMediaType)).build()
                ).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val json = JSONObject(resp.body?.string() ?: "{}")
                        uid = json.optString("localId", "")
                        firebaseIdToken = json.optString("idToken", "")
                        lastAuthEmail = cleanEmail
                        lastAuthPass = googlePass
                    }
                }
            }

            if (firebaseIdToken.isBlank() || uid.isBlank()) {
                // 2b. Account does not exist yet in Firebase Authentication -> Create it via accounts:signUp!
                val signUpUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$apiKey"
                val signUpPayload = JSONObject().apply {
                    put("email", cleanEmail)
                    put("password", googlePass)
                    put("returnSecureToken", true)
                }
                client.newCall(
                    Request.Builder().url(signUpUrl).post(signUpPayload.toString().toRequestBody(jsonMediaType)).build()
                ).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val json = JSONObject(resp.body?.string() ?: "{}")
                        uid = json.optString("localId", "")
                        firebaseIdToken = json.optString("idToken", "")
                        lastAuthEmail = cleanEmail
                        lastAuthPass = googlePass
                    }
                }
            }

            // Update displayName and photoUrl in Firebase Authentication so it displays nicely in Firebase Console -> Authentication
            if (firebaseIdToken.isNotBlank()) {
                currentAuthToken = firebaseIdToken
                try {
                    val updateUrl = "https://identitytoolkit.googleapis.com/v1/accounts:update?key=$apiKey"
                    val updPayload = JSONObject().apply {
                        put("idToken", firebaseIdToken)
                        put("displayName", resolvedName)
                        if (photoUrl.isNotBlank()) {
                            put("photoUrl", photoUrl)
                        }
                        put("returnSecureToken", false)
                    }
                    client.newCall(
                        Request.Builder().url(updateUrl).post(updPayload.toString().toRequestBody(jsonMediaType)).build()
                    ).execute().close()
                } catch (_: Exception) {
                }
            } else {
                // 2c. If email was previously registered with a custom password, obtain an active Firebase token
                firebaseIdToken = refreshTokenIfNeeded()
                if (uid.isBlank()) {
                    // Check if user already exists in RTDB /users by email
                    val allUsers = getJson("users", firebaseIdToken)
                    if (allUsers != null) {
                        val uKeys = allUsers.keys()
                        while (uKeys.hasNext()) {
                            val k = uKeys.next()
                            val uObj = allUsers.optJSONObject(k) ?: continue
                            if (uObj.optString("email", "").equals(cleanEmail, ignoreCase = true)) {
                                uid = k
                                break
                            }
                        }
                    }
                }
                if (uid.isBlank()) {
                    uid = "google_" + cleanEmail.replace(Regex("[^a-z0-9]"), "_")
                }
            }

            // Step 3: Sync with Firebase Realtime Database (/users/$uid)
            val existingObj = getJson("users/$uid", firebaseIdToken)
            val remoteDeposit = existingObj?.optDouble("deposit", 0.0) ?: 0.0
            val remoteWinning = existingObj?.optDouble("winning", 0.0) ?: 0.0
            val finalDeposit = maxOf(remoteDeposit, preserveDeposit)
            val finalWinning = maxOf(remoteWinning, preserveWinning)
            val phone = existingObj?.optString("phone", "")?.ifBlank { preservePhone } ?: preservePhone
            val savedName = existingObj?.optString("username", resolvedName)?.ifBlank { resolvedName } ?: resolvedName
            val ownPromo = existingObj?.optString("promoCode", "")?.ifBlank {
                generatePromoCodeForUser(savedName, resolvedEmail, uid)
            } ?: generatePromoCodeForUser(savedName, resolvedEmail, uid)
            val referredBy = existingObj?.optString("referredBy", "") ?: ""

            val userNode = JSONObject().apply {
                put("uid", uid)
                put("username", savedName)
                put("email", resolvedEmail)
                put("phone", phone)
                put("photoUrl", photoUrl)
                put("provider", "google.com")
                put("deposit", finalDeposit)
                put("winning", finalWinning)
                put("promoCode", ownPromo)
                if (referredBy.isNotBlank()) {
                    put("referredBy", referredBy)
                }
                put("lastLogin", System.currentTimeMillis())
            }
            putJson("users/$uid", userNode, firebaseIdToken)

            Result.success(
                UserEntity(
                    uid = uid,
                    username = savedName,
                    email = resolvedEmail,
                    phone = phone,
                    photoUrl = photoUrl,
                    deposit = finalDeposit,
                    winning = finalWinning,
                    idToken = firebaseIdToken,
                    promoCode = ownPromo,
                    referredBy = referredBy
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun ensureUserSyncedWithFirebase(user: UserEntity): UserEntity = withContext(Dispatchers.IO) {
        if (user.email.isBlank()) return@withContext user
        // If user has an old local/unauthenticated Google session or empty idToken, register/sync with Firebase Auth & RTDB
        if (user.idToken.isBlank() || user.uid.startsWith("google_") || user.uid == "demo_player_bd") {
            val synced = signInWithGoogleAccount(
                googleIdToken = null,
                email = user.email,
                displayName = user.username,
                photoUrl = user.photoUrl,
                preserveDeposit = user.deposit,
                preserveWinning = user.winning,
                preservePhone = user.phone
            )
            if (synced.isSuccess) {
                return@withContext synced.getOrThrow()
            }
        } else {
            currentAuthToken = user.idToken
            val fallbackPromo = user.promoCode.ifBlank {
                generatePromoCodeForUser(user.username, user.email, user.uid)
            }
            // Also make sure /users/$uid node exists in Firebase RTDB
            val existing = getJson("users/${user.uid}", user.idToken)
            if (existing == null) {
                val userNode = JSONObject().apply {
                    put("uid", user.uid)
                    put("username", user.username)
                    put("email", user.email)
                    put("phone", user.phone)
                    put("photoUrl", user.photoUrl)
                    put("deposit", user.deposit)
                    put("winning", user.winning)
                    put("promoCode", fallbackPromo)
                    if (user.referredBy.isNotBlank()) {
                        put("referredBy", user.referredBy)
                    }
                    put("lastLogin", System.currentTimeMillis())
                }
                putJson("users/${user.uid}", userNode, user.idToken)
                return@withContext user.copy(promoCode = fallbackPromo)
            } else {
                val remoteDeposit = existing.optDouble("deposit", user.deposit)
                val remoteWinning = existing.optDouble("winning", user.winning)
                val remoteUsername = existing.optString("username", user.username).ifBlank { user.username }
                val remotePhone = existing.optString("phone", user.phone).ifBlank { user.phone }
                val remotePromo = existing.optString("promoCode", "").ifBlank { fallbackPromo }
                val remoteReferredBy = existing.optString("referredBy", user.referredBy)
                if (existing.optString("promoCode", "").isBlank()) {
                    patchJson("users/${user.uid}", JSONObject().apply { put("promoCode", remotePromo) }, user.idToken)
                }
                return@withContext user.copy(
                    username = remoteUsername,
                    phone = remotePhone,
                    deposit = remoteDeposit,
                    winning = remoteWinning,
                    promoCode = remotePromo,
                    referredBy = remoteReferredBy
                )
            }
        }
        user
    }

    private fun isApprovedStatus(status: String): Boolean {
        val s = status.trim().lowercase()
        return s == "approved" || s == "success" || s == "completed" || s == "accepted" || s == "approve"
    }

    private fun isRejectedStatus(status: String): Boolean {
        val s = status.trim().lowercase()
        return s == "rejected" || s == "cancelled" || s == "canceled" || s == "declined" || s == "failed" || s == "reject"
    }

    suspend fun syncAndFetchUserTransactions(user: UserEntity): List<TransactionEntity> =
        withContext(Dispatchers.IO) {
            val uid = user.uid
            val idToken = user.idToken
            val txRoot = getJson("transactions/$uid", idToken) ?: JSONObject()
            val depReqRoot = getJson("deposit_requests", idToken)
            val autoPayRoot = getJson("Auto Pay", idToken)
            val wdReqRoot = getJson("withdraw_requests", idToken)

            var totalNewlyCredited = 0.0
            var totalNewlyDebited = 0.0
            val list = mutableListOf<TransactionEntity>()
            val keys = txRoot.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val v = txRoot.optJSONObject(k) ?: continue
                val type = v.optString("type", "Transaction")
                val amount = v.optDouble("amount", 0.0)
                val number = v.optString("number", "").ifBlank { v.optString("senderNumber", "") }
                val method = v.optString("method", "")
                var status = v.optString("status", "Pending")
                val txId = v.optString("txID", k).ifBlank { k }
                val date = v.optString("date", "")
                var credited = v.optBoolean("credited", false)

                if (type.contains("Deposit", ignoreCase = true)) {
                    val depItem = depReqRoot?.optJSONObject(k) ?: depReqRoot?.optJSONObject(txId)
                    val autoItem = autoPayRoot?.optJSONObject(k) ?: autoPayRoot?.optJSONObject(txId)

                    val depStatus = depItem?.optString("status", "") ?: ""
                    val autoStatus = autoItem?.optString("status", "") ?: ""
                    val alreadyCreditedAnywhere = credited ||
                            (depItem?.optBoolean("credited", false) == true) ||
                            (autoItem?.optBoolean("credited", false) == true)

                    val approvedAnywhere = isApprovedStatus(status) ||
                            isApprovedStatus(depStatus) ||
                            isApprovedStatus(autoStatus)
                    val rejectedAnywhere = !approvedAnywhere && (
                            isRejectedStatus(status) ||
                                    isRejectedStatus(depStatus) ||
                                    isRejectedStatus(autoStatus)
                            )

                    if (approvedAnywhere) {
                        status = "Approved"
                        if (!alreadyCreditedAnywhere && amount > 0.0) {
                            credited = true
                            totalNewlyCredited += amount
                            val updateObj = JSONObject().apply {
                                put("status", "Approved")
                                put("credited", true)
                                put("approvedAt", System.currentTimeMillis())
                            }
                            patchJson("transactions/$uid/$k", updateObj, idToken)
                            patchJson("deposit_requests/$txId", updateObj, idToken)
                            patchJson("Auto Pay/$txId", updateObj, idToken)
                        } else if (!isApprovedStatus(v.optString("status", ""))) {
                            val updateObj = JSONObject().apply {
                                put("status", "Approved")
                                put("credited", true)
                            }
                            patchJson("transactions/$uid/$k", updateObj, idToken)
                        }
                    } else if (rejectedAnywhere && status != "Rejected") {
                        status = "Rejected"
                        val updateObj = JSONObject().apply {
                            put("status", "Rejected")
                        }
                        patchJson("transactions/$uid/$k", updateObj, idToken)
                        patchJson("deposit_requests/$txId", updateObj, idToken)
                        patchJson("Auto Pay/$txId", updateObj, idToken)
                    }
                } else if (type.contains("Withdraw", ignoreCase = true)) {
                    val wdItem = wdReqRoot?.optJSONObject(k) ?: wdReqRoot?.optJSONObject(txId)
                    val wdStatus = wdItem?.optString("status", "") ?: ""
                    val debited = v.optBoolean("debited", false)
                    val alreadyDebitedAnywhere = debited || (wdItem?.optBoolean("debited", false) == true)

                    val approvedAnywhere = isApprovedStatus(status) || isApprovedStatus(wdStatus)
                    val rejectedAnywhere = !approvedAnywhere && (isRejectedStatus(status) || isRejectedStatus(wdStatus))

                    if (approvedAnywhere) {
                        status = "Approved"
                        if (!alreadyDebitedAnywhere && amount > 0.0) {
                            totalNewlyDebited += amount
                            val updateObj = JSONObject().apply {
                                put("status", "Approved")
                                put("debited", true)
                                put("approvedAt", System.currentTimeMillis())
                            }
                            patchJson("transactions/$uid/$k", updateObj, idToken)
                            patchJson("withdraw_requests/$txId", updateObj, idToken)
                        } else if (!isApprovedStatus(v.optString("status", ""))) {
                            val updateObj = JSONObject().apply {
                                put("status", "Approved")
                                put("debited", true)
                            }
                            patchJson("transactions/$uid/$k", updateObj, idToken)
                        }
                    } else if (rejectedAnywhere && status != "Rejected") {
                        status = "Rejected"
                        val updateObj = JSONObject().apply {
                            put("status", "Rejected")
                        }
                        patchJson("transactions/$uid/$k", updateObj, idToken)
                        patchJson("withdraw_requests/$txId", updateObj, idToken)
                    }
                }

                list.add(
                    TransactionEntity(
                        id = k,
                        uid = uid,
                        type = type,
                        amount = amount,
                        number = number,
                        method = method,
                        status = status,
                        txId = txId,
                        date = date
                    )
                )
            }

            if (totalNewlyCredited > 0.0 || totalNewlyDebited > 0.0) {
                val remoteUserObj = getJson("users/$uid", idToken)
                val currentRemoteDeposit = remoteUserObj?.optDouble("deposit", user.deposit) ?: user.deposit
                val currentRemoteWinning = remoteUserObj?.optDouble("winning", user.winning) ?: user.winning
                val updatedDeposit = currentRemoteDeposit + totalNewlyCredited
                val updatedWinning = maxOf(0.0, currentRemoteWinning - totalNewlyDebited)
                syncUserBalance(uid, updatedDeposit, updatedWinning, idToken)

                // If user was referred by someone's promoCode and deposited >= 100 TK for the first time, credit 10 TK to referrer
                val refCode = remoteUserObj?.optString("referredBy", user.referredBy)?.trim().orEmpty()
                val bonusAlreadyPaid = remoteUserObj?.optBoolean("referralBonusPaid", false) == true
                if (totalNewlyCredited >= 100.0 && refCode.isNotBlank() && !bonusAlreadyPaid) {
                    val allUsers = getJson("users", idToken)
                    if (allUsers != null) {
                        val uKeys = allUsers.keys()
                        while (uKeys.hasNext()) {
                            val refUid = uKeys.next()
                            if (refUid == uid) continue
                            val refObj = allUsers.optJSONObject(refUid) ?: continue
                            val theirPromo = refObj.optString("promoCode", "").ifBlank {
                                generatePromoCodeForUser(
                                    refObj.optString("username", ""),
                                    refObj.optString("email", ""),
                                    refUid
                                )
                            }
                            if (theirPromo.equals(refCode, ignoreCase = true)) {
                                val refDep = refObj.optDouble("deposit", 0.0) + 10.0
                                patchJson("users/$refUid", JSONObject().apply { put("deposit", refDep) }, idToken)
                                patchJson("users/$uid", JSONObject().apply { put("referralBonusPaid", true) }, idToken)
                                break
                            }
                        }
                    }
                }
            }

            list
        }

    suspend fun fetchUserTransactions(uid: String, idToken: String): List<TransactionEntity> =
        withContext(Dispatchers.IO) {
            val d = getJson("transactions/$uid", idToken) ?: return@withContext emptyList()
            val list = mutableListOf<TransactionEntity>()
            val keys = d.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val v = d.optJSONObject(k) ?: continue
                list.add(
                    TransactionEntity(
                        id = k,
                        uid = uid,
                        type = v.optString("type", "Transaction"),
                        amount = v.optDouble("amount", 0.0),
                        number = v.optString("number", ""),
                        method = v.optString("method", ""),
                        status = v.optString("status", "Pending"),
                        txId = v.optString("txID", k),
                        date = v.optString("date", "")
                    )
                )
            }
            list
        }

    suspend fun syncUserBalance(uid: String, deposit: Double, winning: Double, idToken: String) =
        withContext(Dispatchers.IO) {
            val payload = JSONObject().apply {
                put("deposit", deposit)
                put("winning", winning)
                put("updatedAt", System.currentTimeMillis())
            }
            patchJson("users/$uid", payload, idToken)
        }

    suspend fun pushTransaction(user: UserEntity, tx: TransactionEntity) =
        withContext(Dispatchers.IO) {
            val payload = JSONObject().apply {
                put("id", tx.id)
                put("uid", user.uid)
                put("username", user.username)
                put("email", user.email)
                put("type", tx.type)
                put("amount", tx.amount)
                put("number", tx.number)
                put("method", tx.method)
                put("status", tx.status)
                put("txID", tx.txId)
                put("date", tx.date)
                put("timestamp", System.currentTimeMillis())
            }
            putJson("transactions/${user.uid}/${tx.id}", payload, user.idToken)
        }

    suspend fun pushWithdrawRequest(user: UserEntity, tx: TransactionEntity) =
        withContext(Dispatchers.IO) {
            val payload = JSONObject().apply {
                put("id", tx.id)
                put("txID", tx.txId)
                put("uid", user.uid)
                put("username", user.username)
                put("email", user.email)
                put("phone", user.phone)
                put("type", tx.type)
                put("amount", tx.amount)
                put("number", tx.number)
                put("method", tx.method)
                put("status", "Pending")
                put("debited", false)
                put("date", tx.date)
                put("timestamp", System.currentTimeMillis())
            }
            putJson("transactions/${user.uid}/${tx.id}", payload, user.idToken)
            putJson("withdraw_requests/${tx.id}", payload, user.idToken)
        }

    suspend fun pushParticipantAndCount(
        matchKey: String,
        ignList: List<String>,
        uid: String,
        newJoinedCount: Int,
        idToken: String
    ) = withContext(Dispatchers.IO) {
        for (ign in ignList) {
            val payload = JSONObject().apply {
                put("ign", ign)
                put("joinedBy", uid)
                put("kills", 0)
                put("win", 0)
            }
            postJson("match_participants/$matchKey", payload, idToken)
        }
        val mPayload = JSONObject().apply { put("joined", newJoinedCount) }
        patchJson("matches/$matchKey", mPayload, idToken)
    }

    suspend fun submitDepositRequest(
        trxId: String,
        enteredAmount: Double,
        senderNumber: String,
        method: String,
        dateStr: String,
        user: UserEntity
    ): Result<Double> = withContext(Dispatchers.IO) {
        try {
            val cleanTrx = trxId.trim().uppercase()
            val cleanSender = senderNumber.trim()
            if (cleanSender.length < 11) {
                return@withContext Result.failure(Exception("যে নাম্বার থেকে টাকা পাঠিয়েছেন সেই সঠিক একাউন্ট নাম্বার দিন (কমপক্ষে ১১ ডিজিট)"))
            }
            if (cleanTrx.length < 4) {
                return@withContext Result.failure(Exception("সঠিক Transaction ID দিন (কমপক্ষে ৪ অক্ষর)"))
            }
            if (enteredAmount <= 0.0) {
                return@withContext Result.failure(Exception("সঠিক টাকার পরিমাণ (Amount) লিখুন"))
            }

            // 1. Check if this Transaction ID was already submitted in Firebase RTDB
            val existingAutoPay = getJson("Auto Pay/$cleanTrx", user.idToken)
            val existingDepReq = getJson("deposit_requests/$cleanTrx", user.idToken)
            val existingUserTx = getJson("transactions/${user.uid}/$cleanTrx", user.idToken)
            if (existingAutoPay != null || existingDepReq != null || existingUserTx != null) {
                return@withContext Result.failure(Exception("এই Transaction ID ($cleanTrx) ইতিমধ্যে সাবমিট করা হয়েছে!"))
            }

            // 2. Save as Pending in Firebase Realtime Database (Admin will verify TrxID & Sender Number and change status to "Approved")
            val now = System.currentTimeMillis()
            val depositPayload = JSONObject().apply {
                put("id", cleanTrx)
                put("trxId", cleanTrx)
                put("txID", cleanTrx)
                put("uid", user.uid)
                put("username", user.username)
                put("email", user.email)
                put("phone", user.phone)
                put("type", "Deposit ($method)")
                put("method", method)
                put("amount", enteredAmount)
                put("number", cleanSender)
                put("senderNumber", cleanSender)
                put("status", "Pending")
                put("credited", false)
                put("date", dateStr)
                put("timestamp", now)
            }
            putJson("deposit_requests/$cleanTrx", depositPayload, user.idToken)
            putJson("Auto Pay/$cleanTrx", depositPayload, user.idToken)
            putJson("transactions/${user.uid}/$cleanTrx", depositPayload, user.idToken)

            Result.success(enteredAmount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfileInFirebase(
        user: UserEntity,
        newUsername: String,
        newPhone: String,
        newPass: String
    ): Boolean = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("username", newUsername)
            put("phone", newPhone)
            put("updatedAt", System.currentTimeMillis())
        }
        val ok = patchJson("users/${user.uid}", payload, user.idToken)
        if (user.idToken.isNotBlank()) {
            try {
                val updateUrl = "https://identitytoolkit.googleapis.com/v1/accounts:update?key=$apiKey"
                val updPayload = JSONObject().apply {
                    put("idToken", user.idToken)
                    put("displayName", newUsername)
                    if (newPass.length >= 6) {
                        put("password", newPass)
                    }
                    put("returnSecureToken", true)
                }
                client.newCall(
                    Request.Builder().url(updateUrl).post(updPayload.toString().toRequestBody(jsonMediaType)).build()
                ).execute().close()
            } catch (_: Exception) {
            }
        }
        ok
    }
}
