package com.kynzai.data.remote

import com.kynzai.data.remote.dto.ContactWireDto
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

internal fun JsonElement?.toStringList(): List<String> =
    when (this) {
        null, JsonNull -> emptyList()
        is JsonArray -> mapNotNull { element ->
            runCatching { element.jsonPrimitive.content }.getOrNull()
        }
        is JsonPrimitive -> {
            val raw = content.trim()
            if (raw.isEmpty()) {
                emptyList()
            } else if (raw.startsWith("[")) {
                runCatching {
                    Json.parseToJsonElement(raw).toStringList()
                }.getOrElse {
                    raw.splitSkills()
                }
            } else {
                raw.splitSkills()
            }
        }
        else -> emptyList()
    }

private fun String.splitSkills(): List<String> =
    split(',', ';')
        .map { it.trim() }
        .filter { it.isNotEmpty() }

internal fun JsonElement?.toContactWireList(): List<ContactWireDto> =
    when (this) {
        null, JsonNull -> emptyList()
        is JsonArray -> mapNotNull { element ->
            val obj = element as? JsonObject ?: return@mapNotNull null
            ContactWireDto(
                name = obj["name"]?.jsonPrimitive?.content.orEmpty(),
                link = obj["link"]?.jsonPrimitive?.content.orEmpty(),
            )
        }
        is JsonPrimitive -> {
            val raw = content.trim()
            if (raw.isEmpty()) {
                emptyList()
            } else {
                runCatching {
                    Json.decodeFromString<List<ContactWireDto>>(raw)
                }.getOrElse { emptyList() }
            }
        }
        else -> emptyList()
    }
