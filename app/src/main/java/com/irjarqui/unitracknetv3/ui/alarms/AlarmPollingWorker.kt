package com.irjarqui.unitracknetv3.ui.alarms

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.irjarqui.unitracknetv3.R
import com.irjarqui.unitracknetv3.data.remote.repository.BgpRepository
import com.irjarqui.unitracknetv3.data.remote.repository.OspfRepository
import com.irjarqui.unitracknetv3.ui.alarms.model.AlarmStatus
import com.irjarqui.unitracknetv3.ui.topology.view.TopologyStatusActivity
import com.irjarqui.unitracknetv3.utils.RetrofitHelper
import com.irjarqui.unitracknetv3.utils.TopologyHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class AlarmPollingWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val ospfRepo by lazy { OspfRepository(RetrofitHelper.getOspfService()) }
    private val bgpRepo by lazy { BgpRepository(RetrofitHelper.getBgpService()) }
    private val prefs by lazy {
        applicationContext.getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        )
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val ctx: Context = applicationContext
        Log.d(TAG, ctx.getString(R.string.worker_ejecut_ndose))

        return@withContext try {
            val ospf = ospfRepo.getSegmentos() ?: emptyList()
            val bgp = bgpRepo.getSegmentos() ?: emptyList()

            val alarmas = TopologyHelper.buildAlarmList(bgp, ospf)
            val criticas = alarmas.filter { it.status == AlarmStatus.ERROR }

            if (criticas.isEmpty()) {
                Log.d(TAG, ctx.getString(R.string.sin_alarmas_cr_ticas_limpio_hash_ts_y_salgo))
                prefs.edit().clear().apply()
                return@withContext Result.success()
            }

            val now = System.currentTimeMillis()
            val lastTs = prefs.getLong(KEY_LAST_TS, 0L)
            val diffMin = (now - lastTs) / 60_000

            if (now - lastTs < REMIND_EVERY_HOURS * 3_600_000L) {
                Log.d(
                    TAG,
                    ctx.getString(R.string.a_n_dentro_de_la_ventana_de_silencio_no_notifico)
                )
                return@withContext Result.success()
            }

            prefs.edit().putLong(KEY_LAST_TS, now).apply()
            Log.d(TAG, ctx.getString(R.string.notificando, criticas))
            notificar(criticas.size)

            Result.success()

        } catch (e: Exception) {
            Log.e(TAG, ctx.getString(R.string.excepci_n_en_worker), e)
            Result.retry()
        }
    }

    private fun notificar(totalErrores: Int) {
        val ctx = applicationContext
        val mgr = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            mgr.getNotificationChannel(CHANNEL_ID) == null
        ) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                ctx.getString(R.string.notif_alarm_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = ctx.getString(R.string.notif_alarm_channel_desc)
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 600, 300, 600)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    null
                )
            }
            mgr.createNotificationChannel(channel)
        }

        val title = ctx.getString(R.string.notif_alarm_title, totalErrores)
        val body = ctx.getString(R.string.notif_alarm_body)

        val mapIntent = Intent(ctx, TopologyStatusActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        val pendingMap = PendingIntent.getActivity(
            ctx, 0, mapIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notif = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setColor(Color.RED)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 600, 300, 600))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .setContentIntent(pendingMap)
            .build()

        mgr.notify(NOTIF_ID, notif)
        Log.d(TAG, ctx.getString(R.string.notificaci_n_enviada))
    }


    companion object {
        const val TAG = "AlarmPollingWorker"
        private const val CHANNEL_ID = "alarm_channel_id"
        private const val NOTIF_ID = 1001
        private const val PREFS = "alarms_prefs"
        private const val KEY_LAST_TS = "last_ts"

        private const val REMIND_EVERY_HOURS = 0
    }
}

fun Context.scheduleAlarmPolling() {
    val request = PeriodicWorkRequestBuilder<AlarmPollingWorker>(15, TimeUnit.MINUTES)
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
        .addTag(AlarmPollingWorker.TAG)
        .build()

    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        AlarmPollingWorker.TAG,
        ExistingPeriodicWorkPolicy.UPDATE,
        request
    )

    WorkManager.getInstance(this).getWorkInfosByTag(AlarmPollingWorker.TAG)
        .get()
        .forEach { info ->
            Log.d(
                getString(R.string.app),
                getString(R.string.workinfo_id_state, info.id, info.state)
            )
        }
}
