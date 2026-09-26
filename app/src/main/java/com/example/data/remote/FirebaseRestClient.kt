package com.example.data.remote

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
import java.util.concurrent.TimeUnit

class FirebaseRestClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val dbUrl = "https://bd-tournament-3-default-rtdb.firebaseio.com"
    private val apiKey = "AIzaSyCu-u7wsjMWDYGVUxwuF8iNMPvBUTAn--s"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun getJson(path: String, authToken: String? = null): JSONObject? {
        return try {
            val url = if (!authToken.isNullOrBlank()) {
                "$dbUrl/$path.json?auth=$authToken"
            } else {
                "$dbUrl/$path.json"
            }
            val req = Request.Builder().url(url).get().build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val body = resp.body?.string() ?: return null
                if (body == "null" || body.isBlank()) return null
                JSONObject(body)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun sanitizeAppName(raw: String): String {
        return raw
            .replace("Khelo Bangladesh", "Bd Tournament", ignoreCase = true)
            .replace("KheloBangladesh", "Bd Tournament", ignoreCase = true)
            .replace("Developer Sketvia Tour", "Bd Tournament", ignoreCase = true)
            .replace("Developer Sketvia", "Bd Tournament", ignoreCase = true)
            .replace("Sketvia Tour", "Bd Tournament", ignoreCase = true)
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
            list.add(
                MatchEntity(
                    dbKey = k,
                    categoryId = v.optString("categoryId", ""),
                    title = v.optString("title", "Free Fire Match"),
                    time = v.optString("time", ""),
                    timestamp = v.optLong("timestamp", System.currentTimeMillis() + 3600_000L),
                    totalPrize = v.optInt("total_prize", 500),
                    type = v.optString("type", "Solo"),
                    entry = v.optInt("entry", 20),
                    perKill = v.optInt("per_kill", 10),
                    map = v.optString("map", "Bermuda"),
                    joined = v.optInt("joined", 0),
                    total = v.optInt("total", 48),
                    status = v.optString("status", "Upcoming"),
                    roomId = v.optString("room_id", ""),
                    roomPass = v.optString("room_pass", ""),
                    prizeDesc = v.optString("prize_desc", "")
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
                        id = "${mKey}_$pKey",
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
                    timestamp = System.currentTimeMillis() + (idx++)
                )
            )
        }
        list
    }

    suspend fun signInEmail(email: String, pass: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val authUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$apiKey"
            val payload = JSONObject().apply {
                put("email", email)
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
                val displayName = json.optString("displayName", email.substringBefore("@"))
                val userObj = getJson("users/$uid", idToken) ?: getJson("users/$uid")
                val user = UserEntity(
                    uid = uid,
                    username = userObj?.optString("username", displayName)?.ifBlank { displayName } ?: displayName,
                    email = email,
                    phone = userObj?.optString("phone", "") ?: "",
                    deposit = userObj?.optDouble("deposit", 0.0) ?: 0.0,
                    winning = userObj?.optDouble("winning", 0.0) ?: 0.0,
                    idToken = idToken
                )
                Result.success(user)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUpEmail(username: String, email: String, phone: String, pass: String): Result<UserEntity> =
        withContext(Dispatchers.IO) {
            try {
                val authUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$apiKey"
                val payload = JSONObject().apply {
                    put("email", email)
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

                    // Update profile displayName
                    val updateUrl = "https://identitytoolkit.googleapis.com/v1/accounts:update?key=$apiKey"
                    val updPayload = JSONObject().apply {
                        put("idToken", idToken)
                        put("displayName", username)
                        put("returnSecureToken", false)
                    }
                    client.newCall(
                        Request.Builder().url(updateUrl).post(updPayload.toString().toRequestBody(jsonMediaType)).build()
                    ).execute().close()

                    // Save user node in RTDB
                    val userNode = JSONObject().apply {
                        put("username", username)
                        put("email", email)
                        put("phone", phone)
                        put("deposit", 0)
                        put("winning", 0)
                    }
                    client.newCall(
                        Request.Builder()
                            .url("$dbUrl/users/$uid.json?auth=$idToken")
                            .put(userNode.toString().toRequestBody(jsonMediaType))
                            .build()
                    ).execute().close()

                    Result.success(
                        UserEntity(
                            uid = uid,
                            username = username,
                            email = email,
                            phone = phone,
                            deposit = 0.0,
                            winning = 0.0,
                            idToken = idToken
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
        photoUrl: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            var uid = "google_" + email.lowercase().replace(Regex("[^a-z0-9]"), "_")
            var firebaseIdToken = ""
            var resolvedEmail = email
            var resolvedName = displayName.ifBlank { email.substringBefore("@") }

            // If we have a real Google ID token from Credential Manager, exchange it with Firebase IdentityToolkit
            if (!googleIdToken.isNullOrBlank()) {
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
                        uid = json.optString("localId", uid)
                        firebaseIdToken = json.optString("idToken", "")
                        resolvedEmail = json.optString("email", resolvedEmail)
                        resolvedName = json.optString("displayName", resolvedName).ifBlank { resolvedName }
                    }
                }
            }

            // Check if user already exists in Firebase Realtime Database
            val existingObj = getJson("users/$uid", firebaseIdToken) ?: getJson("users/$uid")
            val deposit = existingObj?.optDouble("deposit", 0.0) ?: 0.0
            val winning = existingObj?.optDouble("winning", 0.0) ?: 0.0
            val phone = existingObj?.optString("phone", "") ?: ""
            val savedName = existingObj?.optString("username", resolvedName)?.ifBlank { resolvedName } ?: resolvedName

            if (existingObj == null) {
                val userNode = JSONObject().apply {
                    put("username", savedName)
                    put("email", resolvedEmail)
                    put("phone", phone)
                    put("deposit", 0)
                    put("winning", 0)
                }
                val putUrl = if (firebaseIdToken.isNotBlank()) {
                    "$dbUrl/users/$uid.json?auth=$firebaseIdToken"
                } else {
                    "$dbUrl/users/$uid.json"
                }
                try {
                    client.newCall(
                        Request.Builder()
                            .url(putUrl)
                            .put(userNode.toString().toRequestBody(jsonMediaType))
                            .build()
                    ).execute().close()
                } catch (_: Exception) {
                }
            }

            Result.success(
                UserEntity(
                    uid = uid,
                    username = savedName,
                    email = resolvedEmail,
                    phone = phone,
                    photoUrl = photoUrl,
                    deposit = deposit,
                    winning = winning,
                    idToken = firebaseIdToken
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchUserTransactions(uid: String, idToken: String): List<TransactionEntity> =
        withContext(Dispatchers.IO) {
            val d = getJson("transactions/$uid", idToken) ?: getJson("transactions/$uid") ?: return@withContext emptyList()
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
                        status = v.optString("status", "Success"),
                        txId = v.optString("txID", k),
                        date = v.optString("date", "")
                    )
                )
            }
            list
        }

    suspend fun syncUserBalance(uid: String, deposit: Double, winning: Double, idToken: String) =
        withContext(Dispatchers.IO) {
            try {
                val payload = JSONObject().apply {
                    put("deposit", deposit)
                    put("winning", winning)
                }
                val url = if (idToken.isNotBlank()) "$dbUrl/users/$uid.json?auth=$idToken" else "$dbUrl/users/$uid.json"
                val req = Request.Builder()
                    .url(url)
                    .patch(payload.toString().toRequestBody(jsonMediaType))
                    .build()
                client.newCall(req).execute().close()
            } catch (_: Exception) {
            }
        }

    suspend fun pushTransaction(uid: String, tx: TransactionEntity, idToken: String) =
        withContext(Dispatchers.IO) {
            try {
                val payload = JSONObject().apply {
                    put("type", tx.type)
                    put("amount", tx.amount)
                    put("number", tx.number)
                    put("method", tx.method)
                    put("status", tx.status)
                    put("txID", tx.txId)
                    put("date", tx.date)
                }
                val url = if (idToken.isNotBlank()) {
                    "$dbUrl/transactions/$uid/${tx.id}.json?auth=$idToken"
                } else {
                    "$dbUrl/transactions/$uid/${tx.id}.json"
                }
                client.newCall(
                    Request.Builder().url(url).put(payload.toString().toRequestBody(jsonMediaType)).build()
                ).execute().close()
            } catch (_: Exception) {
            }
        }

    suspend fun pushParticipantAndCount(
        matchKey: String,
        ignList: List<String>,
        uid: String,
        newJoinedCount: Int,
        idToken: String
    ) = withContext(Dispatchers.IO) {
        try {
            for (ign in ignList) {
                val payload = JSONObject().apply {
                    put("ign", ign)
                    put("joinedBy", uid)
                    put("kills", 0)
                    put("win", 0)
                }
                val pUrl = if (idToken.isNotBlank()) {
                    "$dbUrl/match_participants/$matchKey.json?auth=$idToken"
                } else {
                    "$dbUrl/match_participants/$matchKey.json"
                }
                client.newCall(
                    Request.Builder().url(pUrl).post(payload.toString().toRequestBody(jsonMediaType)).build()
                ).execute().close()
            }
            val mPayload = JSONObject().apply { put("joined", newJoinedCount) }
            val mUrl = if (idToken.isNotBlank()) {
                "$dbUrl/matches/$matchKey.json?auth=$idToken"
            } else {
                "$dbUrl/matches/$matchKey.json"
            }
            client.newCall(
                Request.Builder().url(mUrl).patch(mPayload.toString().toRequestBody(jsonMediaType)).build()
            ).execute().close()
        } catch (_: Exception) {
        }
    }

    suspend fun verifyAutoPayTrx(trxId: String, uid: String, idToken: String): Result<Double> =
        withContext(Dispatchers.IO) {
            try {
                // Demo TrxIDs for testing in emulator anytime
                if (trxId.equals("DEMO100", ignoreCase = true) || trxId.equals("BKASH100", ignoreCase = true)) {
                    return@withContext Result.success(100.0)
                }
                if (trxId.equals("DEMO500", ignoreCase = true)) {
                    return@withContext Result.success(500.0)
                }

                val usedObj = getJson("Auto Pay/$trxId", idToken) ?: getJson("Auto Pay/$trxId")
                if (usedObj != null) {
                    return@withContext Result.failure(Exception("This TrxID has already been used!"))
                }
                val apiObj = getJson("XNXANIKPAY", idToken) ?: getJson("XNXANIKPAY")
                if (apiObj != null) {
                    val keys = apiObj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        val item = apiObj.optJSONObject(k) ?: continue
                        val itemTx = item.optString("txid", "")
                        if (itemTx.equals(trxId, ignoreCase = true)) {
                            val amount = item.optDouble("amount", 0.0)
                            if (amount > 0) {
                                val usedPayload = JSONObject().apply {
                                    put("amount", amount)
                                    put("usedBy", uid)
                                    put("time", System.currentTimeMillis())
                                }
                                val markUrl = if (idToken.isNotBlank()) {
                                    "$dbUrl/Auto Pay/$trxId.json?auth=$idToken"
                                } else {
                                    "$dbUrl/Auto Pay/$trxId.json"
                                }
                                client.newCall(
                                    Request.Builder().url(markUrl).put(usedPayload.toString().toRequestBody(jsonMediaType)).build()
                                ).execute().close()
                                return@withContext Result.success(amount)
                            }
                        }
                    }
                }
                Result.failure(Exception("Invalid TrxID. (Tip: Use DEMO100 to test instant 100 TK deposit)"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
