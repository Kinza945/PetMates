package com.kynzai.data.network

import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

internal fun JSONArray.toStringList(): List<String> =
    buildList {
        for (i in 0 until length()) {
            val v = opt(i)
            if (v is String) add(v)
        }
    }

internal fun JSONObject.optInstant(name: String): Instant? =
    optString(name, null)?.takeIf { !it.isNullOrBlank() }?.let { Instant.parse(it) }

internal fun JSONObject.optStringOrNull(name: String): String? =
    optString(name, null)?.takeIf { !it.isNullOrBlank() }

