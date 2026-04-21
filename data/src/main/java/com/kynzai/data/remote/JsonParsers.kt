package com.kynzai.data.remote

import org.json.JSONArray
import org.json.JSONObject

internal fun parseJsonArray(raw: String): JSONArray = JSONArray(raw)

internal fun JSONArrayObjects(arr: JSONArray): Sequence<JSONObject> = sequence {
    for (i in 0 until arr.length()) {
        val obj = arr.optJSONObject(i) ?: continue
        yield(obj)
    }
}

