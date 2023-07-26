package com.simformsolutions.myspotify

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.simformsolutions.myspotify.data.model.local.ItemType
import com.simformsolutions.myspotify.ui.activity.MainActivity
import com.simformsolutions.myspotify.utils.AppConstants


class SongService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private val binder = LocalBinder()

    val broadcastReceiver = NowPlayingReceiver()


    inner class LocalBinder : Binder() {
        fun getService(): SongService = this@SongService
    }

    override fun onCreate() {
        Log.d("service", "Service Created")
        mediaPlayer = MediaPlayer.create(this, R.raw.out_of_time)
        registerReceiver(broadcastReceiver, IntentFilter().apply {
            addAction(AppConstants.INTENT_PREVIOUS)
            addAction(AppConstants.INTENT_PLAY_PAUSE)
            addAction(AppConstants.INTENT_NEXT)
        })
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("service", "Service Started")
        mediaPlayer?.start()
        setupNotification(intent?.getStringExtra("title") ?: "N/A", intent?.getStringExtra("artist") ?: "N/A", intent?.getStringExtra("id") ?: "")

        return START_STICKY
    }

    fun pauseSong() {
        mediaPlayer?.pause()
    }

    fun resumeSong() {
        mediaPlayer?.start()
    }

    override fun stopService(name: Intent?): Boolean {
        mediaPlayer?.pause()
        return true
    }

    override fun onDestroy() {
        mediaPlayer?.stop()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "my_channel"
            val descriptionText = "mydesc"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(AppConstants.CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun setupNotification(title: String, artist: String, id: String) {


        val args = bundleOf(
            "trackId" to id,
            "id" to id,
            "type" to ItemType.TRACK
        )

        val pendingIntent = NavDeepLinkBuilder(this)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.nowPlayingFragment, args)
            .createPendingIntent()

        val previousIntent = Intent("abc")
        val previousPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            previousIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val playPauseIntent = Intent("playPause")
        val playPausePendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            playPauseIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent = Intent("next")
        val nextPendingIntent = PendingIntent.getBroadcast(
            this,
            2,
            nextIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, AppConstants.CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_music_note_24)
            .setContentTitle(title)
            .setContentText(artist)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .addAction(R.drawable.ic_previous_24, null, previousPendingIntent)
            .addAction(R.drawable.ic_play_alt_24, null, playPausePendingIntent)
            .addAction(R.drawable.ic_next_24, null, nextPendingIntent)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(1))

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@SongService,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(1, notificationBuilder.build())
        }
    }

    inner class NowPlayingReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action) {
                AppConstants.INTENT_PREVIOUS -> Toast.makeText(this@SongService, "Previous click", Toast.LENGTH_SHORT).show()
                AppConstants.INTENT_PLAY_PAUSE -> {
                    if (mediaPlayer?.isPlaying == true) {
                        mediaPlayer?.pause()
                    } else {
                        mediaPlayer?.start()
                    }
                }
                AppConstants.INTENT_NEXT -> Toast.makeText(this@SongService, "Next click", Toast.LENGTH_SHORT).show()
            }
        }
    }

}