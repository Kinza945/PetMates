package com.kynzai.petmates.ui.common

import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kynzai.data.BuildConfig as DataBuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun RemoteAvatar(
    avatarUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    iconSize: Dp = size * 0.5f,
) {
    val normalizedUrl = avatarUrl.toAbsoluteAvatarUrl()

    // Передаем URL как ключ. Если он не менялся, produceState не перезапускается
    val image = produceState<ImageBitmap?>(initialValue = null, key1 = normalizedUrl) {
        if (normalizedUrl == null) {
            value = null
            return@produceState
        }

        // Сначала проверяем, нет ли аватарки в быстром кэше оперативки
        val cachedBitmap = AvatarMemoryCache.get(normalizedUrl)
        if (cachedBitmap != null) {
            value = cachedBitmap
        } else {
            // Если в кэше пусто — сбрасываем в null (пока качается) и идем в сеть
            value = null
            val downloadedBitmap = loadRemoteImage(normalizedUrl)
            if (downloadedBitmap != null) {
                // Сохраняем в кэш, чтобы при следующем скролле не скачивать заново
                AvatarMemoryCache.put(normalizedUrl, downloadedBitmap)
                value = downloadedBitmap
            }
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.LightGray),
        contentAlignment = Alignment.Center,
    ) {
        val bitmap = image.value
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}

/**
 * Простейший глобальный кэш картинок в оперативной памяти смартфона.
 * Ограничен 50 аватарками, чтобы не забивать память телефона.
 */
private object AvatarMemoryCache {
    private val cache = LruCache<String, ImageBitmap>(50)

    fun get(url: String): ImageBitmap? = cache.get(url)
    fun put(url: String, bitmap: ImageBitmap) {
        cache.put(url, bitmap)
    }
}

private fun String?.toAbsoluteAvatarUrl(): String? {
    val raw = this?.trim()?.takeIf { it.isNotBlank() } ?: return null
    if (raw.startsWith("http://", ignoreCase = true) || raw.startsWith("https://", ignoreCase = true)) {
        return raw
    }
    if (raw.startsWith("//")) {
        return "https:$raw"
    }

    val baseUrl = DataBuildConfig.API_BASE_URL
        .ifBlank { DataBuildConfig.SUPABASE_URL }
        .trimEnd('/')
        .takeIf { it.isNotBlank() }
        ?: return null

    return if (raw.startsWith("/")) {
        baseUrl + raw
    } else {
        "$baseUrl/$raw"
    }
}

private suspend fun loadRemoteImage(url: String): ImageBitmap? =
    withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            connection = (URL(url).openConnection() as? HttpURLConnection)?.apply {
                connectTimeout = 3_000 // Чуть ускорили таймауты
                readTimeout = 5_000
                instanceFollowRedirects = true
            } ?: return@withContext null

            connection.inputStream.use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
            }
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }