package com.voidplayer.music.playback

import android.content.Context
import android.net.ConnectivityManager
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.voidplayer.music.constants.AudioQuality
import com.voidplayer.music.constants.SmartCacheEnabledKey
import com.voidplayer.music.constants.SmartCacheLimitKey
import com.voidplayer.music.constants.SongSortType
import com.voidplayer.music.db.MusicDatabase
import com.voidplayer.music.utils.YTPlayerUtils
import com.voidplayer.music.utils.dataStore
import com.voidplayer.music.utils.get
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SmartCacheWorker(
    private val appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val enabled = appContext.dataStore.get(SmartCacheEnabledKey, true)
        if (!enabled) {
            Timber.tag(TAG).d("SmartCacheWorker disabled in settings")
            return Result.success()
        }

        val limit = appContext.dataStore.get(SmartCacheLimitKey, 50)
        Timber.tag(TAG).d("SmartCacheWorker starting pre-cache (limit=$limit)")

        val database = com.voidplayer.music.db.InternalDatabase.newInstance(appContext)
        val connectivityManager =
            appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return try {
            val likedSongs = database.likedSongs(SongSortType.CREATE_DATE, descending = true).first()
            val mostPlayed = database.mostPlayedSongs(fromTimeStamp = 0L, limit = limit).first()
            val librarySongs = database.songsByCreateDateAsc().first()

            val candidateSongs = (likedSongs + mostPlayed + librarySongs)
                .distinctBy { it.id }
                .take(limit)

            var cachedCount = 0

            for (song in candidateSongs) {
                if (isStopped) break
                try {
                    val result = YTPlayerUtils.playerResponseForPlayback(
                        videoId = song.id,
                        audioQuality = AudioQuality.OPUS,
                        connectivityManager = connectivityManager,
                        context = appContext
                    )
                    if (result.isSuccess) {
                        cachedCount++
                    }
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "Failed to smart cache song ${song.id}")
                }
            }

            Timber.tag(TAG).i("SmartCacheWorker completed: pre-cached $cachedCount songs across liked/history/library")
            Result.success()
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "SmartCacheWorker error")
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "SmartCacheWorker"
        private const val WORK_NAME = "smart_cache_work"

        fun schedule(context: Context, wifiOnly: Boolean = true) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<SmartCacheWorker>(12, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
