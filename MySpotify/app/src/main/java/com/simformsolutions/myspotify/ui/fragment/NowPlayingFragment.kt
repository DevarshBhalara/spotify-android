package com.simformsolutions.myspotify.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.media.MediaSession2Service.MediaNotification
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.navigation.fragment.navArgs
import com.simformsolutions.myspotify.R
import com.simformsolutions.myspotify.SongService
import com.simformsolutions.myspotify.data.model.local.ItemType
import com.simformsolutions.myspotify.data.model.local.TrackItem
import com.simformsolutions.myspotify.databinding.FragmentNowPlayingBinding
import com.simformsolutions.myspotify.ui.activity.MainActivity
import com.simformsolutions.myspotify.ui.base.BaseFragment
import com.simformsolutions.myspotify.ui.viewmodel.MainViewModel
import com.simformsolutions.myspotify.ui.viewmodel.NowPlayingViewModel
import com.simformsolutions.myspotify.utils.AppConstants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class NowPlayingFragment : BaseFragment<FragmentNowPlayingBinding, NowPlayingViewModel>() {

    private val args: NowPlayingFragmentArgs by navArgs()
    private val activityViewModel: MainViewModel by activityViewModels()



    var isPlaying = true

    override val viewModel: NowPlayingViewModel by viewModels()

    override fun getLayoutResId(): Int = R.layout.fragment_now_playing

    private lateinit var mService: SongService
    private var mBound: Boolean = false

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as SongService.LocalBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }


    override fun initialize() {

        super.initialize()
//        requireContext().registerReceiver(broadcastReceiver, IntentFilter().apply {
//            addAction(AppConstants.INTENT_PREVIOUS)
//            addAction(AppConstants.INTENT_PLAY_PAUSE)
//            addAction(AppConstants.INTENT_NEXT)
//        })
        setupUI()
    }

    override fun initializeObservers() {
        super.initializeObservers()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch {
                    viewModel.name.collectLatest { name ->
                        activityViewModel.setSubtitle(name)
                    }
                }

                launch {
                    viewModel.track.collectLatest { track ->
                        track?.let {
                            //setupNotification(track)
                            val intent = Intent(requireContext(), SongService::class.java).also {intent ->
                                requireContext().bindService(intent, connection, Context.BIND_AUTO_CREATE)
                                intent.putExtra("title", track.title)
                                intent.putExtra("artist", track.artists)
                            }
                            requireContext().startService(intent)
                        }

                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        activityViewModel.setAppBarScrollingEnabled(true)
        activityViewModel.setSubtitle("")
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        super.onDestroyView()
    }


    @SuppressLint("SourceLockedOrientationActivity")
    private fun setupUI() {
        //createNotificationChannel()
        activityViewModel.setAppBarScrollingEnabled(false)
        setupTitle()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        viewModel.setupPlayingQueue(args.trackId, args.id, args.type)


        binding.btnPlayPause.setOnClickListener {

            if (mBound) {
                isPlaying = if (isPlaying) {
                    mService.pauseSong()
                    false
                } else {
                    mService.resumeSong()
                    true
                }
            }
        }
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
               requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun setupNotification(track: TrackItem?) {
        val playPauseAction = NotificationCompat.Action(R.drawable.ic_play_alt_24, null, null)

        val intent = Intent("abc")
        val pendingIntent = PendingIntent.getActivity(requireContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val previousIntent = Intent("abc")
        val previousPendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            previousIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val playPauseIntent = Intent("playPause")
        val playPausePendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            1,
            playPauseIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent = Intent("next")
        val nextPendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            2,
            nextIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(requireContext(), AppConstants.CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_music_note_24)
            .setContentTitle(track?.title)
            .setContentText(track?.artists)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .addAction(R.drawable.ic_previous_24, null, previousPendingIntent)
            .addAction(R.drawable.ic_play_alt_24, null, playPausePendingIntent)
            .addAction(R.drawable.ic_next_24, null, nextPendingIntent)
            .setStyle(MediaStyle())

        with(NotificationManagerCompat.from(requireContext())) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(1, notificationBuilder.build())
        }
    }

    private fun setupTitle() {
        val type = if (args.type == ItemType.TRACK) {
            ItemType.ALBUM
        } else {
            args.type
        }
        requireActivity().title =
            getString(R.string.playing_from, type.getLocalizedName(requireContext()))
    }
}